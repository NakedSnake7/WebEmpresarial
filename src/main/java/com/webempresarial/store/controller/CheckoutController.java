package com.webempresarial.store.controller;

import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import com.webempresarial.store.model.*;
import com.webempresarial.store.model.Order.PaymentMethod;
import com.webempresarial.store.service.OrderService;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private static final Logger logger =
            LoggerFactory.getLogger(CheckoutController.class);

    private static final double LIMITE_ENVIO_GRATIS = 1250.0;
    private static final double COSTO_ENVIO = 120.0;

    private final OrderService orderService;
    private final UserService userService;
    private final StoreResolver storeResolver;

    public CheckoutController(
            OrderService orderService,
            UserService userService,
            StoreResolver storeResolver
    ) {
        this.orderService = orderService;
        this.userService = userService;
        this.storeResolver = storeResolver;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(
            @Valid @RequestBody CheckoutRequestDTO checkoutRequest,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        try {
            orderService.validarStockCheckout(checkoutRequest, store);

            String direccion = checkoutRequest.getCustomer().getAddress();

            if (direccion == null || direccion.trim().length() < 5) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "La dirección debe tener al menos 5 caracteres"
                ));
            }

            Cliente cliente = userService.findOrCreateUserByEmail(
                    checkoutRequest.getCustomer().getEmail(),
                    checkoutRequest.getCustomer().getFullName(),
                    checkoutRequest.getCustomer().getPhone(),
                    store
            );

            String direccionCheckout =
                    checkoutRequest.getCustomer().getAddress();

            if (direccionCheckout != null &&
                    direccionCheckout.trim().length() >= 5) {

                if (cliente.getDefaultAddress() == null ||
                        !cliente.getDefaultAddress()
                                .equalsIgnoreCase(direccionCheckout.trim())) {

                    cliente.setDefaultAddress(
                            direccionCheckout.trim()
                    );
                }
            }

            userService.saveUser(cliente, store);

            double subtotal = checkoutRequest.getCart().stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();

            double envio =
                    subtotal >= LIMITE_ENVIO_GRATIS
                            ? 0.0
                            : COSTO_ENVIO;

            double totalFinal = subtotal + envio;

            PaymentMethod metodoPago =
                    "STRIPE".equalsIgnoreCase(
                            checkoutRequest.getPaymentMethod()
                    )
                            ? PaymentMethod.STRIPE
                            : PaymentMethod.TRANSFER;

            String emailNormalizado =
                    checkoutRequest.getCustomer()
                            .getEmail()
                            .trim()
                            .toLowerCase();

            Order order = new Order(
                    cliente,
                    totalFinal,
                    direccion,
                    checkoutRequest.getCustomer().getFullName(),
                    emailNormalizado
            );

            order.setStore(store);
            order.setPaymentMethod(metodoPago);
            order.setPaymentStatus(PaymentStatus.PENDING);

            checkoutRequest.getCart().forEach(cartItem -> {

                Producto producto;
                ProductoVariante variante = null;

                if (cartItem.getVarianteId() != null) {

                    variante = orderService.obtenerVarianteConLock(
                            cartItem.getVarianteId(),
                            store
                    );

                    producto = variante.getProducto();

                } else {

                    producto = orderService.buscarProducto(
                            cartItem.getProductId(),
                            store
                    );
                }

                OrderItem item = new OrderItem(
                        producto,
                        cartItem.getQuantity(),
                        BigDecimal.valueOf(cartItem.getPrice()),
                        order
                );

                item.setProductName(cartItem.getName());

                if (variante != null) {
                    item.setVariante(variante);
                }

                order.addItem(item);
            });

            if (metodoPago == PaymentMethod.TRANSFER) {

                orderService.saveOrderTransferencia(
                        order,
                        store
                );

            } else {

                orderService.crearOrden(
                        order,
                        store
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "message", "¡Orden creada correctamente!"
            ));

        } catch (Exception e) {

            logger.error("Error en checkout", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al procesar la orden"
                    ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errors", errors
        ));
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Store store = storeResolver.getCurrentStore(request);

        Cliente cliente =
                userService.findByEmail(
                        userDetails.getUsername(),
                        store
                ).orElseThrow();

        return ResponseEntity.ok(Map.of(
                "email", cliente.getEmail(),
                "fullName", cliente.getFullName(),
                "phone", cliente.getPhone(),
                "address", cliente.getDefaultAddress()
        ));
    }
}