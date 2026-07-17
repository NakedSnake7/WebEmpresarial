package com.webempresarial.store.service;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import com.webempresarial.store.dto.order.OrderRequestDTO;
import com.webempresarial.store.dto.producto.reportes.ProductoVentaDTO;
import com.webempresarial.store.exceptions.OrderNotFoundException;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderStatus;
import com.webempresarial.store.model.OrderTransition;
import com.webempresarial.store.model.OrderTransitionContext;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.InventoryMovementRepository;
import com.webempresarial.store.repository.OrderOutboxRepository;
import com.webempresarial.store.repository.OrderRepository;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository productoVarianteRepository;
    private final StockService stockService;
    private final NotificationService notificationService;
    private final OrderStateMachine orderStateMachine;
    private final OrderOutboxRepository orderOutboxRepository;
    private final InventoryMovementRepository
    inventoryMovementRepository;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(
            OrderRepository orderRepository,
            ProductoRepository productoRepository,
            ProductoVarianteRepository productoVarianteRepository,
            StockService stockService,
            NotificationService notificationService,
            OrderStateMachine orderStateMachine,
            OrderOutboxRepository orderOutboxRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.orderRepository = orderRepository;
        this.productoRepository = productoRepository;
        this.productoVarianteRepository = productoVarianteRepository;
        this.stockService = stockService;
        this.notificationService = notificationService;
        this.orderStateMachine = orderStateMachine;
        this.orderOutboxRepository = orderOutboxRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public Optional<Order> findByStripeSessionId(String stripeSessionId, Store store) {
        return orderRepository.findByStripeSessionIdAndStore(stripeSessionId, store);
    }

    public List<Order> findOrdersFiltered(
            OrderStatus status,
            PaymentStatus payment,
            LocalDateTime from,
            LocalDateTime to,
            Store store
    ) {
        return orderRepository.findFilteredWithCliente(status, payment, from, to, store);
    }

    public Producto buscarProducto(Long productId, Store store) {
        return productoRepository.findByIdConTodo(productId, store)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));
    }

    public Order getOrderByIdWithUserAndItems(Long id, Store store) {
        return orderRepository.findByIdWithClienteAndItemsAndStore(id, store)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }

    public List<Order> findAllOrders(Store store) {
        return orderRepository.findAllWithCliente(store);
    }
    

    public Order getById(Long id, Store store) {
        return orderRepository.findByIdAndStore(id, store)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));
    }

 


    @Transactional
    public Producto obtenerProductoConLock(
            Long productId,
            Store store
    ) {
        return productoRepository
                .findByIdForUpdate(productId, store)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado: " + productId
                        )
                );
    }
 
    
    /* =====================================================
    reclamar ordenes 
===================================================== */
    @Transactional
    public void claimGuestOrders(Cliente cliente, Store store) {

        String email = cliente.getEmail().trim().toLowerCase();

        List<Order> orders =
                orderRepository.findByCustomerEmailIgnoreCaseAndClienteIsNullAndStore(
                        email,
                        store
                );

        List<Order> toUpdate = new ArrayList<>();

        for (Order order : orders) {
            if (order.canBeClaimed()) {
                order.claim(cliente);
                toUpdate.add(order);
            }
        }

        if (!toUpdate.isEmpty()) {
            orderRepository.saveAll(toUpdate);
        }
    }
