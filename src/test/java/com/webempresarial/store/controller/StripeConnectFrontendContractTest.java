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
                .doesNotContain(ENDPOINT);

        assertThat(dashboard)
                .doesNotContain(ENDPOINT);

        assertThat(countOccurrences(layout, ENDPOINT))
                .isEqualTo(1);
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
