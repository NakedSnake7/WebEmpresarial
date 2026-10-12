package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class StorefrontTransferPaymentAvailabilityContractTest {

    private static final Path THEMES =
            Path.of(
                    "src/main/resources/templates/themes"
            );

    @Test
    void everyCheckoutWithTransferMustRespectStoreAvailability()
            throws Exception {

        List<Path> checkoutTemplates =
                discoverCheckoutTemplates();

        assertThat(checkoutTemplates)
                .as("Debe existir al menos un checkout storefront")
                .isNotEmpty();

        for (Path template : checkoutTemplates) {

            String html =
                    Files.readString(template);

            if (!html.contains("value=\"TRANSFER\"")) {
                continue;
            }

            assertThat(html)
                    .as(
                            "TRANSFER debe depender de "
                            + "settings.transferPaymentAvailable en %s",
                            template
                    )
                    .contains(
                            "settings.transferPaymentAvailable"
                    );
        }
    }

    @Test
    void transferOptionMustNotBeRenderedWhenUnavailable()
            throws Exception {

        for (Path template : discoverCheckoutTemplates()) {

            String html =
                    Files.readString(template);

            if (!html.contains("value=\"TRANSFER\"")) {
                continue;
            }

            int transfer =
                    html.indexOf("value=\"TRANSFER\"");

            int start =
                    Math.max(
                            0,
                            transfer - 700
                    );

            int end =
                    Math.min(
                            html.length(),
                            transfer + 700
                    );

            String transferBlock =
                    html.substring(
                            start,
                            end
                    );

            assertThat(transferBlock)
                    .as(
                            "El bloque TRANSFER debe usar th:if "
                            + "en %s",
                            template
                    )
                    .contains("th:if=")
                    .contains(
                            "settings.transferPaymentAvailable"
                    );
        }
    }

    private List<Path> discoverCheckoutTemplates()
            throws IOException {

        try (Stream<Path> paths =
                     Files.walk(THEMES)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .equals("checkout.html")
                    )
                    .filter(path ->
                            path.toString()
                                    .contains(
                                            "/fragments/"
                                    )
                    )
                    .sorted()
                    .toList();
        }
    }
}