/* =====================================================
    CREACIÓN DE ORDEN
 ===================================================== */
    @Transactional
    public Order crearOrden(Order order, Store store) {
        order.setStore(store);
        return orderRepository.save(order);
    }
    
    
 /* =====================================================
guardar orden por transferencia
===================================================== */
 
    @Transactional
    public Order saveOrderTransferencia(Order order, Store store) {

        order.setStore(store);

        Order saved = orderRepository.saveAndFlush(order);

        stockService.descontarStock(saved, store);

        Order fullOrder = orderRepository
                .findByIdFullAndStore(saved.getId(), store)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        notificationService.sendTransferInstructions(fullOrder);

        return fullOrder;
    }
 /* =====================================================
    WEBHOOK STRIPE – PASO 1 (CRÍTICO)
    👉 ESTE NUNCA DEBE FALLAR
 ===================================================== */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarOrdenComoPagada(
            Long orderId,
            String paymentIntentId,
            Store store
    ) {
        Order order = getByIdForUpdate(orderId, store);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "No puedes pagar una orden cancelada"
            );
        }

        if (order.isPaid()) {
            return;
        }

        orderStateMachine.transition(
                order,
                OrderTransition.PAYMENT_CONFIRMED,
                OrderTransitionContext.payment(
                        paymentIntentId
                )
        );
    }
 /* =====================================================
    POST-PAGO – PASO 2 (PUEDE FALLAR)
    👉 STOCK / LOGÍSTICA
 ===================================================== */

    @Transactional
    public void procesarPostPago(
            Long orderId,
            Store store
    ) {
        Order order =
                getFullOrderByIdForUpdate(
                        orderId,
                        store
                );

        if (!order.isPaid()) {
            throw new IllegalStateException(
                    "No puedes procesar una orden no pagada"
            );
        }

        if (order.isStockReduced()) {

            orderStateMachine.transition(
                    order,
                    OrderTransition.STOCK_CONFIRMED,
                    OrderTransitionContext.empty()
            );

            notificationService
                    .sendPaymentConfirmation(order);

            return;
        }

        try {
            stockService.descontarStock(
                    order,
                    store
            );

            orderStateMachine.transition(
                    order,
                    OrderTransition.STOCK_CONFIRMED,
                    OrderTransitionContext.empty()
            );

            notificationService
                    .sendPaymentConfirmation(order);

        } catch (Exception ex) {

            orderStateMachine.transition(
                    order,
                    OrderTransition.STOCK_FAILED,
                    OrderTransitionContext.empty()
            );

            log.error(
                    "Stock falló en orden {}",
                    orderId,
                    ex
            );
        }
    }
 
 public List<Order> findOrdersForExport(
	        OrderStatus status,
	        PaymentStatus payment,
	        LocalDateTime from,
	        LocalDateTime to,
	        Store store
	) {
	    return orderRepository.findFilteredWithCliente(
	            status,
	            payment,
	            from,
	            to,
	            store
	    );
	}
 
 @Transactional
 public void confirmarPagoTransferencia(
         Long orderId,
         Store store
 ) {
     Order order =
             getFullOrderByIdForUpdate(
                     orderId,
                     store
             );

     if (order.isPaid()
             && order.getOrderStatus()
                     == OrderStatus.PROCESSED) {
         return;
     }

     if (!order.isStockReduced()) {
         stockService.descontarStock(
                 order,
                 store
         );
     }

     if (!order.isPaid()) {
         orderStateMachine.transition(
                 order,
                 OrderTransition.PAYMENT_CONFIRMED,
                 OrderTransitionContext.empty()
         );
     }

     orderStateMachine.transition(
             order,
             OrderTransition.STOCK_CONFIRMED,
             OrderTransitionContext.empty()
     );

     notificationService
             .sendPaymentConfirmation(order);
 }
    // ============================
    // ACTUALIZAR ESTADO + STOCK
    // ============================
 @Transactional
 public Order updateOrderStatus(
         Long orderId,
         String newStatus,
         Store store						
 ) {
     Order order =
             getFullOrderByIdForUpdate(
                     orderId,
                     store
             );

     OrderStatus targetStatus;

     try {
         targetStatus =
                 OrderStatus.valueOf(
                         newStatus.trim().toUpperCase()
                 );
     } catch (Exception ex) {
         throw new IllegalArgumentException(
                 "Estado de orden no válido: "
                         + newStatus
         );
     }

     if (targetStatus != OrderStatus.DELIVERED) {
         throw new IllegalStateException(
                 "Este endpoint solo permite marcar "
                         + "órdenes como entregadas"
         );
     }

     orderStateMachine.transition(
             order,
             OrderTransition.DELIVERED,
             OrderTransitionContext.empty()
     );

     return order;
 }
    // ============================
    // ACTUALIZAR INFO DE ENVÍO
    // ============================
 @Transactional
 public Order updateShippingInfo(
         Long orderId,
         String tracking,
         String carrier,
         Store store
 ) {
     Order order =
             getFullOrderByIdForUpdate(
                     orderId,
                     store
             );

     orderStateMachine.transition(
             order,
             OrderTransition.SHIPPED,
             OrderTransitionContext.shipping(
                     tracking,
                     carrier
             )
     );

     notificationService.sendShipping(order);

     return order;
 }
    // ============================
    // ELIMINAR ORDEN
    // ============================
 @Transactional
 public void deleteOrder(
         Long orderId,
         Store store
 ) {
     Order order =
             getFullOrderByIdForUpdate(
                     orderId,
                     store
             );

     if (order.isPaid()) {
         throw new IllegalStateException(
                 "No se puede eliminar una orden pagada"
         );
     }
     
     if (inventoryMovementRepository.existsByOrderId(orderId)) {
    	    throw new IllegalStateException(
    	            "No se puede eliminar una orden con movimientos de inventario"
    	    );
    	}

     if (orderOutboxRepository.existsByOrderId(orderId)) {
         throw new IllegalStateException(
                 "No se puede eliminar la orden porque ya tiene "
                 + "eventos de notificación registrados"
         );
     }

     if (order.isTransferInstructionsSent()
             || order.isPaymentConfirmedSent()
             || order.isShippingConfirmationSent()
             || order.isOrderExpiredSent()) {
         throw new IllegalStateException(
                 "No se puede eliminar una orden con notificaciones enviadas"
         );
     }

     if (order.isStockReduced()) {
         stockService.restaurarStock(
                 order,
                 store
         );
     }

     orderRepository.delete(order);
 }
 
 @Transactional
 public Order cancelOrder(
         Long orderId,
         Store store
 ) {
     Order order =
             getFullOrderByIdForUpdate(
                     orderId,
                     store
             );

     if (order.isPaid()) {
         throw new IllegalStateException(
                 "Una orden pagada no puede cancelarse directamente"
         );
     }

     if (order.getOrderStatus() == OrderStatus.CANCELLED) {
         return order;
     }

     if (order.isStockReduced()) {
         stockService.restaurarStock(
                 order,
                 store
         );
     }

     orderStateMachine.transition(
             order,
             OrderTransition.CANCELLED,
             OrderTransitionContext.empty()
     );

     return order;
 }
 
 public Order getOrderById(Long id, Store store) {
	    return getById(id, store);
	}
 
 
    public Order save(Order order, Store store) {
        order.setStore(store);
        return orderRepository.save(order);
    }
    @Retryable(
    	    value = { PessimisticLockingFailureException.class, CannotAcquireLockException.class },
    	    maxAttempts = 3,
    	    backoff = @Backoff(delay = 200)
    	)    
    @Transactional
    public void validarStockOrden(OrderRequestDTO request, Store store) {
        stockService.validarStock(request.getItems(), store);
    }

    public void validarStockCheckout(CheckoutRequestDTO request, Store store) {
        stockService.validarStock(request.getCart(), store);
    }
