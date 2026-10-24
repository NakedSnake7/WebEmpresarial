package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Subscription;

import com.webempresarial.store.entity.StripeWebhookEvent;
import com.webempresarial.store.model.StripeWebhookEventStatus;
import com.webempresarial.store.repository.StripeWebhookEventRepository;

@SpringBootTest
@ActiveProfiles("dev")
class StripeWebhookFailurePersistenceIntegrationTest {

    private static final String EVENT_ID =
            "evt_failure_persistence_it";

    @Autowired
    private StripeWebhookService webhookService;

    @Autowired
    private StripeWebhookEventRepository
            webhookEventRepository;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @BeforeEach
    void cleanBefore() {
        deleteTestEvent();
    }

    @AfterEach
    void cleanAfter() {
        deleteTestEvent();
    }

    @Test
    void failedWebhookShouldRemainPersistedAfterProcessingRollback() {

        Event event =
                mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        Subscription stripeSubscription =
                mock(Subscription.class);

        when(event.getId())
                .thenReturn(EVENT_ID);

        when(event.getType())
                .thenReturn(
                        "customer.subscription.deleted"
                );

        when(event.getCreated())
                .thenReturn(1_000L);

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(stripeSubscription)
                );

        when(stripeSubscription.getId())
                .thenReturn(
                        "sub_failure_persistence_it"
                );

        doThrow(
                new IllegalStateException(
                        "forced webhook failure"
                )
        ).when(subscriptionService)
                .cancelByStripeSubscriptionIdFromWebhook(
                        "sub_failure_persistence_it",
                        1_000L,
                        "customer.subscription.deleted"
                );

        assertThatThrownBy(() ->
                webhookService.handle(event)
        )
        .isInstanceOf(
                IllegalStateException.class
        )
        .hasMessage(
                "forced webhook failure"
        );

        StripeWebhookEvent persisted =
                webhookEventRepository
                        .findByStripeEventId(EVENT_ID)
                        .orElseThrow();

        assertThat(persisted.getStatus())
                .isEqualTo(
                        StripeWebhookEventStatus.FAILED
                );

        assertThat(persisted.getProcessedAt())
                .isNotNull();

        assertThat(persisted.getErrorMessage())
                .isEqualTo(
                        "forced webhook failure"
                );
    }

    @Test
    void failedWebhookShouldBeProcessedSuccessfullyOnStripeRetry() {

        Event event =
                mock(Event.class);

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        Subscription stripeSubscription =
                mock(Subscription.class);

        when(event.getId())
                .thenReturn(EVENT_ID);

        when(event.getType())
                .thenReturn(
                        "customer.subscription.deleted"
                );

        when(event.getCreated())
                .thenReturn(1_000L);

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(stripeSubscription)
                );

        when(stripeSubscription.getId())
                .thenReturn(
                        "sub_failure_persistence_it"
                );

        /*
         * Primer delivery de Stripe falla.
         * El segundo delivery con exactamente el mismo event_id
         * debe poder procesarse normalmente.
         */
        when(
                subscriptionService
                        .cancelByStripeSubscriptionIdFromWebhook(
                                "sub_failure_persistence_it",
                                1_000L,
                                "customer.subscription.deleted"
                        )
        )
        .thenThrow(
                new IllegalStateException(
                        "forced webhook failure"
                )
        )
        .thenReturn(null);

        assertThatThrownBy(() ->
                webhookService.handle(event)
        )
        .isInstanceOf(
                IllegalStateException.class
        )
        .hasMessage(
                "forced webhook failure"
        );

        StripeWebhookEvent failed =
                webhookEventRepository
                        .findByStripeEventId(EVENT_ID)
                        .orElseThrow();

        assertThat(failed.getStatus())
                .isEqualTo(
                        StripeWebhookEventStatus.FAILED
                );

        assertThat(failed.getErrorMessage())
                .isEqualTo(
                        "forced webhook failure"
                );

        /*
         * Stripe reenvía el MISMO evento.
         */
        webhookService.handle(event);

        StripeWebhookEvent processed =
                webhookEventRepository
                        .findByStripeEventId(EVENT_ID)
                        .orElseThrow();

        assertThat(processed.getStatus())
                .isEqualTo(
                        StripeWebhookEventStatus.PROCESSED
                );

        assertThat(processed.getProcessedAt())
                .isNotNull();

        /*
         * markProcessed() debe eliminar el error anterior.
         */
        assertThat(processed.getErrorMessage())
                .isNull();

        verify(
                subscriptionService,
                times(2)
        ).cancelByStripeSubscriptionIdFromWebhook(
                "sub_failure_persistence_it",
                1_000L,
                "customer.subscription.deleted"
        );
    }


    private void deleteTestEvent() {

        webhookEventRepository
                .findByStripeEventId(EVENT_ID)
                .ifPresent(
                        webhookEventRepository::delete
                );

        webhookEventRepository.flush();
    }
}
