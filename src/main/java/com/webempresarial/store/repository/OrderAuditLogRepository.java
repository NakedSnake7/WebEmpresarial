package com.webempresarial.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.OrderAuditLog;

public interface OrderAuditLogRepository
        extends JpaRepository<OrderAuditLog, Long> {

    List<OrderAuditLog>
    findByOrderIdAndStoreIdOrderByCreatedAtAsc(
            Long orderId,
            Long storeId
    );

    boolean existsByOrderId(Long orderId);
}