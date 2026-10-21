package com.webempresarial.store.commerce.infrastructure.checkout.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.webempresarial.store.model.Store;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class StripeCommerceCheckoutSessionExpirationTest {

    private final StripeCommerceCheckoutService service =
            new StripeCommerceCheckoutService();

    @Test
    void expireSession_shouldExpireOpenSession() throws Exception {

        Store store = connectedStore();
        Session session = mock(Session.class);

        when(session.getStatus())
                .thenReturn("open");

        try (MockedStatic<Session> stripe =
                     mockStatic(Session.class)) {

            stripe.when(() ->
                    Session.retrieve(
                            eq("cs_open"),
                            any(RequestOptions.class)
                    )
            ).thenReturn(session);

            service.expireSession(
                    "cs_open",
                    store
            );

            verify(session)
                    .expire(
                            any(RequestOptions.class)
                    );
        }
    }

    @Test
    void expireSession_shouldBeIdempotentWhenAlreadyExpired()
            throws Exception {

        Store store = connectedStore();
        Session session = mock(Session.class);

        when(session.getStatus())
                .thenReturn("expired");

        try (MockedStatic<Session> stripe =
                     mockStatic(Session.class)) {

            stripe.when(() ->
                    Session.retrieve(
                            eq("cs_expired"),
                            any(RequestOptions.class)
                    )
            ).thenReturn(session);

            service.expireSession(
                    "cs_expired",
                    store
            );

            verify(session, never())
                    .expire(any(RequestOptions.class));
        }
    }

    @Test
    void expireSession_shouldRejectCompletedSession()
            throws Exception {

        Store store = connectedStore();
        Session session = mock(Session.class);

        when(session.getStatus())
                .thenReturn("complete");

        try (MockedStatic<Session> stripe =
                     mockStatic(Session.class)) {

            stripe.when(() ->
                    Session.retrieve(
                            eq("cs_complete"),
                            any(RequestOptions.class)
                    )
            ).thenReturn(session);

            assertThatThrownBy(() ->
                    service.expireSession(
                            "cs_complete",
                            store
                    )
            )
                    .isInstanceOf(
                            IllegalStateException.class
                    )
                    .hasMessage(
                            "La sesión Stripe ya no está abierta"
                    );

            verify(session, never())
                    .expire(any(RequestOptions.class));
        }
    }

    @Test
    void expireSession_shouldWorkForExistingSessionEvenIfNewPaymentsAreDisabled()
            throws Exception {

        Store store = connectedStore();

        /*
         * Una sesión existente debe poder cancelarse aunque
         * la tienda ya no esté habilitada para crear pagos nuevos.
         */
        store.setStripeConnected(false);

        Session session = mock(Session.class);

        when(session.getStatus())
                .thenReturn("open");

        try (MockedStatic<Session> stripe =
                     mockStatic(Session.class)) {

            stripe.when(() ->
                    Session.retrieve(
                            eq("cs_existing"),
                            any(RequestOptions.class)
                    )
            ).thenReturn(session);

            service.expireSession(
                    "cs_existing",
                    store
            );

            verify(session)
                    .expire(
                            any(RequestOptions.class)
                    );
        }
    }

    private Store connectedStore() {

        Store store = new Store();
        store.setId(5L);
        store.setStripeConnected(true);
        store.setStripeConnectedAccountId(
                "acct_store_5"
        );

        return store;
    }
}
