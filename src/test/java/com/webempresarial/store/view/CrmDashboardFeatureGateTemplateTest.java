package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CrmDashboardFeatureGateTemplateTest {

    private static final Path DASHBOARD =
            Path.of(
                    "src/main/resources/templates/crm/"
                    + "dashboard.html"
            );

    @Test
    void dashboardMustNotUseLegacyPlanFeatureServiceGuard()
            throws Exception {

        String html = Files.readString(DASHBOARD);

        assertThat(html)
                .doesNotContain(
                        "features.canUseCRM(store)"
                )
                .contains(
                        "data-crm-view=\"dashboard\""
                )
                .contains(
                        ":: tabs('summary')"
                );
    }
}
