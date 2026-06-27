package com.webempresarial.store.repository;

import com.webempresarial.store.dto.producto.reportes.ProductoVentaDTO;
import com.webempresarial.store.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndStore(Long id, Store store);

    Optional<Order> findByStripeSessionIdAndStore(
            String stripeSessionId,
            Store store
    );

    @Query("""
    	    SELECT o 
    	    FROM Order o 
    	    LEFT JOIN FETCH o.cliente 
    	    WHERE o.id = :id
    	    AND o.store = :store
    	""")
    Optional<Order> findByIdWithClienteAndStore(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN FETCH o.cliente
        JOIN FETCH o.items i
        JOIN FETCH i.producto
        LEFT JOIN FETCH i.variante
        WHERE o.id = :id
        AND o.store = :store
    """)
    Optional<Order> findByIdWithClienteAndItemsAndStore(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Query("""
    	    SELECT o
    	    FROM Order o
    	    LEFT JOIN FETCH o.cliente
    	    WHERE o.store = :store
    	""")
    List<Order> findAllWithCliente(
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.producto
        LEFT JOIN FETCH i.variante
        WHERE o.orderStatus = com.webempresarial.store.model.OrderStatus.CREATED
        AND o.paymentMethod = com.webempresarial.store.model.Order.PaymentMethod.TRANSFER
        AND o.store = :store
    """)
    List<Order> findPendingOrdersWithItems(
            @Param("store") Store store
    );

    @Query("""
    	    SELECT DISTINCT o
    	    FROM Order o
    	    LEFT JOIN FETCH o.cliente
    	    WHERE (:status IS NULL OR o.orderStatus = :status)
    	    AND (:payment IS NULL OR o.paymentStatus = :payment)
    	    AND (:from IS NULL OR o.orderDate >= :from)
    	    AND (:to IS NULL OR o.orderDate <= :to)
    	    AND o.store = :store
    	""")
    List<Order> findFilteredWithCliente(
            @Param("status") OrderStatus status,
            @Param("payment") PaymentStatus payment,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("store") Store store
    );

    @Query("""
        SELECT new com.webempresarial.store.dto.producto.reportes.ProductoVentaDTO(
            p.productName,
            SUM(oi.quantity)
        )
        FROM OrderItem oi
        JOIN oi.order o
        JOIN oi.producto p
        WHERE o.paymentStatus = com.webempresarial.store.model.PaymentStatus.PAID
        AND o.store = :store
        GROUP BY p.productName
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<ProductoVentaDTO> getPaidProductSales(
            @Param("store") Store store
    );

    @Query("""
        SELECT new com.webempresarial.store.dto.producto.reportes.ProductoVentaDTO(
            p.productName,
            SUM(i.quantity)
        )
        FROM Order o
        JOIN o.items i
        JOIN i.producto p
        WHERE o.paymentStatus = com.webempresarial.store.model.PaymentStatus.PAID
        AND (:from IS NULL OR o.paidAt >= :from)
        AND (:to IS NULL OR o.paidAt <= :to)
        AND o.store = :store
        GROUP BY p.productName
        ORDER BY SUM(i.quantity) DESC
    """)
    List<ProductoVentaDTO> getPaidProductSalesByDate(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("store") Store store
    );

    List<Order> findByCustomerEmailAndStoreOrderByOrderDateDesc(
            String customerEmail,
            Store store
    );

    Optional<Order> findTopByClienteAndStoreOrderByOrderDateDesc(
            Cliente cliente,
            Store store
    );

    List<Order> findByCustomerEmailIgnoreCaseAndClienteIsNullAndStore(
            String email,
            Store store
    );

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.producto
        LEFT JOIN FETCH i.variante
        WHERE o.id = :id
        AND o.store = :store
    """)
    Optional<Order> findByIdFullAndStore(
            @Param("id") Long id,
            @Param("store") Store store
    );
    
    long countByStoreId(Long storeId);
    
}