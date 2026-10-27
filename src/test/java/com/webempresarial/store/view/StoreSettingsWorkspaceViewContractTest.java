package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StoreSettingsWorkspaceViewContractTest {

    @Test
    void workspaceShouldExposeExpectedSubsections()
            throws Exception {

        String navigation = normalize(
                read(
                    "src/main/resources/templates/admin/fragments/"
                    + "settings-workspace.html"
                )
        );

        assertThat(navigation)
                .contains(
                        "th:fragment=\"tabs(activeTab)\""
                )
                .contains("General")
                .contains("Identidad")
                .contains("Información comercial")
                .contains("Pagos")
                .contains("CRM")
                .contains("Avanzado")
                .contains(
                        "th:if=\"${canManageStoreSettings}\""
                )
                .contains(
                        "th:if=\"${canManageWhiteLabel}\""
                );
    }

    @Test
    void settingsShouldExposeWorkspaceAnchors()
            throws Exception {

        String html = read(
                "src/main/resources/templates/admin/store/"
                + "settings.html"
        );

        assertThat(html)
                .contains("id=\"general\"")
                .contains("id=\"identity\"")
                .contains("id=\"commercial\"")
                .contains("id=\"payments\"")
                .contains("id=\"advanced\"")
                .contains(
                        ":: tabs(${settingsTab})"
                );
    }

    @Test
    void everyCrmSettingsViewShouldUseSettingsWorkspace()
            throws Exception {

        for (String file : new String[] {
                "index.html",
                "budget-ranges.html",
                "lead-sources.html",
                "pipeline.html",
                "scoring.html",
                "duplicate.html",
                "merge.html",
                "automation.html",
                "ai.html",
                "settings.html"
        }) {

            String html = read(
                    "src/main/resources/templates/admin/store/crm/"
                    + file
            );

            assertThat(html)
                    .as(file)
                    .contains(
                            ":: tabs('crm')"
                    );
        }
    }

    @Test
    void advancedShouldBeIndependentFromStoreEditFieldset()
            throws Exception {

        String html = read(
                "src/main/resources/templates/admin/store/"
                + "settings.html"
        );

        int general =
                html.indexOf(
                        "data-store-settings-general"
                );

        int generalClose =
                html.indexOf(
                        "</fieldset>",
                        general
                );

        int advanced =
                html.indexOf(
                        "id=\"advanced\""
                );

        int commercial =
                html.indexOf(
                        "id=\"commercial\""
                );

        assertThat(general)
                .isGreaterThanOrEqualTo(0);

        assertThat(generalClose)
                .isGreaterThan(general);

        assertThat(advanced)
                .isGreaterThan(generalClose);

        assertThat(commercial)
                .isGreaterThan(advanced);

        assertThat(html)
                .contains(
                        "canManageWhiteLabel and "
                        + "features.canUse(store, "
                        + "'WHITE_LABEL_FULL')"
                );
    }

    @Test
    void whiteLabelManagerShouldSeeMainSaveAction()
            throws Exception {

        String html = normalize(
                read(
                    "src/main/resources/templates/admin/store/"
                    + "settings.html"
                )
        );

        assertThat(html)
                .contains(
                        "th:if=\"${canManageStoreSettings or "
                        + "canManageStoreBranding or "
                        + "canManageCommercialInfo or "
                        + "canManageWhiteLabel}\""
                );
    }

    @Test
    void domainAndBillingShouldRemainSeparate()
            throws Exception {

        String navigation = read(
                "src/main/resources/templates/admin/fragments/"
                + "settings-workspace.html"
        );

        assertThat(navigation)
                .doesNotContain("/admin/domains")
                .doesNotContain("/admin/billing");
    }

    private String read(String path)
            throws Exception {

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
