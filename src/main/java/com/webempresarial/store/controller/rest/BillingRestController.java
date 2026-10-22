package com.webempresarial.store.controller.rest;

import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.webempresarial.store.dto.billing.SaaSCheckoutRequestDTO;
import com.webempresarial.store.dto.billing.SaaSCheckoutResponseDTO;
import com.webempresarial.store.entity.SaaSCheckoutReservation;
import com.webempresarial.store.service.SaaSCheckoutReservationService;
import com.webempresarial.store.service.StripeSaaSCheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/billing")
public class BillingRestController {

	private final StripeSaaSCheckoutService stripeSaaSCheckoutService;
	private final SaaSCheckoutReservationService reservationService;

	public BillingRestController(
            StripeSaaSCheckoutService stripeSaaSCheckoutService,
            SaaSCheckoutReservationService reservationService
    ) {
        this.stripeSaaSCheckoutService =
                stripeSaaSCheckoutService;

        this.reservationService =
                reservationService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<SaaSCheckoutResponseDTO> createCheckout(
            @Valid @RequestBody SaaSCheckoutRequestDTO dto
    ) throws StripeException {
        SaaSCheckoutReservation reservation =
                reservationService.reserve(
                        dto.getDomain(),
                        dto.getEmail(),
                        dto.getPlan()
                );

        Session session;

        try {
            session =
                    stripeSaaSCheckoutService
                            .createSaaSCheckoutSession(
                                    dto,
                                    reservation.getAttemptId(),
                                    reservation.getExpiresAt()
                            );

        } catch (StripeException | RuntimeException ex) {

            /*
             * El usuario todavía no recibió una URL de Stripe.
             * La identidad puede volver a quedar disponible.
             */
            reservationService.release(
                    reservation.getAttemptId()
            );

            throw ex;
        }

        /*
         * A partir de aquí Stripe ya creó una sesión válida.
         * Si falla esta escritura, conservamos la reserva:
         * no debemos permitir otro checkout concurrente.
         */
        reservationService.attachStripeSession(
                reservation.getAttemptId(),
                session.getId()
        );
    	
        return ResponseEntity.ok(
                new SaaSCheckoutResponseDTO(session.getUrl())
        );
    }
}