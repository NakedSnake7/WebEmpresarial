package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.webempresarial.store.entity.StripeWebhookEvent;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.StripeWebhookEventStatus;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StripeWebhookEventRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

@SpringBootTest
@ActiveProfiles("dev")
class StripeSubscriptionEventOrderingIntegrationTest {

    private static final String MISSING_EVENT_ID =
            "evt_gl_g04_missing_subscription";

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private StripeWebhookService stripeWebhookService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StripeWebhookEventRepository webhookEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanupWebhookAudit() {
        webhookEventRepository
                .findByStripeEventId(MISSING_EVENT_ID)
                .ifPresent(webhookEventRepository::delete);

        webhookEventRepository.flush();
    }

    @Test
    void olderInvoicePaidMustNotReactivateDeletedSubscription() {

        TestFixture fixture =
                createSubscription("deleted-then-old-paid");

        try {
            subscriptionService
                    .cancelByStripeSubscriptionIdFromWebhook(
                            fixture.stripeSubscriptionId(),
                            200L,
                            "customer.subscription.deleted"
                    );

            subscriptionService
                    .registerSuccessfulPaymentFromStripeWebhook(
                            fixture.stripeSubscriptionId(),
                            100L,
                            "invoice.paid"
                    );

            Subscription persisted =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            Store persistedStore =
                    storeRepository
                            .findById(fixture.storeId())
                            .orElseThrow();

            assertThat(persisted.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.CANCELLED
                    );

            assertThat(persistedStore.isActiva())
                    .isFalse();

            assertThat(
                    persisted.getLastStripeEventCreated()
            ).isEqualTo(200L);

            assertThat(
                    persisted.getLastStripeEventPriority()
            ).isEqualTo(400);

            assertThat(
                    persisted.getLastStripeEventType()
            ).isEqualTo(
                    "customer.subscription.deleted"
            );

        } finally {
            deleteFixture(fixture);
        }
    }

    @Test
    void newerInvoicePaidMustWinOverOlderPaymentFailure() {

        TestFixture fixture =
                createSubscription("failed-then-paid");

        try {
            subscriptionService
                    .markPastDueFromStripeWebhook(
                            fixture.stripeSubscriptionId(),
                            100L,
                            "invoice.payment_failed"
                    );

            Subscription pastDue =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            assertThat(pastDue.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.PAST_DUE
                    );

            subscriptionService
                    .registerSuccessfulPaymentFromStripeWebhook(
                            fixture.stripeSubscriptionId(),
                            200L,
                            "invoice.paid"
                    );

            Subscription persisted =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            Store persistedStore =
                    storeRepository
                            .findById(fixture.storeId())
                            .orElseThrow();

            assertThat(persisted.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.ACTIVE
                    );

            assertThat(persistedStore.isActiva())
                    .isTrue();

            assertThat(
                    persisted.getLastStripeEventCreated()
            ).isEqualTo(200L);

            assertThat(
                    persisted.getLastStripeEventPriority()
            ).isEqualTo(200);

            assertThat(
                    persisted.getLastStripeEventType()
            ).isEqualTo("invoice.paid");

        } finally {
            deleteFixture(fixture);
        }
    }

    @Test
    void olderPaymentFailureMustNotOverrideNewerPaidEvent() {

        TestFixture fixture =
                createSubscription("paid-then-old-failure");

        try {
            subscriptionService
                    .registerSuccessfulPaymentFromStripeWebhook(
                            fixture.stripeSubscriptionId(),
                            200L,
                            "invoice.paid"
                    );

            subscriptionService
                    .markPastDueFromStripeWebhook(
                            fixture.stripeSubscriptionId(),
                            100L,
                            "invoice.payment_failed"
                    );

            Subscription persisted =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            assertThat(persisted.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.ACTIVE
                    );

            assertThat(
                    persisted.getLastStripeEventCreated()
            ).isEqualTo(200L);

            assertThat(
                    persisted.getLastStripeEventPriority()
            ).isEqualTo(200);

            assertThat(
                    persisted.getLastStripeEventType()
            ).isEqualTo("invoice.paid");

        } finally {
            deleteFixture(fixture);
        }
    }

    @Test
    void deletedMustWinOverInvoicePaidCreatedInSameSecond() {

        TestFixture fixture =
                createSubscription("same-second");

        try {
            subscriptionService
                    .cancelByStripeSubscriptionIdFromWebhook(
                            fixture.stripeSubscriptionId(),
                            300L,
                            "customer.subscription.deleted"
                    );

            subscriptionService
                    .registerSuccessfulPaymentFromStripeWebhook(
                            fixture.stripeSubscriptionId(),
                            300L,
                            "invoice.paid"
                    );

            Subscription persisted =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            Store persistedStore =
                    storeRepository
                            .findById(fixture.storeId())
                            .orElseThrow();

            assertThat(persisted.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.CANCELLED
                    );

            assertThat(persistedStore.isActiva())
                    .isFalse();

            assertThat(
                    persisted.getLastStripeEventCreated()
            ).isEqualTo(300L);

            assertThat(
                    persisted.getLastStripeEventPriority()
            ).isEqualTo(400);

            assertThat(
                    persisted.getLastStripeEventType()
            ).isEqualTo(
                    "customer.subscription.deleted"
            );

        } finally {
            deleteFixture(fixture);
        }
    }

