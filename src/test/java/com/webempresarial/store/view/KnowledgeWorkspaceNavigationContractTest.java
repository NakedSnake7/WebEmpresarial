package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeWorkspaceNavigationContractTest {

    private static final Path SIDEBAR =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "fragments/sidebar.html"
            );

    @Test
    void sidebarShouldExposeSingleKnowledgeWorkspace()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "href=\"/admin/knowledge\""
                )
                .contains(
                        "<span class=\"sidebar-text "
                        + "flex-grow-1\">"
                )
                .contains(
                        "Knowledge"
                )
                .doesNotContain(
                        "href=\"/admin/knowledge/library\""
                )
                .doesNotContain(
                        "href=\"/admin/knowledge/new\""
                )
                .doesNotContain(
                        "status=IN_REVIEW"
                )
                .doesNotContain(
                        "status=PUBLISHED"
                )
                .doesNotContain(
                        "/admin/knowledge/review"
                );
    }

    @Test
    void knowledgeWorkspaceShouldUseVisualActivePath()
            throws Exception {

        String html = Files.readString(SIDEBAR);

        assertThat(html)
                .contains(
                        "sidebarActivePath == "
                        + "'/admin/knowledge'"
                )
                .contains(
                        "sidebarActivePath.startsWith("
                );
    }
}
