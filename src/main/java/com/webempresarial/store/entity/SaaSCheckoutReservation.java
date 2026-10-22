package com.webempresarial.store.entity;

import java.time.LocalDateTime;

import com.webempresarial.store.model.StorePlan;

import jakarta.persistence.*;

@Entity
@Table(
        name = "saas_checkout_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_saas_checkout_reservation_attempt",
                        columnNames = "attempt_id"
                ),
                @UniqueConstraint(
                        name = "uk_saas_checkout_reservation_domain",
                        columnNames = "domain"
                ),
                @UniqueConstraint(
                        name = "uk_saas_checkout_reservation_email",
                        columnNames = "email"
                )
        }
)
public class SaaSCheckoutReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "attempt_id",
            nullable = false,
            length = 36
    )
    private String attemptId;

    @Column(
            nullable = false,
            length = 255
    )
    private String domain;

    @Column(
            nullable = false,
            length = 255
    )
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StorePlan plan;

    @Column(
            name = "stripe_session_id",
            length = 100
    )
    private String stripeSessionId;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    public Long getId() {
        return id;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StorePlan getPlan() {
        return plan;
    }

    public void setPlan(StorePlan plan) {
        this.plan = plan;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
