package com.webempresarial.store.repository;

import com.webempresarial.store.model.AdminPasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface AdminPasswordResetTokenRepository
        extends JpaRepository<AdminPasswordResetToken, Long> {

    Optional<AdminPasswordResetToken> findByTokenHash(
            String tokenHash
    );

    /*
     * Proyección escalar: no carga el token en el
     * contexto JPA antes del bloqueo exclusivo.
     */
    @Query("""
        SELECT t.adminUser.email
        FROM AdminPasswordResetToken t
        WHERE t.tokenHash = :tokenHash
        """)
    Optional<String> findAdminEmailByTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM AdminPasswordResetToken t
        WHERE t.tokenHash = :tokenHash
        """)
    Optional<AdminPasswordResetToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    List<AdminPasswordResetToken>
    findByAdminUserIdAndConsumedAtIsNull(Long adminUserId);
}
