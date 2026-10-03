package com.webempresarial.store.repository;

import com.webempresarial.store.entity.AdminAccountActivationToken;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminAccountActivationTokenRepository
        extends JpaRepository<
                AdminAccountActivationToken,
                Long
        > {

    @EntityGraph(
            attributePaths = "adminUser"
    )
    Optional<AdminAccountActivationToken>
            findByToken(String token);

    Optional<AdminAccountActivationToken>
            findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                    Long adminUserId
            );
}