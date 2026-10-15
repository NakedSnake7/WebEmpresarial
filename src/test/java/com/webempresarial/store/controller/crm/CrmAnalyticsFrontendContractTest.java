package com.webempresarial.store.controller.crm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrmAnalyticsFrontendContractTest {

    @Test
    void reportsNavigationShouldRequireAnalyticsPermission()
            throws IOException {

        String sidebar = Files.readString(
                Path.of(
                        "src/main/resources/templates/"
                                + "crm/crm-sidebar.html"
                )
        );

        assertThat(sidebar)
                .contains(
                        "th:if=\"${canViewAnalytics}\""
                )
                .contains(
                        "th:href=\"@{/crm/reports}\""
                );
    }
}
