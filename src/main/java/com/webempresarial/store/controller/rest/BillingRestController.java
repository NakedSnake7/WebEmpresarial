package com.webempresarial.store.controller.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.webempresarial.store.dto.billing.SaaSCheckoutRequestDTO;
import com.webempresarial.store.dto.billing.SaaSCheckoutResponseDTO;
import com.webempresarial.store.service.StripeCheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/billing")
public class BillingRestController {

    private final StripeCheckoutService stripeCheckoutService;

    public BillingRestController(StripeCheckoutService stripeCheckoutService) {
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<SaaSCheckoutResponseDTO> createCheckout(
            @Valid @RequestBody SaaSCheckoutRequestDTO dto
    ) throws StripeException {

        Session session = stripeCheckoutService.createSaaSCheckoutSession(dto);

        return ResponseEntity.ok(
                new SaaSCheckoutResponseDTO(session.getUrl())
        );
    }
}