package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.webempresarial.store.entity.SaaSCheckoutReservation;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.SaaSCheckoutReservationRepository;

@Service
public class SaaSCheckoutReservationService {

    /*
     * Debe mantenerse alineado posteriormente con expires_at
     * de la Stripe Checkout Session.
     */
    static final long RESERVATION_HOURS = 24L;

    private final SaaSCheckoutReservationRepository
            reservationRepository;

    private final ProvisioningService provisioningService;

    public SaaSCheckoutReservationService(
            SaaSCheckoutReservationRepository reservationRepository,
            ProvisioningService provisioningService
    ) {
        this.reservationRepository =
                reservationRepository;

        this.provisioningService =
                provisioningService;
    }

    @Transactional
    public SaaSCheckoutReservation reserve(
            String domain,
            String email,
            StorePlan plan
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * Las reservas expiradas no deben bloquear
         * una adquisición futura.
         */
        reservationRepository
                .deleteByExpiresAtBefore(now);

        /*
         * Primera barrera:
         * no reservamos una identidad que ya pertenece
         * a un tenant provisionado.
         */
        provisioningService
                .validateCheckoutEligibility(
                        domain,
                        email
                );

        String normalizedDomain =
                normalizeDomain(domain)
                        + ".web-empresarial.com";

        String normalizedEmail =
                normalizeEmail(email);

        SaaSCheckoutReservation reservation =
                new SaaSCheckoutReservation();

        reservation.setAttemptId(
                UUID.randomUUID().toString()
        );

        reservation.setDomain(
                normalizedDomain
        );

        reservation.setEmail(
                normalizedEmail
        );

        reservation.setPlan(plan);
        reservation.setCreatedAt(now);

        reservation.setExpiresAt(
                now.plusHours(
                        RESERVATION_HOURS
                )
        );

        try {
            /*
             * saveAndFlush es intencional.
             *
             * Necesitamos que las UNIQUE constraints se
             * evalúen antes de permitir cualquier llamada
             * posterior a Stripe.
             */
            return reservationRepository
                    .saveAndFlush(reservation);

        } catch (DataIntegrityViolationException ex) {

            /*
             * Segunda barrera:
             * dos requests concurrentes pudieron observar
             * la identidad libre, pero sólo uno puede ganar
             * la UNIQUE constraint.
             */
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un checkout activo para este dominio o correo",
                    ex
            );
        }
    }

    @Transactional
    public void attachStripeSession(
            String attemptId,
            String stripeSessionId
    ) {

        SaaSCheckoutReservation reservation =
                reservationRepository
                        .findByAttemptId(attemptId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Reserva SaaS no encontrada: "
                                                + attemptId
                                )
                        );

        reservation.setStripeSessionId(
                stripeSessionId
        );
    }

    @Transactional
    public void release(
            String attemptId
    ) {

        reservationRepository
                .findByAttemptId(attemptId)
                .ifPresent(
                        reservationRepository::delete
                );
    }

    private String normalizeDomain(
            String domain
    ) {

        if (domain == null
                || domain.isBlank()) {

            throw new IllegalArgumentException(
                    "El dominio no puede estar vacío"
            );
        }

        return domain
                .trim()
                .toLowerCase()
                .replace("https://", "")
                .replace("http://", "")
                .replace(".web-empresarial.com", "")
                .replace("/", "")
                .replace(" ", "-");
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            throw new IllegalArgumentException(
                    "El correo no puede estar vacío"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }
}
