package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformWorkspaceViewContractTest {

    @Test
    void platformViewsShouldUseSharedWorkspace()
            throws Exception {

        assertTab(
                "index.html",
                ":: tabs('console')"
        );

        assertTab(
                "operations.html",
                ":: tabs('operations')"
        );

        assertTab(
                "dev/index.html",
                ":: tabs('developer')"
        );

        assertTab(
                "../permissions/index.html",
                ":: tabs('permissions')"
        );

        assertTab(
                "../marketplace/index.html",
                ":: tabs('marketplace')"
        );

        assertTab(
                "timeline-detail.html",
                ":: tabs('operations')"
        );

        assertTab(
                "trace-detail.html",
                ":: tabs('operations')"
        );

        assertTab(
                "graph-detail.html",
                ":: tabs('operations')"
        );
    }

    @Test
    void workspaceShouldExposeThreePlatformSections()
            throws Exception {

        String html = Files.readString(
                Path.of(
                        "src/main/resources/templates/admin/"
                        + "fragments/platform-workspace.html"
                )
        );

        assertThat(html)
                .contains(
                        "href=\"/admin/platform\""
                )
                .contains(
                        "href=\"/admin/platform/operations\""
                )
                .contains(
                        "href=\"/admin/platform/dev\""
                )
                .contains(
                        "href=\"/admin/permissions\""
                )
                .contains(
                        "href=\"/admin/marketplace\""
                )
                .doesNotContain(
                        "/admin/platform/timeline/"
                )
                .doesNotContain(
                        "/admin/platform/trace/"
                )
                .doesNotContain(
                        "/admin/platform/graph/"
                );
    }

    private void assertTab(
            String file,
            String expected
    ) throws Exception {

        String html = Files.readString(
                Path.of(
                        "src/main/resources/templates/admin/"
                        + "platform/"
                        + file
                )
        );

        assertThat(html)
                .as(file)
                .contains(expected);
    }
}
