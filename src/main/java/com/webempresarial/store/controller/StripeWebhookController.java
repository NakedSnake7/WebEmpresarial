package com.webempresarial.store.controller;

import org.springframework.beans.factory.annotation.Value;     
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.webempresarial.store.service.StripeWebhookService;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody byte[] payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
        	event = Webhook.constructEvent(
        	        new String(payload, java.nio.charset.StandardCharsets.UTF_8),
        	        sigHeader,
        	        endpointSecret
        	);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        try {
        	if ("checkout.session.completed".equals(event.getType())) {

        	    var deserializer = event.getDataObjectDeserializer();
        	    var optionalObject = deserializer.getObject();

        	    if (optionalObject.isPresent()) {
        	        Object obj = optionalObject.get();

        	        if (obj instanceof Session session) {
        	            stripeWebhookService.procesarCheckoutCompleted(session);
        	        }
        	    } else {
        	        // fallback por si Stripe no puede deserializar automáticamente
        	        Session session = Session.GSON.fromJson(
        	                event.getDataObjectDeserializer().getRawJson(),
        	                Session.class
        	        );
        	        stripeWebhookService.procesarCheckoutCompleted(session);
        	    }
        	}

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 necesario mientras estabilizamos
            return ResponseEntity.status(500).body("Webhook processing error");
        }

        return ResponseEntity.ok("OK");
    }
}
