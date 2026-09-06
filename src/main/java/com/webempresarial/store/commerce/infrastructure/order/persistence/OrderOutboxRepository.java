package com.webempresarial.store.commerce.infrastructure.order.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxEvent;
import com.webempresarial.store.commerce.domain.order.OutboxStatus;

import jakarta.persistence.LockModeType;

public interface OrderOutboxRepository
        extends JpaRepository<OrderOutboxEvent, Long> {

    @Modifying
    @Query(
            value = """
                INSERT IGNORE INTO order_outbox_events (
                    order_id,
                    store_id,
                    event_type,
                    expiration_date,
                    status,
                    attempts,
                    idempotency_key,
                    created_at,
                    updated_at
                )
                VALUES (
                    :orderId,
                    :storeId,
                    :eventType,
                    :expirationDate,
                    'PENDING',
                    0,
                    :idempotencyKey,
                    NOW(6),
                    NOW(6)
                )
                """,
            nativeQuery = true
    )
    int enqueueIgnoringDuplicate(
            @Param("orderId") Long orderId,
            @Param("storeId") Long storeId,
            @Param("eventType") String eventType,
            @Param("expirationDate") LocalDateTime expirationDate,
            @Param("idempotencyKey") String idempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e
        FROM OrderOutboxEvent e
        WHERE e.status IN (
            com.webempresarial.store.commerce.domain.order.OutboxStatus.PENDING,
            com.webempresarial.store.commerce.domain.order.OutboxStatus.FAILED
        )
        AND (
            e.nextAttemptAt IS NULL
            OR e.nextAttemptAt <= :now
        )
        ORDER BY e.createdAt ASC
    """)
    List<OrderOutboxEvent> findClaimableForUpdate(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e
        FROM OrderOutboxEvent e
        WHERE e.id = :id
    """)
    OrderOutboxEvent findByIdForUpdate(
            @Param("id") Long id
    );

    List<OrderOutboxEvent> findByStatusAndLockedAtBefore(
            OutboxStatus status,
            LocalDateTime lockedBefore
    );
    boolean existsByOrderId(Long orderId);
    
    long countByOrderId(Long orderId);
    
    
}