package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AutomationWorkspaceNavigationContractTest {

    private static final Path SIDEBAR =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "fragments/sidebar.html"
            );

    @Test
    void sidebarShouldExposeSingleAutomationWorkspace()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "href=\"/admin/automations/kernel\""
                )
                .contains(
                        "<span class=\"sidebar-text\">"
                        + "Automations</span>"
                )
                .doesNotContain(
                        "href=\"/admin/automations/history\""
                )
                .doesNotContain(
                        ">Kernel Automations<"
                )
                .doesNotContain(
                        ">Historial Automations<"
                );
    }

    @Test
    void sidebarShouldUseVisualAutomationActivePath()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "sidebarActivePath == "
                        + "'/admin/automations/kernel'"
                );
    }
}
