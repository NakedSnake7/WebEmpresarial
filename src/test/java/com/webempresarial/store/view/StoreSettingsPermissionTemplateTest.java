package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StoreSettingsPermissionTemplateTest {

    private static final Path TEMPLATE =
            Path.of(
                    "src/main/resources/templates/admin/store/settings.html"
            );

    @Test
    void shouldSeparateGeneralAndCommercialEditingPermissions()
            throws Exception {

        String html = Files.readString(TEMPLATE);

        assertThat(html)
                .contains("data-store-settings-general")
                .contains(
                        "th:disabled=\"${!canManageStoreSettings}\""
                )
                .contains("data-store-settings-commercial")
                .contains(
                        "th:disabled=\"${!canManageCommercialInfo}\""
                )
                .contains("data-store-settings-proposals")
                .contains(
                        "th:if=\"${canManageStoreSettings or canManageCommercialInfo}\""
                );
    }
}
