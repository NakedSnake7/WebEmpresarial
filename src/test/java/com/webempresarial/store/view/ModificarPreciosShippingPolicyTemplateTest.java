package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ModificarPreciosShippingPolicyTemplateTest {

    private static final Path TEMPLATE =
            Path.of(
                    "src/main/resources/templates/admin/modificar-precios.html"
            );

    @Test
    void shouldExposeStoreFreeShippingPolicyForm()
            throws Exception {

        String html = Files.readString(TEMPLATE);

        assertThat(html)
                .contains(
                        "th:action=\"@{/admin/store/settings/shipping}\""
                )
                .contains(
                        "name=\"freeShippingEnabled\""
                )
                .contains(
                        "name=\"freeShippingThreshold\""
                )
                .contains(
                        "settings.freeShippingEnabled"
                )
                .contains(
                        "settings.freeShippingThreshold"
                );
    }
}
