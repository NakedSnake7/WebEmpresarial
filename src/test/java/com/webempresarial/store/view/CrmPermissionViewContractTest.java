package com.webempresarial.store.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrmPermissionViewContractTest {

    @Test
    void pipelineShouldExposePermissionContractToJavascript()
            throws IOException {

        String html = read(
                "src/main/resources/templates/crm/pipeline.html"
        );

        assertThat(html)
                .contains(
                        "th:data-can-manage-pipeline=\"${canManagePipeline}\""
                )
                .contains(
                        "th:data-can-edit-crm=\"${canEditCrm}\""
                )
                .contains(
                        "th:data-can-view-proposals=\"${canViewProposals}\""
                )
                .contains(
                        "th:data-can-create-proposals=\"${canCreateProposals}\""
                )
                .contains(
                        "th:data-can-edit-proposals=\"${canEditProposals}\""
                );
    }

    @Test
    void pipelineJavascriptShouldSupportReadOnlyMode()
            throws IOException {

        String js = read(
                "src/main/resources/static/assets/js/admin/crm/"
                + "pipeline.js"
        );

        assertThat(js)
                .contains("hasPipelinePermission")
                .contains("\"canManagePipeline\"")
                .contains("\"canEditCrm\"")
                .contains("\"canViewProposals\"")
                .contains("\"canCreateProposals\"")
                .contains("\"canEditProposals\"")
                .doesNotContain(
                        "card.draggable = true;"
                );
    }

    @Test
    void tasksShouldHideCompletionWithoutCrmEdit()
            throws IOException {

        String html = read(
                "src/main/resources/templates/crm/tasks.html"
        );

        String js = read(
                "src/main/resources/static/assets/js/admin/crm/"
                + "tasks.js"
        );

        assertThat(html)
                .contains(
                        "th:data-can-edit-crm=\"${canEditCrm}\""
                );

        assertThat(js)
                .contains("function canEditCrm()")
                .contains("\"crmTasksBoard\"")
                .contains("if (canEditCrm())")
                .contains("Solo lectura");
    }

    @Test
    void crmWorkspaceNavigationShouldRespectModulePermissions()
            throws IOException {

        String navigation = read(
                "src/main/resources/templates/admin/fragments/"
                + "crm-workspace.html"
        );

        assertThat(navigation)
                .contains(
                        "th:if=\"${canViewCrm}\""
                )
                .contains(
                        "th:if=\"${canViewPipeline}\""
                )
                .contains(
                        "th:href=\"@{/crm/dashboard}\""
                )
                .contains(
                        "th:href=\"@{/crm/pipeline}\""
                )
                .contains(
                        "th:href=\"@{/admin/leads}\""
                )
                .contains(
                        "th:href=\"@{/crm/tasks}\""
                )
                .doesNotContain(
                        "canViewAnalytics"
                )
                .doesNotContain(
                        "/crm/reports"
                );
    }

    @Test
    void leadDetailShouldSeparateCrmAndProposalPermissions()
            throws IOException {

        String html = read(
                "src/main/resources/templates/crm/"
                + "lead-detail.html"
        );

        assertThat(html)
                .contains(
                        "th:if=\"${canViewProposals}\""
                )
                .contains(
                        "th:if=\"${canEditCrm}\""
                )
                .contains(
                        ":: tabs('leads')"
                );
    }

    private String read(String path)
            throws IOException {

        return Files.readString(
                Path.of(path)
        );
    }
}
