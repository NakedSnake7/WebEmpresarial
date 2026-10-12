package com.webempresarial.store.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StoreStripePaymentAvailabilityTest {

    @Test
    void shouldBeUnavailableWhenDisconnected() {

        Store store = new Store();

        store.setStripeConnected(false);
        store.setStripeConnectedAccountId(
                "acct_test_123"
        );

        assertThat(
                store.isStripePaymentAvailable()
        ).isFalse();
    }

    @Test
    void shouldBeUnavailableWhenAccountIdIsNull() {

        Store store = new Store();

        store.setStripeConnected(true);
        store.setStripeConnectedAccountId(null);

        assertThat(
                store.isStripePaymentAvailable()
        ).isFalse();
    }

    @Test
    void shouldBeUnavailableWhenAccountIdIsBlank() {

        Store store = new Store();

        store.setStripeConnected(true);
        store.setStripeConnectedAccountId("   ");

        assertThat(
                store.isStripePaymentAvailable()
        ).isFalse();
    }

    @Test
    void shouldBeAvailableWhenConnectedWithAccountId() {

        Store store = new Store();

        store.setStripeConnected(true);
        store.setStripeConnectedAccountId(
                "acct_test_123"
        );

        assertThat(
                store.isStripePaymentAvailable()
        ).isTrue();
    }
}