    @Test
    void createdMustNotOverrideUpdatedCreatedInSameSecond() {

        TestFixture fixture =
                createSubscription(
                        "updated-then-created-same-second"
                );

        try {
            Subscription persisted =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            /*
             * Simula un customer.subscription.updated
             * ya aplicado en ese mismo segundo.
             */
            persisted.setStatus(
                    SubscriptionStatus.PAST_DUE
            );

            persisted.setLastStripeEventCreated(
                    400L
            );

            persisted.setLastStripeEventPriority(
                    350
            );

            persisted.setLastStripeEventType(
                    "customer.subscription.updated"
            );

            subscriptionRepository.saveAndFlush(
                    persisted
            );

            com.stripe.model.Subscription
                    staleCreated =
                    mock(
                            com.stripe.model.Subscription.class
                    );

            when(staleCreated.getId())
                    .thenReturn(
                            fixture.stripeSubscriptionId()
                    );

            subscriptionService
                    .syncStripeSubscriptionUpdatedFromWebhook(
                            staleCreated,
                            400L,
                            "customer.subscription.created"
                    );

            Subscription after =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            assertThat(after.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.PAST_DUE
                    );

            assertThat(
                    after.getLastStripeEventCreated()
            ).isEqualTo(400L);

            assertThat(
                    after.getLastStripeEventPriority()
            ).isEqualTo(350);

            assertThat(
                    after.getLastStripeEventType()
            ).isEqualTo(
                    "customer.subscription.updated"
            );

        } finally {
            deleteFixture(fixture);
        }
    }

    @Test
    void newStripeSubscriptionForSameCustomerMustNotBeBlockedByOldCancellation() {

        TestFixture fixture =
                createSubscription(
                        "cancelled-then-new-subscription"
                );

        try {
            Subscription original =
                    subscriptionRepository
                            .findByStripeSubscriptionId(
                                    fixture.stripeSubscriptionId()
                            )
                            .orElseThrow();

            String stripeCustomerId =
                    original.getStripeCustomerId();

            String newStripeSubscriptionId =
                    fixture.stripeSubscriptionId()
                            + "_new";

            /*
             * La suscripción anterior queda cancelada
             * definitivamente.
             */
            subscriptionService
                    .cancelByStripeSubscriptionIdFromWebhook(
                            fixture.stripeSubscriptionId(),
                            100L,
                            "customer.subscription.deleted"
                    );

            Subscription cancelled =
                    subscriptionRepository
                            .findById(
                                    fixture.subscriptionId()
                            )
                            .orElseThrow();

            assertThat(cancelled.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.CANCELLED
                    );

            assertThat(
                    cancelled.getStore().isActiva()
            ).isFalse();

            /*
             * El mismo customer crea posteriormente una
             * suscripción Stripe nueva con otro sub_...
             */
            com.stripe.model.Subscription
                    newStripeSubscription =
                    mock(
                            com.stripe.model.Subscription.class
                    );

            when(newStripeSubscription.getId())
                    .thenReturn(
                            newStripeSubscriptionId
                    );

            when(newStripeSubscription.getCustomer())
                    .thenReturn(
                            stripeCustomerId
                    );

            when(newStripeSubscription.getStatus())
                    .thenReturn("active");

            com.stripe.model.SubscriptionItemCollection dahliaItems =
                    org.mockito.Mockito.mock(
                            com.stripe.model.SubscriptionItemCollection.class
                    );

            com.stripe.model.SubscriptionItem dahliaPeriodItem =
                    org.mockito.Mockito.mock(
                            com.stripe.model.SubscriptionItem.class
                    );

            org.mockito.Mockito.when(
                    newStripeSubscription.getItems()
            ).thenReturn(dahliaItems);

            org.mockito.Mockito.when(
                    dahliaItems.getData()
            ).thenReturn(
                    java.util.List.of(dahliaPeriodItem)
            );

            org.mockito.Mockito.when(
                    dahliaPeriodItem.getCurrentPeriodStart()
            ).thenReturn(1_800_000_000L);

            org.mockito.Mockito.when(
                    dahliaPeriodItem.getCurrentPeriodEnd()
            ).thenReturn(1_802_592_000L);

            subscriptionService
                    .syncStripeSubscriptionUpdatedFromWebhook(
                            newStripeSubscription,
                            200L,
                            "customer.subscription.updated"
                    );

            Subscription after =
                    subscriptionRepository
                            .findById(
                                    fixture.subscriptionId()
                            )
                            .orElseThrow();

            Store store =
                    storeRepository
                            .findById(
                                    fixture.storeId()
                            )
                            .orElseThrow();

            /*
             * Se reutiliza la fila local del tenant, pero ahora
             * representa la nueva suscripción real de Stripe.
             */
            assertThat(after.getId())
                    .isEqualTo(
                            fixture.subscriptionId()
                    );

            assertThat(after.getStripeSubscriptionId())
                    .isEqualTo(
                            newStripeSubscriptionId
                    );

            assertThat(after.getStripeCustomerId())
                    .isEqualTo(
                            stripeCustomerId
                    );

            assertThat(after.getStatus())
                    .isEqualTo(
                            SubscriptionStatus.ACTIVE
                    );

            assertThat(store.isActiva())
                    .isTrue();

            assertThat(
                    after.getLastStripeEventCreated()
            ).isEqualTo(200L);

            assertThat(
                    after.getLastStripeEventPriority()
            ).isEqualTo(350);

            assertThat(
                    after.getLastStripeEventType()
            ).isEqualTo(
                    "customer.subscription.updated"
            );

        } finally {
            deleteFixture(fixture);
        }
    }

