package com.webempresarial.store.commerce.application.checkout;

import java.math.BigDecimal;   

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderItem;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.commerce.domain.order.Order.PaymentMethod;
import com.webempresarial.store.commerce.domain.order.OrderAuditAction;

import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.application.order.OrderAuditService;

import com.webempresarial.store.commerce.application.catalog.CatalogProductQueryService;

@Service
public class CheckoutService {

    private static final BigDecimal LIMITE_ENVIO_GRATIS =
            new BigDecimal("1250.00");

    private static final BigDecimal COSTO_ENVIO =
            new BigDecimal("120.00");

    private final OrderService orderService;
    private final UserService userService;

    private final OrderAuditService orderAuditService;
    
    private static final String ORDER_CREATED_REASON =
            "Orden creada desde checkout";
    
    private final CatalogProductQueryService catalogProductQueryService;	
    
    public CheckoutService(
            OrderService orderService,
            UserService userService,
            OrderAuditService orderAuditService,
            CatalogProductQueryService catalogProductQueryService
    ) {
        this.orderService = orderService;
        this.userService = userService;
        this.orderAuditService = orderAuditService;
        this.catalogProductQueryService =
                catalogProductQueryService;
    }

    @Transactional
    public Order createOrder(
            CheckoutRequestDTO request,
            Store store
    ) {

    	if (store == null || store.getId() == null || !store.isActiva()) {
    	    throw new IllegalArgumentException(
    	            "La tienda no está disponible"
    	    );
    	}
    	
        validateRequest(request);


        Cliente cliente = resolveCustomer(request, store);

        PaymentMethod paymentMethod =
                resolvePaymentMethod(request.getPaymentMethod());

        String email = request.getCustomer()
                .getEmail()
                .trim()
                .toLowerCase();

        String address = request.getCustomer()
                .getAddress()
                .trim();

        Order order = new Order(
        	    cliente,
        	    BigDecimal.ZERO,
        	    address,
        	    request.getCustomer().getFullName(),
        	    email
        	);
        
        order.setStore(store);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;
        
        Map<CartKey, Integer> groupedCart =
                new LinkedHashMap<>();

        for (var cartItem : request.getCart()) {

            Integer quantity = cartItem.getQuantity();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor a cero"
                );
            }

            if (cartItem.getProductId() == null) {
                throw new IllegalArgumentException(
                        "El ID del producto es obligatorio"
                );
            }

            CartKey key = new CartKey(
                    cartItem.getProductId(),
                    cartItem.getVarianteId()
            );

