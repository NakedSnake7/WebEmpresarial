package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StoreSettingsShippingPolicyTemplateTest {

    private static final Path SETTINGS =
            Path.of(
                    "src/main/resources/templates/admin/store/"
                    + "settings.html"
            );

    private static final Path PRICING =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "modificar-precios.html"
            );

    @Test
    void shippingPolicyShouldBelongToSettingsPayments()
            throws Exception {

        String html = Files.readString(SETTINGS);

        assertThat(html)
                .contains("id=\"payments\"")
                .contains(
                        "data-store-settings-shipping"
                )
                .contains(
                        "th:action="
                        + "\"@{/admin/store/settings/shipping}\""
                )
                .contains(
                        "id=\"shippingPolicyForm\""
                )
                .contains(
                        "name=\"freeShippingEnabled\""
                )
                .contains(
                        "name=\"shippingCost\""
                )
                .contains(
                        "name=\"freeShippingThreshold\""
                );
    }

    @Test
    void shippingMutationShouldRequireStoreEdit()
            throws Exception {

        String html = Files.readString(SETTINGS);

        assertThat(html)
                .contains(
                        "th:disabled="
                        + "\"${!canManageStoreSettings}\""
                )
                .contains(
                        "th:if="
                        + "\"${canManageStoreSettings}\""
                );
    }

    @Test
    void pricingShouldNoLongerOwnShippingPolicy()
            throws Exception {

        String html = Files.readString(PRICING);

        assertThat(html)
                .doesNotContain(
                        "Política de envío"
                )
                .doesNotContain(
                        "shippingPolicyForm"
                )
                .doesNotContain(
                        "/admin/store/settings/shipping"
                )
                .doesNotContain(
                        "Compatibilidad temporal "
                        + "de Commerce Workspace v1"
                );
    }
}
