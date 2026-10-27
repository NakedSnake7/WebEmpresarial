package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeWorkspaceViewContractTest {

    private static final Path BASE =
            Path.of(
                    "src/main/resources/templates/admin/knowledge"
            );

    @Test
    void workspaceShouldExposeOnlySummaryAndLibraryAsSections()
            throws Exception {

        String html = read(
                "src/main/resources/templates/admin/fragments/"
                + "knowledge-workspace.html"
        );

        assertThat(html)
                .contains(
                        "th:fragment="
                        + "\"tabs(activeTab, showCreateAction)\""
                )
                .contains("Resumen")
                .contains("Biblioteca")
                .contains("+ Nuevo conocimiento")
                .contains(
                        "th:if=\"${showCreateAction}\""
                )
                .doesNotContain("Revisiones")
                .doesNotContain("Publicados")
                .doesNotContain("status=IN_REVIEW")
                .doesNotContain("status=PUBLISHED");
    }

    @Test
    void primaryViewsShouldUseExpectedWorkspaceSection()
            throws Exception {

        assertThat(
                Files.readString(
                        BASE.resolve("dashboard.html")
                )
        ).contains(
                ":: tabs('summary', true)"
        );

        assertThat(
                Files.readString(
                        BASE.resolve("index.html")
                )
        ).contains(
                ":: tabs('library', true)"
        );

        assertThat(
                Files.readString(
                        BASE.resolve("create.html")
                )
        ).contains(
                ":: tabs('none', false)"
        );
    }

    @Test
    void contextualViewsShouldRemainInsideLibraryContext()
            throws Exception {

        for (String file : new String[] {
                "detail.html",
                "version-create.html",
                "version-detail.html"
        }) {

            assertThat(
                    Files.readString(
                            BASE.resolve(file)
                    )
            ).contains(
                    ":: tabs('library', false)"
            );
        }
    }

    @Test
    void libraryHeaderShouldNotDuplicateWorkspaceNavigation()
            throws Exception {

        String html =
                Files.readString(
                        BASE.resolve("index.html")
                );

        assertThat(html)
                .doesNotContain(
                        "th:href=\"@{/admin/knowledge}\""
                )
                .doesNotContain(
                        "th:href=\"@{/admin/knowledge/new}\""
                )
                .contains(
                        "id=\"refreshKnowledgeButton\""
                );
    }

    @Test
    void detailShouldKeepContextualLibraryBackLink()
            throws Exception {

        String html =
                Files.readString(
                        BASE.resolve("detail.html")
                );

        assertThat(html)
                .doesNotContain(
                        "← Volver al Knowledge Engine"
                )
                .contains(
                        "← Volver a la biblioteca"
                );
    }

    @Test
    void everyKnowledgeViewShouldLoadWorkspaceStyles()
            throws Exception {

        for (String file : new String[] {
                "dashboard.html",
                "index.html",
                "create.html",
                "detail.html",
                "version-create.html",
                "version-detail.html"
        }) {

            assertThat(
                    Files.readString(
                            BASE.resolve(file)
                    )
            ).contains(
                    "/assets/css/admin/knowledge/workspace.css"
            );
        }
    }

    private String read(String path)
            throws Exception {

        return Files.readString(
                Path.of(path)
        );
    }
}
