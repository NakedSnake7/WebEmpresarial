package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryAdminViewContractTest {

    @Test
    void stockAdjustmentMustRequireInventoryEdit()
            throws IOException {

        String html = normalize(
                read(
                        "templates/admin/inventory/"
                        + "product-detail.html"
                )
        );

        assertThat(html)
                .contains(
                        """
                        <form th:if="${canEditInventory}" \
                        th:action="@{/admin/productos/{id}/inventory/adjust(
                        """.strip()
                );
    }

    @Test
    void alertAcknowledgeMustRequireInventoryEdit()
            throws IOException {

        String html = normalize(
                read(
                        "templates/admin/inventory/"
                        + "alerts.html"
                )
        );

        assertThat(html)
                .contains(
                        """
                        <form th:if="${canEditInventory \
                        and alert.status.name() == 'OPEN'}" \
                        th:action="@{/admin/inventory/alerts/{id}/acknowledge(
                        """.strip()
                );
    }

    @Test
    void productInventoryDetailMustGuardProductCatalogLink()
            throws IOException {

        assertRootProductLinksGuarded(
                "templates/admin/inventory/product-detail.html"
        );
    }

    private void assertRootProductLinksGuarded(
            String resource
    ) throws IOException {

        String html = normalize(
                read(resource)
        );

        String link =
                "<a th:if=\"${canViewProducts}\" "
                + "th:href=\"@{/admin/productos}\"";

        assertThat(html)
                .as(resource)
                .contains(link);

        /*
         * Evita que reaparezca un enlace directo al catálogo
         * sin la authority PRODUCT_VIEW.
         */
        assertThat(html)
                .as(resource)
                .doesNotContain(
                        "<a th:href=\"@{/admin/productos}\""
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
