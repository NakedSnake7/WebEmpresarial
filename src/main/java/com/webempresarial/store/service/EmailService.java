package com.webempresarial.store.service;

import com.fasterxml.jackson.databind.ObjectMapper; 
import com.webempresarial.store.config.MailerSendConfig;
import com.webempresarial.store.feature.runtime.TraceType;
import com.webempresarial.store.feature.runtime.annotations.Trace;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private static final double ENVIO_COSTO = 120;
    private static final double ENVIO_GRATIS_MIN = 1250;
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(MX_ZONE);
    

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    
    private final MailerSendConfig config;
    
    public EmailService(MailerSendConfig config) {
        this.config = config;
    }




    /* =====================================================
       MÉTODO BASE mailsender
    ===================================================== */

    @Trace(
            type = TraceType.HTTP,
            name = "MailerSend API - Send Email",
            source = "MailerSend"
    )
    public void enviarCorreoHTML(String destinatario, String asunto, String htmlCuerpo)
            throws IOException {
        config.validate();

        try {

            var body = Map.of(
                    "from", Map.of(
                            "email", config.getFromEmail(),
                            "name", config.getFromName()
                    ),
                    "to", new Object[]{
                            Map.of("email", destinatario)
                    },
                    "subject", asunto,
                    "html", htmlCuerpo,
                    "reply_to", Map.of(
                            "email", config.getReplyTo()
                    )
            );

            var objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(body);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getApiUrl()))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var client = HttpClient.newHttpClient();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int code = response.statusCode();

            if (code < 200 || code >= 300) {
                log.error("❌ MailerSend error {} → {}", code, response.body());
                throw new IOException("Error enviando correo. Intenta más tarde.");
            }

            log.info("✅ Correo enviado correctamente a {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Error MailerSend", e);
            throw new IOException("Fallo al enviar correo", e);
        }
    }


