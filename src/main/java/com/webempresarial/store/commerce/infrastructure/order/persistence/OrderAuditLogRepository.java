package com.webempresarial.store.commerce.infrastructure.order.persistence;

import java.util.List; 

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderAuditLog;

public interface OrderAuditLogRepository
        extends JpaRepository<OrderAuditLog, Long> {

    List<OrderAuditLog>
    findByOrderIdAndStoreIdOrderByCreatedAtAsc(
            Long orderId,
            Long storeId
    );

    boolean existsByOrderId(Long orderId);
}