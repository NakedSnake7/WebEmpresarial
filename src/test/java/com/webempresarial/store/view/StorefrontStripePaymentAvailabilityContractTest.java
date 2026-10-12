package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class StorefrontStripePaymentAvailabilityContractTest {

    private static final Path THEMES =
            Path.of(
                    "src/main/resources/templates/themes"
            );

    @Test
    void stripeCheckoutMustUseCanonicalAvailabilityRule()
            throws IOException {

        List<Path> checkoutFiles;

        try (var paths = Files.walk(THEMES)) {
            checkoutFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .equals("checkout.html")
                    )
                    .filter(path -> {
                        try {
                            return Files.readString(path)
                                    .contains(
                                            "value=\"STRIPE\""
                                    );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        }

        assertThat(checkoutFiles)
                .as("Debe existir al menos un checkout con Stripe")
                .isNotEmpty();

        for (Path checkout : checkoutFiles) {

            String html =
                    Files.readString(checkout);

            assertThat(html)
                    .as(
                            "%s debe usar "
                            + "store.stripePaymentAvailable",
                            checkout
                    )
                    .contains(
                            "store.stripePaymentAvailable"
                    );

            assertThat(html)
                    .as(
                            "%s no debe usar directamente "
                            + "store.stripeConnected",
                            checkout
                    )
                    .doesNotContain(
                            "store.stripeConnected"
                    );
        }
    }
}