/* =====================================================
    EXPIRAR ÓRDENES (TRANSFERENCIAS)
 ===================================================== */
    @Transactional
    public boolean expirarOrdenTransferencia(
            Long orderId,
            Store store
    ) {
        Order order =
                getFullOrderByIdForUpdate(orderId, store);

        /*
         * La orden pudo cambiar desde que el scheduler obtuvo
         * la lista. Volvemos a validar bajo lock.
         */
        if (!order.canExpire()) {
            return false;
        }

        if (order.isStockReduced()) {
            stockService.restaurarStock(order, store);
        }

        orderStateMachine.transition(
                order,
                OrderTransition.EXPIRED,
                OrderTransitionContext.empty()
        );

        LocalDateTime expirationDate =
                order.getOrderDate().plusHours(24);

        /*
         * La entidad está administrada por JPA.
         * No es obligatorio llamar save().
         */
        notificationService.sendExpired(
                order,
                expirationDate
        );

        return true;
    }

    public List<ProductoVentaDTO> getPaidProductSalesByDate(
            LocalDate from,
            LocalDate to,
            Store store
    ) {
        LocalDateTime fromDT = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDT = to != null ? to.atTime(23, 59, 59) : null;

        return orderRepository.getPaidProductSalesByDate(fromDT, toDT, store);
    }
    private Order getByIdForUpdate(
            Long orderId,
            Store store
    ) {
        return orderRepository
                .findByIdForUpdateAndStore(orderId, store)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Orden no encontrada"
                        )
                );
    }

    private Order getFullOrderByIdForUpdate(
            Long orderId,
            Store store
    ) {
        return orderRepository
                .findByIdFullForUpdateAndStore(orderId, store)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Orden no encontrada"
                        )
                );
    }
 // ============================
 // PEDIDOS POR USUARIO (FRONT)
 // ============================
    public List<Order> findByCustomerEmail(String email, Store store) {
        return orderRepository.findByCustomerEmailAndStoreOrderByOrderDateDesc(
                email.trim().toLowerCase(),
                store
        );
    }
 
    public String obtenerUltimaDireccion(Cliente cliente, Store store) {
        return orderRepository
                .findTopByClienteAndStoreOrderByOrderDateDesc(cliente, store)
                .map(Order::getAddress)
                .orElse(null);
    }
 @Transactional
 public ProductoVariante obtenerVarianteConLock(Long id, Store store) {
     return productoVarianteRepository
             .findByIdForUpdate(id, store)
             .orElseThrow(() ->
             new ResourceNotFoundException(
                     "Variante no encontrada: " + id
             )
     );
     }
 public List<Order> filterOrders(
	        LocalDate from,
	        LocalDate to,
	        OrderStatus status,
	        PaymentStatus payment,
	        Store store
	) {

	    LocalDateTime fromDate =
	            from != null ? from.atStartOfDay() : null;

	    LocalDateTime toDate =
	            to != null ? to.atTime(23, 59, 59) : null;

	    return orderRepository.findFilteredWithCliente(
	            status,
	            payment,
	            fromDate,
	            toDate,
	            store
	    );
	}
 public List<Order> findPendingOrders(Store store) {
	    return orderRepository.findPendingOrdersWithItems(store);
	}
}
