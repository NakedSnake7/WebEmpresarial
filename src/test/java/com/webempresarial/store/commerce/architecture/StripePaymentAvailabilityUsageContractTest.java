package com.webempresarial.store.commerce.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StripePaymentAvailabilityUsageContractTest {

    private static final Path CHECKOUT_SERVICE =
            Path.of(
                    "src/main/java/com/webempresarial/store/commerce/"
                    + "infrastructure/checkout/payment/"
                    + "StripeCommerceCheckoutService.java"
            );

    private static final Path CHECKOUT_CONTROLLER =
            Path.of(
                    "src/main/java/com/webempresarial/store/commerce/"
                    + "web/storefront/checkout/"
                    + "StripeCheckoutController.java"
            );

    @Test
    void stripeCommerceCheckoutMustUseCanonicalAvailabilityRule()
            throws Exception {

        String source =
                Files.readString(CHECKOUT_SERVICE);

        assertThat(source)
                .contains("isStripePaymentAvailable()");

        assertThat(source)
                .doesNotContain("isStripeConnected()");
    }

    @Test
    void stripeCheckoutControllerMustUseCanonicalAvailabilityRule()
            throws Exception {

        String source =
                Files.readString(CHECKOUT_CONTROLLER);

        assertThat(source)
                .contains("isStripePaymentAvailable()");

        assertThat(source)
                .doesNotContain("isStripeConnected()");
    }
}