    @Test
    void subscriptionCreatedBeforeLocalProvisioningMustPersistFailedWebhook() {

        String stripeSubscriptionId =
                "sub_gl_g04_missing_local";

        Event event = mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        com.stripe.model.Subscription stripeSubscription =
                mock(com.stripe.model.Subscription.class);

        when(event.getId())
                .thenReturn(MISSING_EVENT_ID);

        when(event.getType())
                .thenReturn(
                        "customer.subscription.created"
                );

        when(event.getCreated())
                .thenReturn(500L);

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(stripeSubscription)
                );

        when(stripeSubscription.getId())
                .thenReturn(stripeSubscriptionId);

        when(stripeSubscription.getCustomer())
                .thenReturn(
                        "cus_gl_g04_missing_local"
                );

        assertThatThrownBy(() ->
                stripeWebhookService.handle(event)
        )
        .isInstanceOf(
                IllegalStateException.class
        )
        .hasMessageContaining(
                "Suscripción Stripe local no encontrada"
        );

        StripeWebhookEvent persisted =
                webhookEventRepository
                        .findByStripeEventId(
                                MISSING_EVENT_ID
                        )
                        .orElseThrow();

        assertThat(persisted.getStatus())
                .isEqualTo(
                        StripeWebhookEventStatus.FAILED
                );

        assertThat(persisted.getErrorMessage())
                .contains(
                        "Suscripción Stripe local no encontrada"
                );
    }

    private TestFixture createSubscription(
            String scenario
    ) {

        String token =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12);

        Store store = new Store();

        store.setNombre(
                "GL-G04 " + scenario
        );

        store.setTheme("default");

        store.setDominio(
                "gl-g04-"
                        + token
                        + ".web-empresarial.com"
        );

        store.setActiva(true);
        store.setPlan(StorePlan.PRO);

        store.setCompanyEmail(
                "gl-g04-"
                        + token
                        + "@example.test"
        );

        store.setCurrency("MXN");

        Store savedStore =
                storeRepository.saveAndFlush(store);

        LocalDateTime now =
                LocalDateTime.now();

        Subscription subscription =
                new Subscription();

        subscription.setStore(savedStore);
        subscription.setPlan(StorePlan.PRO);

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        subscription.setStripeCustomerId(
                "cus_gl_g04_" + token
        );

        subscription.setStripeSubscriptionId(
                "sub_gl_g04_" + token
        );

        subscription.setStripePriceId(
                "price_gl_g04"
        );

        subscription.setStartsAt(now);

        subscription.setCurrentPeriodStart(now);

        subscription.setCurrentPeriodEnd(
                now.plusMonths(1)
        );

        subscription.setNextBillingDate(
                now.plusMonths(1)
        );

        Subscription savedSubscription =
                subscriptionRepository
                        .saveAndFlush(subscription);

        return new TestFixture(
                savedStore.getId(),
                savedSubscription.getId(),
                savedSubscription
                        .getStripeSubscriptionId()
        );
    }

    private void deleteFixture(
            TestFixture fixture
    ) {

        /*
         * Cleanup técnico del integration test.
         *
         * No usamos JPA aquí porque Store.subscription es
         * el lado inverso de una asociación bidireccional y
         * Hibernate intenta sincronizar el grafo al eliminar.
         *
         * Las mutaciones bajo prueba sí pasan por JPA real;
         * únicamente el teardown usa SQL directo.
         */
        jdbcTemplate.update(
                "DELETE FROM subscriptions WHERE id = ?",
                fixture.subscriptionId()
        );

        jdbcTemplate.update(
                "DELETE FROM stores WHERE id = ?",
                fixture.storeId()
        );
    }

    private record TestFixture(
            Long storeId,
            Long subscriptionId,
            String stripeSubscriptionId
    ) {
    }
}
