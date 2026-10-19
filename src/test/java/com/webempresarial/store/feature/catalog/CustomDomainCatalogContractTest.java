package com.webempresarial.store.feature.catalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomDomainCatalogContractTest {

    private static final Path PLATFORM_FEATURES =
            Path.of(
                    "src/main/java/com/webempresarial/store/"
                            + "feature/catalog/PlatformFeatures.java"
            );

    private static final Path SECURITY_CONFIG =
            Path.of(
                    "src/main/java/com/webempresarial/store/"
                            + "config/SecurityConfig.java"
            );

    @Test
    void customDomainShouldRemainProButHiddenUntilImplemented()
            throws IOException {

        String source = Files.readString(PLATFORM_FEATURES);

        int start =
                source.indexOf(
                        "private FeatureDefinition customDomain()"
                );

        int end =
                source.indexOf(
                        "private FeatureDefinition whiteLabelFull()",
                        start
                );

        assertThat(start).isGreaterThanOrEqualTo(0);
        assertThat(end).isGreaterThan(start);

        String customDomain =
                source.substring(start, end);

        assertThat(source)
                .contains(".feature(customDomain())");

        assertThat(customDomain)
                .contains(
                        "FeatureDefinition.builder(Feature.CUSTOM_DOMAIN)"
                )
                .contains(
                        ".url(\"/admin/domains\")"
                )
                .contains(
                        ".accessPolicy(pro())"
                )
                .contains(
                        ".presentation(unimplementedPresentation())"
                );

        assertThat(source)
                .contains(
                        "private FeaturePresentation "
                                + "unimplementedPresentation()"
                )
                .contains(".showInSidebar(false)")
                .contains(".showInDashboard(false)")
                .contains(".showInBilling(true)")
                .contains(".showUpgradeCard(true)")
                .contains(".trackUsage(false)");

        /*
         * El dominio futuro tampoco debe registrarse manualmente
         * en la navegación.
         */
        assertThat(source)
                .doesNotContain(
                        ".item(\"Dominio\", \"🌐\", "
                                + "\"/admin/domains\", "
                                + "Feature.CUSTOM_DOMAIN)"
                );
    }

    @Test
    void customDomainRoutesShouldFailClosedUntilImplemented()
            throws IOException {

        String security =
                Files.readString(SECURITY_CONFIG);

        assertThat(security)
                .contains(
                        "\"/admin/domains\","
                )
                .contains(
                        "\"/admin/domains/**\""
                );

        int domainMatcher =
                security.indexOf(
                        "\"/admin/domains\","
                );

        int denyAll =
                security.indexOf(
                        ".denyAll()",
                        domainMatcher
                );

        int dashboard =
                security.indexOf(
                        "Dashboard principal.",
                        domainMatcher
                );

        assertThat(domainMatcher)
                .isGreaterThanOrEqualTo(0);

        assertThat(denyAll)
                .isGreaterThan(domainMatcher);

        assertThat(dashboard)
                .isGreaterThan(denyAll);
    }
}
