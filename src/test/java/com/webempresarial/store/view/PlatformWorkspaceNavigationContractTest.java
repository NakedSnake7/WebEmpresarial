package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformWorkspaceNavigationContractTest {

    private static final Path SIDEBAR =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "fragments/sidebar.html"
            );

    @Test
    void sidebarShouldExposeSinglePlatformWorkspace()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "href=\"/admin/platform\""
                )
                .contains(
                        "<span class=\"sidebar-text\">"
                        + "Platform</span>"
                )
                .doesNotContain(
                        "href=\"/admin/platform/operations\""
                )
                .doesNotContain(
                        "href=\"/admin/platform/dev\""
                )
                .doesNotContain(
                        ">Platform Console<"
                )
                .doesNotContain(
                        ">Developer Kit<"
                );
    }

    @Test
    void sidebarShouldUseVisualPlatformActivePath()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "sidebarActivePath == "
                        + "'/admin/platform'"
                );
    }
}
