package com.webempresarial.store.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.webempresarial.store.service.StripeWebhookService;

class StripeWebhookControllerTest {

    private static final String PLATFORM_SECRET =
            "whsec_platform_test";

    private static final String CONNECT_SECRET =
            "whsec_connect_test";

    private StripeWebhookService stripeWebhookService;

    private StripeWebhookController controller;

    @BeforeEach
    void setUp() {

        stripeWebhookService =
                mock(StripeWebhookService.class);

        controller =
                new StripeWebhookController(
                        stripeWebhookService
                );

        ReflectionTestUtils.setField(
                controller,
                "endpointSecret",
                PLATFORM_SECRET
        );

        ReflectionTestUtils.setField(
                controller,
                "connectEndpointSecret",
                CONNECT_SECRET
        );
    }

    @Test
    void platformWebhookShouldAcceptPlatformSecret()
            throws Exception {

        byte[] payload = payload();

        ResponseEntity<String> response =
                controller.handleWebhook(
                        payload,
                        signature(
                                payload,
                                PLATFORM_SECRET
                        )
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);
    }

    @Test
    void platformWebhookShouldRejectConnectSecret()
            throws Exception {

        byte[] payload = payload();

        ResponseEntity<String> response =
                controller.handleWebhook(
                        payload,
                        signature(
                                payload,
                                CONNECT_SECRET
                        )
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(400);

        verifyNoInteractions(
                stripeWebhookService
        );
    }

    @Test
    void connectWebhookShouldAcceptConnectSecret()
            throws Exception {

        byte[] payload = payload();

        ResponseEntity<String> response =
                controller.handleConnectWebhook(
                        payload,
                        signature(
                                payload,
                                CONNECT_SECRET
                        )
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);
    }

    @Test
    void connectWebhookShouldRejectPlatformSecret()
            throws Exception {

        byte[] payload = payload();

        ResponseEntity<String> response =
                controller.handleConnectWebhook(
                        payload,
                        signature(
                                payload,
                                PLATFORM_SECRET
                        )
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(400);

        verifyNoInteractions(
                stripeWebhookService
        );
    }

    private byte[] payload() {

        return """
                {
                  "id": "evt_test",
                  "object": "event",
                  "type": "test.event",
                  "data": {
                    "object": {}
                  }
                }
                """.getBytes(UTF_8);
    }

    private String signature(
            byte[] payload,
            String secret
    ) throws Exception {

        long timestamp =
                Instant.now().getEpochSecond();

        String signedPayload =
                timestamp
                        + "."
                        + new String(
                                payload,
                                UTF_8
                        );

        Mac mac =
                Mac.getInstance(
                        "HmacSHA256"
                );

        mac.init(
                new SecretKeySpec(
                        secret.getBytes(UTF_8),
                        "HmacSHA256"
                )
        );

        String digest =
                HexFormat.of()
                        .formatHex(
                                mac.doFinal(
                                        signedPayload
                                                .getBytes(
                                                        UTF_8
                                                )
                                )
                        );

        return "t="
                + timestamp
                + ",v1="
                + digest;
    }
}
