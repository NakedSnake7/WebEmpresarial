package com.webempresarial.store.feature.catalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomDomainCatalogContractTest {

    @Test
    void customDomainShouldRemainRegisteredButNotPublishedInSidebar()
            throws IOException {

        String source = Files.readString(
                Path.of(
                        "src/main/java/com/webempresarial/store/"
                                + "feature/catalog/PlatformFeatures.java"
                )
        );

        assertThat(source)
                .contains(
                        ".feature(customDomain())"
                )
                .contains(
                        "FeatureDefinition.builder(Feature.CUSTOM_DOMAIN)"
                )
                .contains(
                        ".url(\"/admin/domains\")"
                )
                .doesNotContain(
                        ".item(\"Dominio\", \"🌐\", "
                                + "\"/admin/domains\", "
                                + "Feature.CUSTOM_DOMAIN)"
                );
    }
}
