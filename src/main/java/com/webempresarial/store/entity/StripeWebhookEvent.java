package com.webempresarial.store.entity;

import java.time.LocalDateTime;

import com.webempresarial.store.model.StripeWebhookEventStatus;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stripe_webhook_events",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_stripe_webhook_event_id",
            columnNames = "stripe_event_id"
        )
    }
)
public class StripeWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_event_id", nullable = false, length = 120)
    private String stripeEventId;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StripeWebhookEventStatus status;

    private LocalDateTime receivedAt;

    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }

        if (status == null) {
            status = StripeWebhookEventStatus.PROCESSING;
        }
    }

    public boolean isProcessed() {
        return status == StripeWebhookEventStatus.PROCESSED;
    }

    public boolean isFailed() {
        return status == StripeWebhookEventStatus.FAILED;
    }

    public void markProcessed() {
        this.status = StripeWebhookEventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markFailed(Exception e) {
        this.status = StripeWebhookEventStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = e != null ? e.getMessage() : "Unknown error";
    }

    // getters/setters

    public Long getId() {
        return id;
    }

    public String getStripeEventId() {
        return stripeEventId;
    }

    public void setStripeEventId(String stripeEventId) {
        this.stripeEventId = stripeEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public StripeWebhookEventStatus getStatus() {
        return status;
    }

    public void setStatus(StripeWebhookEventStatus status) {
        this.status = status;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}