package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class StorefrontOrderConfirmationTemplateTest {

    private static final Path THEMES =
            Path.of(
                    "src/main/resources/templates/themes"
            );

    private static final Path SHARED =
            THEMES.resolve(
                    "shared/fragments/order-confirmation.html"
            );

    @Test
    void everyStorefrontThemeMustExposeGraciasView()
            throws IOException {

        List<Path> themes =
                storefrontThemes();

        assertThat(themes)
                .as("Debe existir al menos un tema storefront")
                .isNotEmpty();

        for (Path theme : themes) {

            Path gracias =
                    theme.resolve("gracias.html");

            assertThat(gracias)
                    .as(
                            "Falta gracias.html para tema %s",
                            theme.getFileName()
                    )
                    .exists()
                    .isRegularFile();

            String html =
                    Files.readString(gracias);

            assertThat(html)
                    .contains(
                            "themes/shared/fragments/"
                            + "order-confirmation"
                    )
                    .contains("th:replace");
        }
    }

    @Test
    void sharedConfirmationMustShowOrderIdentityAndTotal()
            throws IOException {

        assertThat(SHARED)
                .exists()
                .isRegularFile();

        String html =
                Files.readString(SHARED);

        assertThat(html)
                .contains("order.id")
                .contains("order.total")
                .contains("order.paymentMethod");
    }

    @Test
    void transferBankingDataMustOnlyLiveInsideTransferBlock()
            throws IOException {

        assertThat(SHARED)
                .exists();

        String html =
                Files.readString(SHARED);

        assertThat(html)
                .contains("settings.transferBankName")
                .contains("settings.transferAccountHolder")
                .contains("settings.transferClabe")
                .containsIgnoringCase("pendiente");

        Pattern transferBlock =
                Pattern.compile(
                        """
                        (?s)
                        <th:block
                        [^>]*th:if="
                        \\$\\{[^"]*TRANSFER[^"]*\\}"
                        [^>]*>
                        .*?
                        settings\\.transferBankName
                        .*?
                        settings\\.transferAccountHolder
                        .*?
                        settings\\.transferClabe
                        .*?
                        </th:block>
                        """,
                        Pattern.COMMENTS
                );

        assertThat(
                transferBlock.matcher(html).find()
        )
                .as(
                        "Banco, titular y CLABE deben "
                        + "estar dentro de un bloque TRANSFER"
                )
                .isTrue();
    }

    @Test
    void stripeConfirmationMustNotDependOnBankingData()
            throws IOException {

        assertThat(SHARED)
                .exists();

        String html =
                Files.readString(SHARED);

        Pattern stripeBlock =
                Pattern.compile(
                        """
                        (?s)
                        <th:block
                        [^>]*th:if="
                        \\$\\{[^"]*STRIPE[^"]*\\}"
                        [^>]*>
                        (.*?)
                        </th:block>
                        """,
                        Pattern.COMMENTS
                );

        var matcher =
                stripeBlock.matcher(html);

        assertThat(matcher.find())
                .as(
                        "Debe existir mensaje específico "
                        + "para Stripe"
                )
                .isTrue();

        String stripeHtml =
                matcher.group(1);

        assertThat(stripeHtml)
                .doesNotContain(
                        "settings.transferBankName"
                )
                .doesNotContain(
                        "settings.transferAccountHolder"
                )
                .doesNotContain(
                        "settings.transferClabe"
                );
    }

    private List<Path> storefrontThemes()
            throws IOException {

        try (Stream<Path> paths =
                     Files.list(THEMES)) {

            return paths
                    .filter(Files::isDirectory)
                    .filter(path ->
                            !"shared".equals(
                                    path.getFileName()
                                            .toString()
                            )
                    )
                    .filter(path ->
                            Files.exists(
                                    path.resolve(
                                            "index.html"
                                    )
                            )
                    )
                    .sorted()
                    .toList();
        }
    }
}
