package com.webempresarial.store.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.webempresarial.store.service.StripeWebhookService;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(
            StripeWebhookService stripeWebhookService
    ) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody byte[] payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        Event event;

        try {
            String payloadString =
                    new String(payload, StandardCharsets.UTF_8);

            event = Webhook.constructEvent(
                    payloadString,
                    sigHeader,
                    endpointSecret
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Invalid signature");
        }

        try {
            stripeWebhookService.handle(event);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Webhook processing error");
        }
    }
}