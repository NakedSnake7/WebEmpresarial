package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionSchedule;
import com.stripe.model.checkout.Session;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StripeWebhookEventRepository;

import com.webempresarial.store.commerce.infrastructure.checkout.payment.StripeCommercePaymentHandler;
import com.webempresarial.store.entity.StripeWebhookEvent;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.StripeWebhookEventStatus;

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

    @Test
    void procesarCheckoutCompleted_shouldProvisionNewSaaSStore() {

        Session session =
                mock(Session.class);

        Map<String, String> metadata =
                Map.of(
                        "checkout_type",
                        "SAAS_SUBSCRIPTION",
                        "companyName",
                        "Acme Store",
                        "domain",
                        "acme-store",
                        "ownerName",
                        "John Doe",
                        "email",
                        "john@acme.test",
                        "plan",
                        "PRO",
                        "stripe_price_id",
                        "price_pro_test",
                        "env",
                        "dev"
                );

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getMetadata())
                .thenReturn(metadata);

        when(session.getCustomer())
                .thenReturn("cus_acme");

        when(session.getSubscription())
                .thenReturn("sub_acme");

        when(
                stripePlanMapper.matches(
                        StorePlan.PRO,
                        "price_pro_test"
                )
        ).thenReturn(true);

        service.procesarCheckoutCompleted(
                session
        );

        verify(stripePlanMapper)
                .matches(
                        StorePlan.PRO,
                        "price_pro_test"
                );

        verify(provisioningService)
                .provisionStoreFromCheckout(
                        "Acme Store",
                        "acme-store",
                        "John Doe",
                        "john@acme.test",
                        StorePlan.PRO,
                        "cus_acme",
                        "sub_acme",
                        "price_pro_test"
                );

        verifyNoInteractions(
                stripeCommercePaymentHandler,
                subscriptionService
        );
    }
    @Test
    void procesarCheckoutCompleted_shouldRejectSaaSCheckoutWhenPriceDoesNotMatchPlan() {

        Session session =
                mock(Session.class);

        Map<String, String> metadata =
                Map.of(
                        "checkout_type",
                        "SAAS_SUBSCRIPTION",
                        "companyName",
                        "Acme Store",
                        "domain",
                        "acme-store",
                        "ownerName",
                        "John Doe",
                        "email",
                        "john@acme.test",
                        "plan",
                        "PRO",
                        "stripe_price_id",
                        "price_invalid"
                );

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getMetadata())
                .thenReturn(metadata);

        when(
                stripePlanMapper.matches(
                        StorePlan.PRO,
                        "price_invalid"
                )
        ).thenReturn(false);

        assertThatThrownBy(() ->
                service.procesarCheckoutCompleted(
                        session
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Stripe priceId no corresponde al plan recibido"
                );

        verifyNoInteractions(
                provisioningService,
                stripeCommercePaymentHandler,
                subscriptionService
        );
    }

    private void stubUnprocessedEvent(
            Event event,
            String eventId,
            String eventType
    ) {
        when(event.getId())
                .thenReturn(eventId);

        when(event.getType())
                .thenReturn(eventType);

        when(webhookEventRepository
                .findByStripeEventId(eventId))
                .thenReturn(Optional.empty());

        when(webhookEventRepository
                .save(any(StripeWebhookEvent.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "customer.subscription.created",
            "customer.subscription.updated"
    })
    void handle_shouldSyncCreatedAndUpdatedSubscription(
            String eventType
    ) {
        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        Subscription stripeSubscription =
                mock(Subscription.class);

        stubUnprocessedEvent(
                event,
                "evt_" + eventType.replace(".", "_"),
                eventType
        );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.of(stripeSubscription));

        service.handle(event);

        verify(subscriptionService)
                .syncStripeSubscriptionUpdated(
                        stripeSubscription
                );
    }

    @Test
    void handle_shouldCancelDeletedSubscription() {
        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        Subscription stripeSubscription =
                mock(Subscription.class);

        stubUnprocessedEvent(
                event,
                "evt_subscription_deleted",
                "customer.subscription.deleted"
        );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.of(stripeSubscription));

        when(stripeSubscription.getId())
                .thenReturn("sub_deleted_123");

        service.handle(event);

        verify(subscriptionService)
                .cancelByStripeSubscriptionId(
                        "sub_deleted_123"
                );
    }

    @Test
    void handle_shouldRegisterSuccessfulInvoicePayment() {
        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        Invoice invoice =
                mock(Invoice.class);

        stubUnprocessedEvent(
                event,
                "evt_invoice_paid",
                "invoice.paid"
        );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.of(invoice));

        when(invoice.getSubscription())
                .thenReturn("sub_paid_123");

        service.handle(event);

        verify(subscriptionService)
                .registerSuccessfulPayment(
                        "sub_paid_123"
                );
    }

    @Test
    void handle_shouldMarkSubscriptionPastDueWhenInvoiceFails() {
        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        Invoice invoice =
                mock(Invoice.class);

        stubUnprocessedEvent(
                event,
                "evt_invoice_failed",
                "invoice.payment_failed"
        );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.of(invoice));

        when(invoice.getSubscription())
                .thenReturn("sub_failed_123");

        service.handle(event);

        verify(subscriptionService)
                .markPastDue(
                        "sub_failed_123"
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "subscription_schedule.created",
            "subscription_schedule.updated"
    })
    void handle_shouldOnlyAuditCreatedAndUpdatedSchedule(
            String eventType
    ) {
        Event event = mock(Event.class);

        stubUnprocessedEvent(
                event,
                "evt_" + eventType.replace(".", "_"),
                eventType
        );

        service.handle(event);

        verifyNoInteractions(
                subscriptionService,
                provisioningService,
                stripeCommercePaymentHandler,
                stripePlanMapper
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "subscription_schedule.completed",
            "subscription_schedule.released"
    })
    void handle_shouldReconcileFinishedSchedule(
            String eventType
    ) {
        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        SubscriptionSchedule schedule =
                mock(SubscriptionSchedule.class);

        stubUnprocessedEvent(
                event,
                "evt_" + eventType.replace(".", "_"),
                eventType
        );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.of(schedule));

        when(schedule.getSubscription())
                .thenReturn("sub_schedule_finished");

        service.handle(event);

        verify(subscriptionService)
                .reconcileStripeSubscription(
                        "sub_schedule_finished"
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "subscription_schedule.canceled",
            "subscription_schedule.aborted"
    })
    void handle_shouldClearPendingPlanForCanceledSchedule(
            String eventType
    ) {
        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        SubscriptionSchedule schedule =
                mock(SubscriptionSchedule.class);

        stubUnprocessedEvent(
                event,
                "evt_" + eventType.replace(".", "_"),
                eventType
        );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.of(schedule));

        when(schedule.getSubscription())
                .thenReturn("sub_schedule_canceled");

        service.handle(event);

        verify(subscriptionService)
                .clearPendingPlanByStripeSubscriptionId(
                        "sub_schedule_canceled"
                );
    }


    @Test
    void handle_shouldIgnoreAlreadyProcessedStripeEvent() {

        Event event =
                mock(Event.class);

        StripeWebhookEvent existing =
                new StripeWebhookEvent();

        existing.setStripeEventId(
                "evt_saas_123"
        );

        existing.setEventType(
                "checkout.session.completed"
        );

        existing.setStatus(
                StripeWebhookEventStatus.PROCESSED
        );

        when(event.getId())
                .thenReturn("evt_saas_123");

        when(
                webhookEventRepository
                        .findByStripeEventId(
                                "evt_saas_123"
                        )
        ).thenReturn(
                Optional.of(existing)
        );

        service.handle(event);

        verify(
                webhookEventRepository
        ).findByStripeEventId(
                "evt_saas_123"
        );

        verify(
                webhookEventRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                provisioningService,
                subscriptionService,
                stripeCommercePaymentHandler,
                stripePlanMapper
        );
    }


    @Test
    void handle_shouldRegisterAndMarkUnknownEventAsProcessed() {

        Event event =
                mock(Event.class);

        when(event.getId())
                .thenReturn(
                        "evt_unknown_123"
                );

        when(event.getType())
                .thenReturn(
                        "some.unknown.event"
                );

        when(
                webhookEventRepository
                        .findByStripeEventId(
                                "evt_unknown_123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                webhookEventRepository.save(
                        any(StripeWebhookEvent.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        service.handle(event);

        ArgumentCaptor<StripeWebhookEvent> captor =
                ArgumentCaptor.forClass(
                        StripeWebhookEvent.class
                );

        verify(
                webhookEventRepository
        ).save(
                captor.capture()
        );

        StripeWebhookEvent saved =
                captor.getValue();

        assertThat(
                saved.getStripeEventId()
        ).isEqualTo(
                "evt_unknown_123"
        );

        assertThat(
                saved.getEventType()
        ).isEqualTo(
                "some.unknown.event"
        );

        assertThat(
                saved.getStatus()
        ).isEqualTo(
                StripeWebhookEventStatus.PROCESSED
        );

        assertThat(
                saved.getProcessedAt()
        ).isNotNull();

        verifyNoInteractions(
                provisioningService,
                subscriptionService,
                stripeCommercePaymentHandler,
                stripePlanMapper
        );
    }


    @Test
    void handle_shouldMarkWebhookEventFailedWhenProcessingThrows() {

        Event event =
                mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(event.getId())
                .thenReturn(
                        "evt_subscription_failure"
                );

        when(event.getType())
                .thenReturn(
                        "customer.subscription.deleted"
                );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(Optional.empty());

        when(deserializer.getRawJson())
                .thenReturn(null);

        when(
                webhookEventRepository
                        .findByStripeEventId(
                                "evt_subscription_failure"
                        )
        ).thenReturn(
                Optional.empty()
        );

        AtomicReference<StripeWebhookEvent>
                savedEvent =
                new AtomicReference<>();

        when(
                webhookEventRepository.save(
                        any(StripeWebhookEvent.class)
                )
        ).thenAnswer(invocation -> {

            StripeWebhookEvent value =
                    invocation.getArgument(0);

            savedEvent.set(value);

            return value;
        });

        assertThatThrownBy(() ->
                service.handle(event)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "No se pudo deserializar customer.subscription.deleted"
                );

        assertThat(savedEvent.get())
                .isNotNull();

        assertThat(
                savedEvent.get().getStatus()
        ).isEqualTo(
                StripeWebhookEventStatus.FAILED
        );

        assertThat(
                savedEvent.get().getProcessedAt()
        ).isNotNull();

        assertThat(
                savedEvent.get().getErrorMessage()
        ).isEqualTo(
                "No se pudo deserializar customer.subscription.deleted"
        );
    }
}