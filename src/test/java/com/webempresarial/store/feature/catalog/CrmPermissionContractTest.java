package com.webempresarial.store.feature.catalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrmPermissionContractTest {

    private static final Path SOURCE =
            Path.of(
                    "src/main/java/com/webempresarial/store/"
                    + "feature/catalog/CrmFeatures.java"
            );

    @Test
    void crmSidebarShouldUsePermissionsInsteadOfLegacyRoles()
            throws IOException {

        String source = Files.readString(SOURCE);

        String normalized =
                source.replaceAll(
                        "\\s+",
                        " "
                );

        assertThat(source)
                .doesNotContain("AdminRole.");

        assertThat(normalized)
                .contains(
                        "Feature.CRM, AdminPermission.CRM_VIEW"
                )
                .contains(
                        "Feature.PIPELINE, AdminPermission.PIPELINE_VIEW"
                )
                .contains(
                        "Feature.LEADS, AdminPermission.CRM_VIEW"
                )
                .contains(
                        "Feature.TASKS, AdminPermission.CRM_VIEW"
                );
    }

    @Test
    void dashboardCardsShouldUseTheirRealPermissions()
            throws IOException {

        String source = Files.readString(SOURCE);

        assertThat(
                featureBlock(
                        source,
                        "crm",
                        "leads"
                )
        )
        .contains("AdminPermission.CRM_VIEW");

        assertThat(
                featureBlock(
                        source,
                        "leads",
                        "pipeline"
                )
        )
        .contains("AdminPermission.CRM_VIEW");

        assertThat(
                featureBlock(
                        source,
                        "pipeline",
                        "tasks"
                )
        )
        .contains("AdminPermission.PIPELINE_VIEW");

        assertThat(
                featureBlock(
                        source,
                        "tasks",
                        "proposals"
                )
        )
        .contains("AdminPermission.CRM_VIEW");

        String proposals =
                proposalsBlock(source);

        assertThat(proposals)
                .contains(
                        "AdminPermission.PROPOSAL_VIEW"
                )
                .contains(
                        ".presentation(standard(false))"
                )
                .doesNotContain(
                        ".presentation(standard(true))"
                );
    }

    private String featureBlock(
            String source,
            String method,
            String nextMethod
    ) {

        String startMarker =
                "private FeatureDefinition "
                + method
                + "()";

        String endMarker =
                "private FeatureDefinition "
                + nextMethod
                + "()";

        int start = source.indexOf(startMarker);
        int end = source.indexOf(
                endMarker,
                start
        );

        assertThat(start)
                .isGreaterThanOrEqualTo(0);

        assertThat(end)
                .isGreaterThan(start);

        return source.substring(
                start,
                end
        );
    }

    private String proposalsBlock(
            String source
    ) {

        String startMarker =
                "private FeatureDefinition proposals()";

        String endMarker =
                "private FeatureAccessPolicy pro()";

        int start = source.indexOf(startMarker);
        int end = source.indexOf(
                endMarker,
                start
        );

        assertThat(start)
                .isGreaterThanOrEqualTo(0);

        assertThat(end)
                .isGreaterThan(start);

        return source.substring(
                start,
                end
        );
    }
}
