package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SaasWorkspaceViewContractTest {

    @Test
    void saasViewsShouldUseSharedWorkspace()
            throws Exception {

        assertTab(
                "saas/dashboard.html",
                ":: tabs('summary')"
        );

        assertTab(
                "store/list.html",
                ":: tabs('stores')"
        );

        assertTab(
                "store/form.html",
                ":: tabs('stores')"
        );

        assertTab(
                "store/admins/list.html",
                ":: tabs('stores')"
        );

        assertTab(
                "store/admins/form.html",
                ":: tabs('stores')"
        );

        assertTab(
                "subscriptions/list.html",
                ":: tabs('subscriptions')"
        );
    }

    @Test
    void workspaceShouldExposeSaasAdministrationSections()
            throws Exception {

        String html = Files.readString(
                Path.of(
                        "src/main/resources/templates/admin/"
                        + "fragments/saas-workspace.html"
                )
        );

        assertThat(html)
                .contains(
                        "href=\"/admin/saas\""
                )
                .contains(
                        "href=\"/admin/stores\""
                )
                .contains(
                        "href=\"/admin/subscriptions\""
                )
                .doesNotContain(
                        "/admin/marketplace"
                );
    }

    private void assertTab(
            String file,
            String expected
    ) throws Exception {

        String html = Files.readString(
                Path.of(
                        "src/main/resources/templates/admin/"
                        + file
                )
        );

        assertThat(html)
                .as(file)
                .contains(expected);
    }
}
