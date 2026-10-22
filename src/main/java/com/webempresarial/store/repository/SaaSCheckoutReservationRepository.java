package com.webempresarial.store.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.SaaSCheckoutReservation;

public interface SaaSCheckoutReservationRepository
        extends JpaRepository<SaaSCheckoutReservation, Long> {

    Optional<SaaSCheckoutReservation> findByAttemptId(
            String attemptId
    );

    Optional<SaaSCheckoutReservation> findByDomain(
            String domain
    );

    Optional<SaaSCheckoutReservation> findByEmail(
            String email
    );

    long deleteByExpiresAtBefore(
            LocalDateTime now
    );
}