/* =====================================================
    CORREO: DATOS DE TRANSFERENCIA
 ===================================================== */
 public void enviarCorreoDatosTransferencia(Order order) throws IOException {

     if (order.getCustomerEmail() == null) return;

     String template = cargarTemplate("email/email-transferencia.html");

     BigDecimal subtotal = BigDecimal.ZERO;
     StringBuilder productos = new StringBuilder();

     for (OrderItem item : order.getItems()) {
    	 BigDecimal sub = item.getPrice()
    		        .multiply(BigDecimal.valueOf(item.getQuantity()));
    	 
    	 subtotal = subtotal.add(sub);
    	 
         productos.append("<tr>")
                 .append("<td>").append(nombreProductoSeguro(item)).append("</td>")
                 .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                 .append("<td style='text-align:center;'>$")
                 .append(MONEY.format(sub))
                 .append("</td>")
                 .append("</tr>");
     }

     BigDecimal envioCosto = calcularEnvio(subtotal);

     String envio = envioCosto.compareTo(BigDecimal.ZERO) == 0
             ? "GRATIS"
             : "$" + MONEY.format(envioCosto);

     BigDecimal total = subtotal.add(envioCosto);
     
     
     String html = template
             .replace("{NOMBRE}", order.getCustomerName())
             .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
             .replace("{LISTADO_PRODUCTOS}", productos.toString())
             .replace("{SUBTOTAL}", MONEY.format(subtotal))
             .replace("{ENVIO}", envio)
             .replace("{TOTAL}", MONEY.format(total));

     enviarCorreoHTML(
             order.getCustomerEmail(),
             "💳 Instrucciones de pago - Orden #" + order.getId(),
             html
     );
 }

    /* =====================================================
       CORREO: CONFIRMACIÓN DE PEDIDO
    ===================================================== */
    public void enviarCorreoPedidoProcesado(
            String destinatario,
            String nombre,
            Long orderId,
            List<OrderItem> items
    ) throws IOException {

        String template = cargarTemplate("email/order-processed.html");

        BigDecimal subtotal = BigDecimal.ZERO;
        StringBuilder productos = new StringBuilder();

        for (OrderItem item : items) {
        	BigDecimal sub = item.getPrice()
        	        .multiply(BigDecimal.valueOf(item.getQuantity()));

        	subtotal = subtotal.add(sub);

            productos.append("<tr>")
                    .append("<td>").append(nombreProductoSeguro(item)).append("</td>")
                    .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='text-align:center;'>$")
                    .append(MONEY.format(sub))
                    .append("</td>")
                    .append("</tr>");
        }

        BigDecimal envioCosto = calcularEnvio(subtotal);

        String envio = envioCosto.compareTo(BigDecimal.ZERO) == 0
                ? "GRATIS"
                : "$" + MONEY.format(envioCosto);

        BigDecimal total = subtotal.add(envioCosto);
        
        String html = template
                .replace("{NOMBRE}", nombre)
                .replace("{NUMERO_ORDEN}", String.valueOf(orderId))
                .replace("{LISTADO_PRODUCTOS}", productos.toString())
                .replace("{SUBTOTAL}", MONEY.format(subtotal))
                .replace("{ENVIO}", envio)
                .replace("{TOTAL}", MONEY.format(total));

        enviarCorreoHTML(destinatario, "✅ Confirmación de tu pedido #" + orderId, html);
    }

    /* =====================================================
       CORREO: ORDEN EXPIRADA
    ===================================================== */
    public void enviarCorreoOrdenExpirada(Order order, LocalDateTime fechaLimite)
            throws IOException {

        if (order.getCustomerEmail() == null) return;

        String template = cargarTemplate("email/email-order-expired.html");

        BigDecimal subtotal = BigDecimal.ZERO;
        StringBuilder productos = new StringBuilder();

        for (OrderItem item : order.getItems()) {
        	BigDecimal sub = item.getPrice()
        	        .multiply(BigDecimal.valueOf(item.getQuantity()));

        	subtotal = subtotal.add(sub);

            productos.append("<tr>")
                    .append("<td>").append(nombreProductoSeguro(item)).append("</td>")
                    .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='text-align:center;'>$")
                    .append(MONEY.format(sub))
                    .append("</td>")
                    .append("</tr>");
        }

        BigDecimal envioCosto = calcularEnvio(subtotal);
        String envio = envioCosto.compareTo(BigDecimal.ZERO) == 0
                ? "GRATIS"
                : "$" + MONEY.format(envioCosto);
        BigDecimal total = subtotal.add(envioCosto);
        
        String html = template
                .replace("{NOMBRE}", order.getCustomerName() != null
                        ? order.getCustomerName()
                        : "Cliente")
                .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                .replace("{FECHA_EXPIRACION}", fechaLimite.format(DATE_FORMAT))
                .replace("{LISTADO_PRODUCTOS}", productos.toString())
                .replace("{SUBTOTAL}", MONEY.format(subtotal))
                .replace("{ENVIO}", envio)
                .replace("{TOTAL}", MONEY.format(total));

        enviarCorreoHTML(order.getCustomerEmail(), "⏰ Orden expirada - WeedTlan", html);
    }

    /* =====================================================
       CORREO: PEDIDO ENVIADO
    ===================================================== */
    public void enviarCorreoEnvio(
            String destinatario,
            String customerName,
            Long orderId,
            String shippingDate,
            String trackingNumber,
            String carrier
    ) throws IOException {

        String template = cargarTemplate("email/shipping-confirmation.html");

        String html = template
                .replace("{CUSTOMER_NAME}", customerName)
                .replace("{ORDER_ID}", String.valueOf(orderId))
                .replace("{SHIPPING_DATE}", shippingDate)
                .replace("{TRACKING_NUMBER}", trackingNumber)
                .replace("{CARRIER}", carrier);

        enviarCorreoHTML(
                destinatario,
                "📦 Tu pedido está en camino - Orden #" + orderId,
                html
        );
    }

    /* =====================================================
       UTILIDADES
    ===================================================== */
   

    private BigDecimal calcularEnvio(BigDecimal subtotal) {

        if (subtotal.compareTo(BigDecimal.valueOf(ENVIO_GRATIS_MIN)) >= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(ENVIO_COSTO);
    }

    private String cargarTemplate(String path) throws IOException {
        return templateCache.computeIfAbsent(path, p -> {
            try (var is = getClass().getClassLoader().getResourceAsStream(p)) {
                if (is == null) {
                    throw new RuntimeException("Template no encontrado: " + p);
                }
                return new String(is.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private String nombreProductoSeguro(OrderItem item) {

        if (item.getProducto() == null) {
            return "Producto";
        }

        try {
            return item.getProductName();
        } catch (Exception e) {
            log.warn("No se pudo cargar producto del item {}", item.getId());
            return "Producto";
        }
    }
}