            groupedCart.merge(
                    key,
                    quantity,
                    Math::addExact
            );
        }

        var groupedItems = groupedCart.entrySet()
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        (Map.Entry<CartKey, Integer> entry) ->
                                                entry.getKey().varianteId() != null
                                                        ? 0
                                                        : 1
                                )
                                .thenComparing(
                                        entry ->
                                                entry.getKey().varianteId() != null
                                                        ? entry.getKey().varianteId()
                                                        : entry.getKey().productId()
                                )
                )
                .toList();

        for (var groupedItem : groupedItems) {

            CartKey key = groupedItem.getKey();
            Integer quantity = groupedItem.getValue();

            Producto producto;
            ProductoVariante variante = null;
            BigDecimal unitPrice;

            if (key.varianteId() != null) {

                variante = catalogProductQueryService.obtenerVarianteConLock(
                        key.varianteId(),
                        store
                );

                producto = variante.getProducto();

                if (producto == null
                        || producto.getId() == null
                        || !producto.getId().equals(key.productId())) {
                    throw new IllegalArgumentException(
                            "La variante no pertenece al producto indicado"
                    );
                }

                if (variante.getStock() < quantity) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para la variante de "
                                    + producto.getProductName()
                    );
                }

                unitPrice = variante.getPrecioFinal();

            } else {

                producto = catalogProductQueryService.obtenerProductoConLock(
                        key.productId(),
                        store
                );

                if (producto.getStockSimple() < quantity) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para el producto: "
                                    + producto.getProductName()
                    );
                }

                unitPrice = producto.getPrecioConDescuento();
            }

            if (unitPrice == null
                    || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Precio inválido para el producto: "
                                + producto.getId()
                );
            }

            unitPrice = unitPrice.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);

            OrderItem item = new OrderItem(
                    producto,
                    quantity,
                    unitPrice,
                    order
            );

            item.setProductName(
                    producto.getProductName()
            );

            if (variante != null) {
                item.setVariante(variante);
            }

            order.addItem(item);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal shipping =
                subtotal.compareTo(LIMITE_ENVIO_GRATIS) >= 0
                        ? BigDecimal.ZERO
                        : COSTO_ENVIO;

        shipping = shipping.setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal
                .add(shipping)
                .setScale(2, RoundingMode.HALF_UP);

        order.setTotal(total);
        
        Order savedOrder;

        if (paymentMethod == PaymentMethod.TRANSFER) {

            savedOrder = orderService.saveOrderTransferencia(
                    order,
                    store
            );

        } else {

            savedOrder = orderService.crearOrden(
                    order,
                    store
            );
        }

        orderAuditService.record(
                savedOrder,
                OrderAuditAction.ORDER_CREATED,
                null,
                null,
                ORDER_CREATED_REASON
        );

        return savedOrder;
    }

    private void validateRequest(CheckoutRequestDTO request) {
    	


        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de checkout es obligatoria"
            );
        }

        if (request.getCustomer() == null) {
            throw new IllegalArgumentException(
                    "Los datos del cliente son obligatorios"
            );
        }

        if (request.getCart() == null
                || request.getCart().isEmpty()) {
            throw new IllegalArgumentException(
                    "El carrito no puede estar vacío"
            );
        }
    	if (request.getCustomer().getEmail() == null
    	        || request.getCustomer().getEmail().isBlank()) {
    	    throw new IllegalArgumentException(
    	            "El correo del cliente es obligatorio"
    	    );
    	}

    	if (request.getCustomer().getFullName() == null
    	        || request.getCustomer().getFullName().isBlank()) {
    	    throw new IllegalArgumentException(
    	            "El nombre del cliente es obligatorio"
    	    );
    	}

    	if (request.getPaymentMethod() == null
    	        || request.getPaymentMethod().isBlank()) {
    	    throw new IllegalArgumentException(
    	            "El método de pago es obligatorio"
    	    );
    	}

        String address = request.getCustomer().getAddress();

        if (address == null || address.trim().length() < 5) {
            throw new IllegalArgumentException(
                    "La dirección debe tener al menos 5 caracteres"
            );
        }
    }

    private Cliente resolveCustomer(
            CheckoutRequestDTO request,
            Store store
    ) {
        Cliente cliente = userService.findOrCreateUserByEmail(
                request.getCustomer().getEmail(),
                request.getCustomer().getFullName(),
                request.getCustomer().getPhone(),
                store
        );

        cliente.setFullName(
                request.getCustomer().getFullName()
        );

        cliente.setPhone(
                request.getCustomer().getPhone()
        );

        cliente.setDefaultAddress(
                request.getCustomer()
                        .getAddress()
                        .trim()
        );

        return userService.saveUser(cliente, store);
    }

    private PaymentMethod resolvePaymentMethod(
            String paymentMethod
    ) {

        if ("STRIPE".equalsIgnoreCase(paymentMethod)) {
            return PaymentMethod.STRIPE;
        }

        if ("TRANSFER".equalsIgnoreCase(paymentMethod)) {
            return PaymentMethod.TRANSFER;
        }

        throw new IllegalArgumentException(
                "Método de pago no válido"
        );
    }
    
    private record CartKey(
            Long productId,
            Long varianteId
    ) {
    }
    
}