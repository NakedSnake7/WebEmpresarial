package com.webempresarial.store.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeTemplateContractTest {

    @Test
    void upgradeActionsShouldRespectBillingPermissionsAndCsrf()
            throws IOException {

        String template = Files.readString(
                Path.of(
                        "src/main/resources/templates/"
                                + "admin/upgrade/index.html"
                )
        );

        assertThat(template)
                .contains(
                        "th:if=\"${canManageBilling and upgradeRequired}\""
                )
                .contains(
                        "th:action=\"@{/admin/billing/checkout}\""
                )
                .contains(
                        "fragments/csrf :: token"
                )
                .contains(
                        "th:if=\"${canViewBilling}\""
                )
                .contains(
                        "th:href=\"@{/admin/billing}\""
                );
    }
}
