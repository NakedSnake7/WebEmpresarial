package com.webempresarial.store.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "admin_password_reset_tokens",
    indexes = {
        @Index(
            name = "idx_admin_password_reset_user_open",
            columnList = "admin_user_id,consumed_at,expires_at"
        )
    }
)
public class AdminPasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "admin_user_id",
        nullable = false
    )
    private AdminUser adminUser;

    @Column(
        name = "token_hash",
        nullable = false,
        unique = true,
        length = 64
    )
    private String tokenHash;

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

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "credential_version", nullable = false)
    private long credentialVersionAtIssue;

    protected AdminPasswordResetToken() {
    }

    public static AdminPasswordResetToken issue(
            AdminUser adminUser,
            String tokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {

        if (adminUser == null
                || tokenHash == null
                || !tokenHash.matches("[0-9a-f]{64}")
                || createdAt == null
                || expiresAt == null
                || !expiresAt.isAfter(createdAt)) {

            throw new IllegalArgumentException(
                "Datos de recuperación inválidos"
            );
        }

        AdminPasswordResetToken token =
                new AdminPasswordResetToken();

        token.adminUser = adminUser;
        token.credentialVersionAtIssue =
                adminUser.getCredentialVersion();
        token.tokenHash = tokenHash;
        token.createdAt = createdAt;
        token.expiresAt = expiresAt;

        return token;
    }

    public boolean isUsableAt(LocalDateTime now) {

        return now != null
                && consumedAt == null
                && !now.isBefore(createdAt)
                && now.isBefore(expiresAt);
    }

    public void consume(LocalDateTime now) {

        if (!isUsableAt(now)) {
            throw new IllegalStateException(
                "Token inválido, expirado o utilizado"
            );
        }

        consumedAt = now;
    }

    public Long getId() {
        return id;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public long getCredentialVersionAtIssue() {
        return credentialVersionAtIssue;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }
}
