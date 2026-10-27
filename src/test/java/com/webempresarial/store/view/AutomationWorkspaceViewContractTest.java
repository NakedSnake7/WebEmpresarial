package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AutomationWorkspaceViewContractTest {

    @Test
    void automationViewsShouldUseSharedWorkspace()
            throws Exception {

        assertTab(
                "kernel.html",
                ":: tabs('kernel')"
        );

        assertTab(
                "history.html",
                ":: tabs('history')"
        );

        assertTab(
                "history-detail.html",
                ":: tabs('history')"
        );
    }

    @Test
    void workspaceShouldExposeOnlyPlatformDiagnosticRoutes()
            throws Exception {

        String html = Files.readString(
                Path.of(
                        "src/main/resources/templates/admin/"
                        + "fragments/automations-workspace.html"
                )
        );

        assertThat(html)
                .contains(
                        "href=\"/admin/automations/kernel\""
                )
                .contains(
                        "href=\"/admin/automations/history\""
                )
                .doesNotContain(
                        "href=\"/admin/automations\""
                );
    }

    private void assertTab(
            String file,
            String expected
    ) throws Exception {

        String html = Files.readString(
                Path.of(
                        "src/main/resources/templates/admin/"
                        + "automations/"
                        + file
                )
        );

        assertThat(html)
                .as(file)
                .contains(expected);
    }
}
