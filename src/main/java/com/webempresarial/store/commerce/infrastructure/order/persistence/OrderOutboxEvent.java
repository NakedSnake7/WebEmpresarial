package com.webempresarial.store.commerce.infrastructure.order.persistence;

import java.time.LocalDateTime; 

import com.webempresarial.store.commerce.domain.order.OrderNotificationType;
import com.webempresarial.store.commerce.domain.order.OutboxStatus;

import jakarta.persistence.*;

@Entity
@Table(
        name = "order_outbox_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_outbox_idempotency",
                        columnNames = "idempotency_key"
                )
        },
        indexes = {
                @Index(
                        name = "idx_order_outbox_pending",
                        columnList = "status,next_attempt_at,created_at"
                ),
                @Index(
                        name = "idx_order_outbox_order",
                        columnList = "order_id"
                )
        }
)
public class OrderOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private OrderNotificationType eventType;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "idempotency_key", nullable = false, length = 150)
    private String idempotencyKey;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = OutboxStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        status = OutboxStatus.PROCESSING;
        lockedAt = LocalDateTime.now();
        attempts++;
        lastError = null;
    }

    public void markProcessed() {
        status = OutboxStatus.PROCESSED;
        processedAt = LocalDateTime.now();
        lockedAt = null;
        nextAttemptAt = null;
        lastError = null;
    }

    public void markFailed(
            String error,
            LocalDateTime retryAt
    ) {
        status = OutboxStatus.FAILED;
        lastError = truncate(error, 4000);
        nextAttemptAt = retryAt;
        lockedAt = null;
    }

    public void releaseStaleLock() {
        status = OutboxStatus.FAILED;
        lockedAt = null;
        nextAttemptAt = LocalDateTime.now();
        lastError = "Evento PROCESSING recuperado por lock expirado";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public OrderNotificationType getEventType() {
        return eventType;
    }

    public void setEventType(OrderNotificationType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getLastError() {
        return lastError;
    }
}