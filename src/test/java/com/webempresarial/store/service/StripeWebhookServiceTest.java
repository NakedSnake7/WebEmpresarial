package com.webempresarial.store.service;

import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stripe.model.checkout.Session;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StripeWebhookEventRepository;

import com.webempresarial.store.commerce.infrastructure.checkout.payment.StripeCommercePaymentHandler;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

	@Mock
	private StoreRepository storeRepository;
	
    @Mock
    private StripeCommercePaymentHandler stripeCommercePaymentHandler;

    @Mock
    private ProvisioningService provisioningService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private StripeWebhookEventRepository webhookEventRepository;

    @Mock
    private StripePlanMapper stripePlanMapper;

    private StripeWebhookService service;

    @BeforeEach
    void setUp() {
        service = new StripeWebhookService(
                stripeCommercePaymentHandler,
                storeRepository,
                provisioningService,
                subscriptionService,
                webhookEventRepository,
                stripePlanMapper
        );
    }
    
    @Test
    void procesarCheckoutCompleted_shouldDelegateEcommerceCheckout() {

        Session session =
                mock(Session.class);

        Map<String, String> metadata =
                Map.of(
                        "checkout_type",
                        "ECOMMERCE_ORDER",
                        "order_id",
                        "100",
                        "store_id",
                        "5"
                );

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getMetadata())
                .thenReturn(metadata);

        service.procesarCheckoutCompleted(
                session
        );

        verify(stripeCommercePaymentHandler)
                .handlePaidCheckout(
                        session,
                        metadata
                );

        verifyNoInteractions(
                provisioningService,
                subscriptionService
        );
    }
    
  
    @Test
    void procesarCheckoutCompleted_shouldIgnoreUnknownCheckoutType() {

        Session session = mock(Session.class);

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getMetadata())
                .thenReturn(
                        Map.of(
                                "checkout_type",
                                "UNKNOWN_CHECKOUT"
                        )
                );

        service.procesarCheckoutCompleted(session);

        verifyNoInteractions(
                stripeCommercePaymentHandler,
                provisioningService,
                subscriptionService
        );
    }

    @Test
    void procesarCheckoutCompleted_shouldIgnoreUnpaidSession() {

        Session session = mock(Session.class);

        when(session.getPaymentStatus())
                .thenReturn("unpaid");

        service.procesarCheckoutCompleted(session);

        verifyNoInteractions(
                stripeCommercePaymentHandler
        );
    }

    @Test
    void procesarCheckoutCompleted_shouldIgnoreSessionWithoutMetadata() {

        Session session = mock(Session.class);

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getMetadata())
                .thenReturn(null);

        service.procesarCheckoutCompleted(session);

        verifyNoInteractions(
                stripeCommercePaymentHandler
        );
    }


}