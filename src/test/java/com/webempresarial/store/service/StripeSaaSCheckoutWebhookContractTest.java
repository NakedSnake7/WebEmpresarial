package com.webempresarial.store.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;

import com.webempresarial.store.commerce.infrastructure.checkout.payment.StripeCommercePaymentHandler;
import com.webempresarial.store.dto.billing.SaaSCheckoutRequestDTO;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StripeWebhookEventRepository;

@ExtendWith(MockitoExtension.class)
class StripeSaaSCheckoutWebhookContractTest {

    @Mock
    private StripePlanMapper stripePlanMapper;

    @Mock
    private ProvisioningService provisioningService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StripeWebhookEventRepository webhookEventRepository;

    @Mock
    private StripeCommercePaymentHandler stripeCommercePaymentHandler;

    private StripeSaaSCheckoutService checkoutService;

    private StripeWebhookService webhookService;

    @BeforeEach
    void setUp() {

        checkoutService =
                new StripeSaaSCheckoutService(
                        stripePlanMapper
                );

        ReflectionTestUtils.setField(
                checkoutService,
                "stripeSecretKey",
                "sk_test_webempresarial"
        );

        ReflectionTestUtils.setField(
                checkoutService,
                "environment",
                "dev"
        );

        webhookService =
                new StripeWebhookService(
                        stripeCommercePaymentHandler,
                        storeRepository,
                        provisioningService,
                        subscriptionService,
                        webhookEventRepository,
                        stripePlanMapper
                );
    }

    @Test
    void checkoutMetadataShouldBeConsumableBySaaSWebhook()
            throws Exception {

        when(
                stripePlanMapper.getPriceId(
                        StorePlan.PRO
                )
        ).thenReturn(
                "price_pro_test"
        );

        when(
                stripePlanMapper.matches(
                        StorePlan.PRO,
                        "price_pro_test"
                )
        ).thenReturn(true);

        SaaSCheckoutRequestDTO dto =
                new SaaSCheckoutRequestDTO();

        dto.setCompanyName(
                "Contract Store"
        );

        dto.setDomain(
                "  Contract Store  "
        );

        dto.setOwnerName(
                "Contract Owner"
        );

        dto.setEmail(
                "owner@contract.test"
        );

        dto.setPlan(
                StorePlan.PRO
        );

        Session checkoutSession =
                mock(Session.class);

        AtomicReference<SessionCreateParams>
                capturedParams =
                new AtomicReference<>();

        try (MockedStatic<Session> mockedSession =
                mockStatic(Session.class)) {

            mockedSession.when(() ->
                    Session.create(
                            any(SessionCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenAnswer(invocation -> {

                capturedParams.set(
                        invocation.getArgument(0)
                );

                return checkoutSession;
            });

            checkoutService
                    .createSaaSCheckoutSession(dto);
        }

        SessionCreateParams stripeParams =
                capturedParams.get();

        if (stripeParams == null) {
            throw new AssertionError(
                    "Stripe checkout params no fueron capturados"
            );
        }

        /*
         * No reconstruimos manualmente la metadata.
         *
         * El webhook recibe exactamente el Map producido por
         * StripeSaaSCheckoutService. Esa es la frontera
         * contractual que queremos proteger.
         */
        Session webhookSession =
                mock(Session.class);

        when(webhookSession.getPaymentStatus())
                .thenReturn("paid");

        when(webhookSession.getMetadata())
                .thenReturn(
                        stripeParams.getMetadata()
                );

        when(webhookSession.getCustomer())
                .thenReturn(
                        "cus_contract_123"
                );

        when(webhookSession.getSubscription())
                .thenReturn(
                        "sub_contract_123"
                );

        webhookService
                .procesarCheckoutCompleted(
                        webhookSession
                );

        verify(provisioningService)
                .provisionStoreFromCheckout(
                        "Contract Store",
                        "contract-store",
                        "Contract Owner",
                        "owner@contract.test",
                        StorePlan.PRO,
                        "cus_contract_123",
                        "sub_contract_123",
                        "price_pro_test"
                );

        verify(stripePlanMapper)
                .matches(
                        StorePlan.PRO,
                        "price_pro_test"
                );

        verifyNoInteractions(
                stripeCommercePaymentHandler,
                subscriptionService,
                storeRepository,
                webhookEventRepository
        );
    }
}
