package com.webempresarial.store.feature.catalog;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformWhiteLabelContractTest {

    @Test
    void whiteLabelShouldRequireDedicatedPermissionOnDashboard()
            throws Exception {

        String source = Files.readString(
                Path.of(
                        "src/main/java/com/webempresarial/store/"
                        + "feature/catalog/PlatformFeatures.java"
                )
        );

        int start = source.indexOf(
                "private FeatureDefinition whiteLabelFull()"
        );

        int end = source.indexOf(
                "private FeatureDefinition multiUser()",
                start
        );

        assertThat(start)
                .isGreaterThanOrEqualTo(0);

        assertThat(end)
                .isGreaterThan(start);

        String whiteLabel =
                source.substring(start, end);

        assertThat(whiteLabel)
                .contains(
                        ".url(\"/admin/store/settings\")"
                )
                .contains(
                        ".accessPolicy(premium())"
                )
                .contains(
                        ".presentation(hiddenFromSidebar(true, false))"
                )
                .contains(
                        ".dashboardPermission("
                )
                .contains(
                        "AdminPermission.WHITE_LABEL_MANAGE"
                );
    }
}
