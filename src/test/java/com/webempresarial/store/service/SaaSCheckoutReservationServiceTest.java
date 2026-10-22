package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import com.webempresarial.store.entity.SaaSCheckoutReservation;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.SaaSCheckoutReservationRepository;

@ExtendWith(MockitoExtension.class)
class SaaSCheckoutReservationServiceTest {

    @Mock
    private SaaSCheckoutReservationRepository
            reservationRepository;

    @Mock
    private ProvisioningService provisioningService;

    private SaaSCheckoutReservationService service;

    @BeforeEach
    void setUp() {

        service =
                new SaaSCheckoutReservationService(
                        reservationRepository,
                        provisioningService
                );
    }

    @Test
    void shouldReserveNormalizedTenantIdentity() {

        when(
                reservationRepository
                        .saveAndFlush(
                                any(SaaSCheckoutReservation.class)
                        )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        SaaSCheckoutReservation reservation =
                service.reserve(
                        " https://Acme.web-empresarial.com/ ",
                        " Owner@ACME.TEST ",
                        StorePlan.PRO
                );

        verify(provisioningService)
                .validateCheckoutEligibility(
                        " https://Acme.web-empresarial.com/ ",
                        " Owner@ACME.TEST "
                );

        assertThat(reservation.getAttemptId())
                .isNotBlank();

        assertThat(reservation.getDomain())
                .isEqualTo(
                        "acme.web-empresarial.com"
                );

        assertThat(reservation.getEmail())
                .isEqualTo(
                        "owner@acme.test"
                );

        assertThat(reservation.getPlan())
                .isEqualTo(StorePlan.PRO);

        assertThat(reservation.getCreatedAt())
                .isNotNull();

        assertThat(reservation.getExpiresAt())
                .isAfter(
                        reservation.getCreatedAt()
                );
    }

    @Test
    void shouldTranslateUniqueConstraintRaceIntoConflict() {

        when(
                reservationRepository
                        .saveAndFlush(
                                any(SaaSCheckoutReservation.class)
                        )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate"
                )
        );

        assertThatThrownBy(() ->
                service.reserve(
                        "acme",
                        "owner@acme.test",
                        StorePlan.BASIC
                )
        )
        .isInstanceOf(
                ResponseStatusException.class
        )
        .satisfies(error -> {

            ResponseStatusException ex =
                    (ResponseStatusException) error;

            assertThat(
                    ex.getStatusCode().value()
            ).isEqualTo(409);
        });
    }

    @Test
    void shouldAttachStripeSession() {

        SaaSCheckoutReservation reservation =
                new SaaSCheckoutReservation();

        reservation.setAttemptId(
                "attempt-123"
        );

        when(
                reservationRepository
                        .findByAttemptId(
                                "attempt-123"
                        )
        ).thenReturn(
                Optional.of(reservation)
        );

        service.attachStripeSession(
                "attempt-123",
                "cs_test_123"
        );

        assertThat(
                reservation.getStripeSessionId()
        ).isEqualTo(
                "cs_test_123"
        );
    }

    @Test
    void shouldReleaseReservation() {

        SaaSCheckoutReservation reservation =
                new SaaSCheckoutReservation();

        reservation.setAttemptId(
                "attempt-123"
        );

        when(
                reservationRepository
                        .findByAttemptId(
                                "attempt-123"
                        )
        ).thenReturn(
                Optional.of(reservation)
        );

        service.release(
                "attempt-123"
        );

        verify(reservationRepository)
                .delete(reservation);
    }
}
