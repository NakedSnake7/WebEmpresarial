package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StoreSettingsWorkspaceNavigationContractTest {

    private static final Path SIDEBAR =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "fragments/sidebar.html"
            );

    @Test
    void sidebarShouldExposeSingleSettingsWorkspace()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "href=\"/admin/store/settings\""
                )
                .contains(
                        "th:if=\"${canViewStoreSettings}\""
                )
                .contains(
                        "<span class=\"sidebar-text\">"
                        + "Configuración</span>"
                )
                .doesNotContain(
                        "href=\"/admin/store/crm\""
                )
                .doesNotContain(
                        "CRM Settings"
                )
                .doesNotContain(
                        "General tienda"
                );
    }

    @Test
    void settingsWorkspaceShouldUseVisualActivePath()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "sidebarActivePath == "
                        + "'/admin/store/settings'"
                )
                .contains(
                        "sidebarActivePath.startsWith("
                );
    }
}
