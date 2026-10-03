package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.stripe.model.Event;
import com.webempresarial.store.service.StripeWebhookService;

@ExtendWith(MockitoExtension.class)
class StripeWebhookControllerTest {

    private static final String WEBHOOK_SECRET =
            "whsec_test_webempresarial";

    @Mock
    private StripeWebhookService stripeWebhookService;

    private StripeWebhookController controller;

    @BeforeEach
    void setUp() {

        controller =
                new StripeWebhookController(
                        stripeWebhookService
                );

        ReflectionTestUtils.setField(
                controller,
                "endpointSecret",
                WEBHOOK_SECRET
        );
    }

    @Test
    void handleWebhook_shouldAcceptValidStripeSignature()
            throws Exception {

        String payload = """
                {
                  "id": "evt_saas_123",
                  "object": "event",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_123",
                      "object": "checkout.session",
                      "payment_status": "paid"
                    }
                  }
                }
                """;

        String signature =
                stripeSignature(
                        payload,
                        WEBHOOK_SECRET
                );

        ResponseEntity<String> response =
                controller.handleWebhook(
                        payload.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        signature
                );

        assertThat(
                response.getStatusCode().value()
        ).isEqualTo(200);

        assertThat(
                response.getBody()
        ).isEqualTo("OK");

        ArgumentCaptor<Event> eventCaptor =
                ArgumentCaptor.forClass(
                        Event.class
                );

        verify(stripeWebhookService)
                .handle(
                        eventCaptor.capture()
                );

        Event event =
                eventCaptor.getValue();

        assertThat(event.getId())
                .isEqualTo(
                        "evt_saas_123"
                );

        assertThat(event.getType())
                .isEqualTo(
                        "checkout.session.completed"
                );
    }

    @Test
    void handleWebhook_shouldRejectInvalidStripeSignature() {

        String payload = """
                {
                  "id": "evt_invalid",
                  "object": "event",
                  "type": "checkout.session.completed"
                }
                """;

        ResponseEntity<String> response =
                controller.handleWebhook(
                        payload.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "t=123,v1=invalid"
                );

        assertThat(
                response.getStatusCode().value()
        ).isEqualTo(400);

        assertThat(
                response.getBody()
        ).isEqualTo(
                "Invalid signature"
        );

        verify(
                stripeWebhookService,
                never()
        ).handle(any());
    }

    @Test
    void handleWebhook_shouldReturn500WhenProcessingFails()
            throws Exception {

        String payload = """
                {
                  "id": "evt_failure_123",
                  "object": "event",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_failure_123",
                      "object": "checkout.session",
                      "payment_status": "paid"
                    }
                  }
                }
                """;

        String signature =
                stripeSignature(
                        payload,
                        WEBHOOK_SECRET
                );

        doThrow(
                new IllegalStateException(
                        "Provisioning failed"
                )
        )
                .when(stripeWebhookService)
                .handle(any(Event.class));

        ResponseEntity<String> response =
                controller.handleWebhook(
                        payload.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        signature
                );

        assertThat(
                response.getStatusCode().value()
        ).isEqualTo(500);

        assertThat(
                response.getBody()
        ).isEqualTo(
                "Webhook processing error"
        );

        verify(stripeWebhookService)
                .handle(any(Event.class));
    }

    private String stripeSignature(
            String payload,
            String secret
    ) throws Exception {

        long timestamp =
                Instant.now()
                        .getEpochSecond();

        String signedPayload =
                timestamp
                        + "."
                        + payload;

        Mac mac =
                Mac.getInstance(
                        "HmacSHA256"
                );

        mac.init(
                new SecretKeySpec(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                )
        );

        byte[] hash =
                mac.doFinal(
                        signedPayload.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder hex =
                new StringBuilder();

        for (byte b : hash) {
            hex.append(
                    String.format(
                            "%02x",
                            b & 0xff
                    )
            );
        }

        return "t="
                + timestamp
                + ",v1="
                + hex;
    }
}