package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CommerceWorkspaceViewContractTest {

    @Test
    void productWorkspaceMustUseCatalogAndPricesTabs()
            throws IOException {

        String fragment = normalize(
                read(
                    "templates/admin/fragments/"
                    + "commerce-workspace.html"
                )
        );

        String catalog = normalize(
                read("templates/admin/VerProductos.html")
        );

        String prices = normalize(
                read("templates/admin/modificar-precios.html")
        );

        assertThat(fragment)
                .contains(
                    "th:fragment=\"productTabs(activeTab)\""
                )
                .contains(
                    "th:if=\"${canViewProducts}\""
                )
                .contains(
                    "th:href=\"@{/admin/productos}\""
                )
                .contains("Catálogo")
                .contains(
                    "th:if=\"${canEditProducts}\""
                )
                .contains(
                    "th:href=\"@{/modificar-precios}\""
                )
                .contains("Precios");

        assertThat(catalog)
                .contains(
                    ":: productTabs('catalog')"
                )
                .doesNotContain(
                    "💲 Precios"
                );

        assertThat(prices)
                .contains(
                    ":: productTabs('prices')"
                )
                .doesNotContain(
                    "← Productos"
                )
                .doesNotContain(
                    "Política de envío"
                )
                .doesNotContain(
                    "/admin/store/settings/shipping"
                )
                .contains(
                    "th:if=\"${canCreateProducts}\""
                );
    }

    @Test
    void inventoryWorkspaceMustUseSummaryMovementsAndAlertsTabs()
            throws IOException {

        String fragment = normalize(
                read(
                    "templates/admin/fragments/"
                    + "commerce-workspace.html"
                )
        );

        assertThat(fragment)
                .contains(
                    "th:fragment=\"inventoryTabs(activeTab)\""
                )
                .contains("Resumen")
                .contains("Movimientos")
                .contains("Alertas")
                .contains(
                    "features.canUse("
                )
                .contains(
                    "'INVENTORY_KARDEX'"
                )
                .contains(
                    "feature='INVENTORY_KARDEX'"
                )
                .containsPattern(
                    "sidebarBadges\\.containsKey\\(\\s*"
                    + "'/admin/inventory'"
                    + "\\s*\\)"
                )
                .containsPattern(
                    "sidebarBadges\\.get\\(\\s*"
                    + "'/admin/inventory'"
                    + "\\s*\\)"
                );

        assertThat(
                normalize(
                    read(
                        "templates/admin/inventory/"
                        + "dashboard.html"
                    )
                )
        ).contains(
                ":: inventoryTabs('summary')"
        );

        assertThat(
                normalize(
                    read(
                        "templates/admin/inventory/"
                        + "movements.html"
                    )
                )
        ).contains(
                ":: inventoryTabs('movements')"
        );

        assertThat(
                normalize(
                    read(
                        "templates/admin/inventory/"
                        + "alerts.html"
                    )
                )
        ).contains(
                ":: inventoryTabs('alerts')"
        );
    }

    @Test
    void inventoryHeadersMustNotDuplicateWorkspaceNavigation()
            throws IOException {

        String dashboard = normalize(
                read(
                    "templates/admin/inventory/dashboard.html"
                )
        );

        String movements = normalize(
                read(
                    "templates/admin/inventory/movements.html"
                )
        );

        String alerts = normalize(
                read(
                    "templates/admin/inventory/alerts.html"
                )
        );

        assertThat(dashboard)
                .doesNotContain(
                    "Gestionar productos"
                )
                .doesNotContain(
                    "Ver Kardex"
                );

        assertThat(movements)
                .doesNotContain(
                    "> Dashboard </a>"
                );

        assertThat(alerts)
                .doesNotContain(
                    "Ver Kardex"
                )
                .doesNotContain(
                    "> Productos </a>"
                )
                .doesNotContain(
                    "Volver al dashboard"
                );
    }

    private String read(
            String resource
    ) throws IOException {

        return new ClassPathResource(
                resource
        ).getContentAsString(
                StandardCharsets.UTF_8
        );
    }

    private String normalize(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}
