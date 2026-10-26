package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CrmWorkspaceViewContractTest {

    @Test
    void crmWorkspaceShouldExposeFourOperationalTabs()
            throws IOException {

        String navigation = normalize(
                read(
                    "src/main/resources/templates/admin/fragments/"
                    + "crm-workspace.html"
                )
        );

        assertThat(navigation)
                .contains(
                        "th:fragment=\"tabs(activeTab)\""
                )
                .contains("Resumen")
                .contains("Pipeline")
                .contains("Leads")
                .contains("Tareas")
                .contains(
                        "th:if=\"${canViewCrm}\""
                )
                .contains(
                        "th:if=\"${canViewPipeline}\""
                )
                .doesNotContain(
                        "Reportes"
                )
                .doesNotContain(
                        "/crm/reports"
                );
    }

    @Test
    void crmViewsShouldSelectTheirWorkspaceTab()
            throws IOException {

        assertThat(
                read(
                    "src/main/resources/templates/crm/"
                    + "dashboard.html"
                )
        ).contains(
                ":: tabs('summary')"
        );

        assertThat(
                read(
                    "src/main/resources/templates/crm/"
                    + "pipeline.html"
                )
        ).contains(
                ":: tabs('pipeline')"
        );

        assertThat(
                read(
                    "src/main/resources/templates/admin/leads/"
                    + "index.html"
                )
        ).contains(
                ":: tabs('leads')"
        );

        assertThat(
                read(
                    "src/main/resources/templates/crm/"
                    + "lead-detail.html"
                )
        ).contains(
                ":: tabs('leads')"
        );

        assertThat(
                read(
                    "src/main/resources/templates/crm/"
                    + "tasks.html"
                )
        ).contains(
                ":: tabs('tasks')"
        );
    }

    @Test
    void crmHeadersShouldNotDuplicateWorkspaceNavigation()
            throws IOException {

        String dashboard = read(
                "src/main/resources/templates/crm/dashboard.html"
        );

        String pipeline = read(
                "src/main/resources/templates/crm/pipeline.html"
        );

        String tasks = read(
                "src/main/resources/templates/crm/tasks.html"
        );

        String leads = read(
                "src/main/resources/templates/admin/leads/index.html"
        );

        String leadDetail = read(
                "src/main/resources/templates/crm/lead-detail.html"
        );

        assertThat(dashboard)
                .doesNotContain(
                        "class=\"crm-header-actions\""
                );

        assertThat(pipeline)
                .doesNotContain(
                        "class=\"crm-header-actions\""
                );

        assertThat(tasks)
                .doesNotContain(
                        "class=\"crm-header-actions\""
                );

        assertThat(leadDetail)
                .doesNotContain(
                        "class=\"crm-header-actions\""
                );

        assertThat(leads)
                .doesNotContain(
                        "Abrir CRM Pipeline"
                )
                .doesNotContain(
                        "CRM Legacy"
                );
    }

    @Test
    void legacyCrmSidebarShouldBeRemoved() {

        assertThat(
                Path.of(
                    "src/main/resources/templates/crm/"
                    + "crm-sidebar.html"
                )
        ).doesNotExist();
    }

    private String read(String path)
            throws IOException {

        return Files.readString(
                Path.of(path)
        );
    }

    private String normalize(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}
