package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SaasWorkspaceNavigationContractTest {

    private static final Path SIDEBAR =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "fragments/sidebar.html"
            );

    @Test
    void sidebarShouldExposeSingleSaasWorkspace()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "href=\"/admin/saas\""
                )
                .contains(
                        "<span class=\"sidebar-text\">"
                        + "SaaS</span>"
                )
                .doesNotContain(
                        "href=\"/admin/stores\""
                )
                .doesNotContain(
                        "href=\"/admin/subscriptions\""
                )
                .doesNotContain(
                        ">SaaS Dashboard<"
                );
    }

    @Test
    void marketplaceShouldNotBeIndependentSidebarDestination()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .doesNotContain(
                        "href=\"/admin/marketplace\""
                );
    }

    @Test
    void sidebarShouldUseVisualSaasActivePath()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "sidebarActivePath == "
                        + "'/admin/saas'"
                );
    }
}
