package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StripeConnectFrontendContractTest {

    private static final String ENDPOINT =
            "/api/admin/stripe/connect/onboarding";

    @Test
    void stripeConnectOnboardingMustBeCentralizedInAdminLayout()
            throws IOException {

        String settings = read(
                "admin/store/settings.html"
        );

        String dashboard = read(
                "admin/dashboard.html"
        );

        String layout = read(
                "admin/layout/main.html"
        );

        assertThat(settings)
                .contains("id=\"stripeConnectBtn\"");

        assertThat(dashboard)
                .contains("id=\"stripeConnectBtn\"");

        assertThat(settings)
                .contains("canManageStripeConnect")
                .contains(
                        "features.canUse(store, 'STRIPE_CONNECT')"
                )
                .contains("data-stripe-connect-locked")
                .contains("/admin/upgrade")
                .contains("STRIPE_CONNECT");

        assertThat(dashboard)
                .contains("canManageStripeConnect")
                .contains(
                        "features.canUse(store, 'STRIPE_CONNECT')"
                )
                .contains("data-stripe-connect-locked")
                .contains("/admin/upgrade")
                .contains("STRIPE_CONNECT");

        assertThat(settings)
                .doesNotContain(ENDPOINT);

        assertThat(dashboard)
                .doesNotContain(ENDPOINT);

        assertThat(countOccurrences(layout, ENDPOINT))
                .isEqualTo(1);
    }

    @Test
    void stripeConnectOnboardingShouldSendCsrfToken()
            throws IOException {

        String layout = read(
                "admin/layout/main.html"
        );

        assertThat(layout)
                .contains("name=\"_csrf\"")
                .contains("name=\"_csrf_header\"")
                .contains("const csrfToken")
                .contains("const csrfHeader")
                .contains(
                        "headers[csrfHeader] = csrfToken"
                )
                .contains(
                        "\"/api/admin/stripe/connect/onboarding\""
                )
                .contains("method: \"POST\"");
    }

    private String read(String relativePath)
            throws IOException {

        return Files.readString(
                Path.of(
                        "src/main/resources/templates/"
                                + relativePath
                )
        );
    }

    private long countOccurrences(
            String text,
            String value
    ) {

        long count = 0;
        int index = 0;

        while ((index = text.indexOf(value, index)) >= 0) {
            count++;
            index += value.length();
        }

        return count;
    }
}
