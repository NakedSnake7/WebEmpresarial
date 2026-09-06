package com.webempresarial.store.commerce.infrastructure.order.persistence;

import java.time.LocalDateTime;

import com.webempresarial.store.commerce.domain.order.OrderAuditAction;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;

import jakarta.persistence.*;

@Entity
@Table(
        name = "order_audit_logs",
        indexes = {
                @Index(
                        name = "idx_order_audit_order",
                        columnList = "order_id,created_at"
                ),
                @Index(
                        name = "idx_order_audit_store",
                        columnList = "store_id,created_at"
                )
        }
)
public class OrderAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_order_status", length = 40)
    private OrderStatus previousOrderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_order_status", length = 40)
    private OrderStatus newOrderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_payment_status", length = 40)
    private PaymentStatus previousPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_payment_status", length = 40)
    private PaymentStatus newPaymentStatus;

    @Column(name = "actor_username", length = 160)
    private String actorUsername;

    @Column(name = "actor_type", length = 40)
    private String actorType;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public OrderAuditAction getAction() {
        return action;
    }

    public void setAction(OrderAuditAction action) {
        this.action = action;
    }

    public OrderStatus getPreviousOrderStatus() {
        return previousOrderStatus;
    }

    public void setPreviousOrderStatus(
            OrderStatus previousOrderStatus
    ) {
        this.previousOrderStatus = previousOrderStatus;
    }

    public OrderStatus getNewOrderStatus() {
        return newOrderStatus;
    }

    public void setNewOrderStatus(
            OrderStatus newOrderStatus
    ) {
        this.newOrderStatus = newOrderStatus;
    }

    public PaymentStatus getPreviousPaymentStatus() {
        return previousPaymentStatus;
    }

    public void setPreviousPaymentStatus(
            PaymentStatus previousPaymentStatus
    ) {
        this.previousPaymentStatus = previousPaymentStatus;
    }

    public PaymentStatus getNewPaymentStatus() {
        return newPaymentStatus;
    }

    public void setNewPaymentStatus(
            PaymentStatus newPaymentStatus
    ) {
        this.newPaymentStatus = newPaymentStatus;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}