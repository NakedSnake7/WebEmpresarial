package com.webempresarial.store.service;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import com.webempresarial.store.dto.order.OrderRequestDTO;
import com.webempresarial.store.dto.producto.reportes.ProductoVentaDTO;
import com.webempresarial.store.exceptions.OrderNotFoundException;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderStatus;
import com.webempresarial.store.model.PaymentStatus;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
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
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(
    	    OrderRepository orderRepository,
    	    ProductoRepository productoRepository,
            ProductoVarianteRepository productoVarianteRepository,
    	    StockService stockService,
    	    NotificationService notificationService
    	) {
    	    this.orderRepository = orderRepository;
    	    this.productoRepository = productoRepository;
    	    this.productoVarianteRepository = productoVarianteRepository;
    	    this.stockService = stockService;
    	    this.notificationService = notificationService;
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

        Order order = getById(orderId, store);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("No puedes pagar una orden cancelada");
        }

        order.markAsPaid(paymentIntentId);
        orderRepository.save(order);
    }
 /* =====================================================
    POST-PAGO – PASO 2 (PUEDE FALLAR)
    👉 STOCK / LOGÍSTICA
 ===================================================== */

 @Transactional
 public void procesarPostPago(Long orderId, Store store) {

     Order order = getOrderByIdWithUserAndItems(orderId, store);

     if (!order.isPaid()) {
         throw new IllegalStateException("No puedes procesar una orden no pagada");
     }

     if (order.isStockReduced()) {
         if (order.getOrderStatus() != OrderStatus.PROCESSED) {
             order.markAsProcessed();
             orderRepository.save(order);
         }
         return;
     }

     try {
         stockService.descontarStock(order, store);

         order.markAsProcessed();
         orderRepository.save(order);

     } catch (Exception e) {

         order.markAsPendingStock();
         orderRepository.save(order);

         log.error("Stock falló en orden {}", orderId, e);
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
 public void confirmarPagoTransferencia(Long orderId, Store store) {

     Order order = getOrderByIdWithUserAndItems(orderId, store);

     if (order.isPaid() && order.getOrderStatus() == OrderStatus.PROCESSED) {
         return;
     }

     order.markAsPaid(null);
     order.markAsProcessed();

     orderRepository.save(order);

     notificationService.sendPaymentConfirmation(order);
 }
    // ============================
    // ACTUALIZAR ESTADO + STOCK
    // ============================
 @Transactional
 public Order updateOrderStatus(Long orderId, String newStatus, Store store) {

     Order order = getOrderByIdWithUserAndItems(orderId, store);

     OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());

     order.changeStatus(status);

     return orderRepository.save(order);
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

     Order order = getById(orderId, store);

     order.markAsShipped(tracking, carrier);

     Order saved = orderRepository.save(order);

     notificationService.sendShipping(saved);

     return saved;
 }
    // ============================
    // ELIMINAR ORDEN
    // ============================
 @Transactional
 public void deleteOrder(Long id, Store store) {

     Order order = getById(id, store);

     orderRepository.delete(order);
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
    public boolean expirarOrdenTransferencia(Order order, Store store) {

        if (!order.canExpire()) return false;

        if (order.isStockReduced()) {
            stockService.restaurarStock(order, store);
        }

        order.markAsExpired();
        orderRepository.save(order);

        notificationService.sendExpired(order, order.getOrderDate().plusHours(24));

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
             .orElseThrow(() -> new RuntimeException("Variante no encontrada"));
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
