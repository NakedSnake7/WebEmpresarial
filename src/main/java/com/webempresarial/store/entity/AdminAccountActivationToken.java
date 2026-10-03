package com.webempresarial.store.entity;

import com.webempresarial.store.model.AdminUser;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_account_activation_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_admin_account_activation_token",
                        columnNames = "token"
                )
        }
)
public class AdminAccountActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "admin_user_id",
            nullable = false
    )
    private AdminUser adminUser;

    @Column(nullable = false, unique = true)
    private String token;

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

    @Column(nullable = false)
    private boolean used;

    public boolean isExpired() {
        return expiresAt != null
                && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsable() {
        return !used && !isExpired();
    }

    public void markUsed() {
        this.used = true;
    }

    public Long getId() {
        return id;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}