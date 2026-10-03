package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.dto.billing.SaaSCheckoutRequestDTO;
import com.webempresarial.store.model.StorePlan;

class StripeSaaSCheckoutServiceTest {

    private StripePlanMapper stripePlanMapper;
    private StripeSaaSCheckoutService service;

    @BeforeEach
    void setUp() {

        stripePlanMapper =
                mock(StripePlanMapper.class);

        service =
                new StripeSaaSCheckoutService(
                        stripePlanMapper
                );

        ReflectionTestUtils.setField(
                service,
                "stripeSecretKey",
                "sk_test_webempresarial"
        );

        ReflectionTestUtils.setField(
                service,
                "environment",
                "dev"
        );
    }

    @Test
    void createSaaSCheckoutSession_shouldBuildExpectedStripeContract()
            throws Exception {

        when(
            stripePlanMapper.getPriceId(
                    StorePlan.PRO
            )
        ).thenReturn(
            "price_pro_test"
        );

        SaaSCheckoutRequestDTO dto =
                new SaaSCheckoutRequestDTO();

        dto.setCompanyName(
                "Acme Store"
        );

        dto.setDomain(
                "  Acme Store  "
        );

        dto.setOwnerName(
                "John Doe"
        );

        dto.setEmail(
                "john@acme.test"
        );

        dto.setPlan(
                StorePlan.PRO
        );

        Session stripeSession =
                mock(Session.class);

        AtomicReference<SessionCreateParams>
                capturedParams =
                new AtomicReference<>();

        AtomicReference<RequestOptions>
                capturedOptions =
                new AtomicReference<>();

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                capturedParams.set(
                        invocation.getArgument(0)
                );

                capturedOptions.set(
                        invocation.getArgument(1)
                );

                return stripeSession;
            });

            Session result =
                    service.createSaaSCheckoutSession(
                            dto
                    );

            assertThat(result)
                    .isSameAs(stripeSession);
        }

        SessionCreateParams params =
                capturedParams.get();

        RequestOptions options =
                capturedOptions.get();

        assertThat(params)
                .isNotNull();

        assertThat(options)
                .isNotNull();

        assertThat(params.getMode())
                .isEqualTo(
                        SessionCreateParams.Mode.SUBSCRIPTION
                );

        assertThat(params.getSuccessUrl())
                .isEqualTo(
                        "http://localhost:8080"
                        + "/billing/success"
                        + "?session_id={CHECKOUT_SESSION_ID}"
                );

        assertThat(params.getCancelUrl())
                .isEqualTo(
                        "http://localhost:8080/#saas-checkout"
                );

        assertThat(params.getCustomerEmail())
                .isEqualTo(
                        "john@acme.test"
                );

        assertThat(params.getClientReferenceId())
                .isEqualTo(
                        "SAAS-acme-store-PRO"
                );

        assertThat(params.getMetadata())
                .containsEntry(
                        "checkout_type",
                        "SAAS_SUBSCRIPTION"
                )
                .containsEntry(
                        "companyName",
                        "Acme Store"
                )
                .containsEntry(
                        "domain",
                        "acme-store"
                )
                .containsEntry(
                        "ownerName",
                        "John Doe"
                )
                .containsEntry(
                        "email",
                        "john@acme.test"
                )
                .containsEntry(
                        "plan",
                        "PRO"
                )
                .containsEntry(
                        "stripe_price_id",
                        "price_pro_test"
                )
                .containsEntry(
                        "env",
                        "dev"
                );

        assertThat(params.getLineItems())
                .hasSize(1);

        SessionCreateParams.LineItem lineItem =
                params.getLineItems()
                        .get(0);

        assertThat(lineItem.getPrice())
                .isEqualTo(
                        "price_pro_test"
                );

        assertThat(lineItem.getQuantity())
                .isEqualTo(1L);

        assertThat(options.getIdempotencyKey())
                .isEqualTo(
                        "saas_acme-store_PRO"
                );
    }

    @Test
    void createSaaSCheckoutSession_shouldUseProductionUrls()
            throws Exception {

        ReflectionTestUtils.setField(
                service,
                "environment",
                "prod"
        );

        when(
            stripePlanMapper.getPriceId(
                    StorePlan.PREMIUM
            )
        ).thenReturn(
            "price_premium_test"
        );

        SaaSCheckoutRequestDTO dto =
                new SaaSCheckoutRequestDTO();

        dto.setCompanyName(
                "Premium Store"
        );

        dto.setDomain(
                "premium"
        );

        dto.setOwnerName(
                "Jane Doe"
        );

        dto.setEmail(
                "jane@premium.test"
        );

        dto.setPlan(
                StorePlan.PREMIUM
        );

        Session stripeSession =
                mock(Session.class);

        AtomicReference<SessionCreateParams>
                capturedParams =
                new AtomicReference<>();

        try (MockedStatic<Session> mocked =
                mockStatic(Session.class)) {

            mocked.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                capturedParams.set(
                        invocation.getArgument(0)
                );

                return stripeSession;
            });

            service.createSaaSCheckoutSession(
                    dto
            );
        }

        assertThat(
                capturedParams.get()
                        .getSuccessUrl()
        ).isEqualTo(
                "https://web-empresarial.com"
                + "/billing/success"
                + "?session_id={CHECKOUT_SESSION_ID}"
        );

        assertThat(
                capturedParams.get()
                        .getCancelUrl()
        ).isEqualTo(
                "https://web-empresarial.com/#saas-checkout"
        );
    }
}