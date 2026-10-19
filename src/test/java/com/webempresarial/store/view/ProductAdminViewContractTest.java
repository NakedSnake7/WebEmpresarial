package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAdminViewContractTest {

    @Test
    void dashboardProductActionsMustRespectPermissions()
            throws IOException {

        String html = normalize(
                read("templates/admin/dashboard.html")
        );

        assertThat(html)
                .contains(
                        """
                        <div class="col-md-3" th:if="${canViewProducts}"> \
                        <div class="card border-0 shadow-sm h-100">
                        """.strip()
                )
                .contains(
                        """
                        <div class="col-sm-6 col-xl-3" \
                        th:if="${canCreateProducts}"> \
                        <a th:href="@{/nuevo}"
                        """.strip()
                )
                .contains(
                        """
                        <div class="col-sm-6 col-xl-3" \
                        th:if="${canViewProducts}"> \
                        <a th:href="@{/admin/productos}"
                        """.strip()
                )
                .contains(
                        """
                        <div class="col-sm-6 col-xl-3" \
                        th:if="${canEditProducts}"> \
                        <a th:href="@{/modificar-precios}"
                        """.strip()
                );
    }

    @Test
    void productCreationControlsMustRequireCreatePermission()
            throws IOException {

        String html = normalize(
                read("templates/admin/VerProductos.html")
        );

        assertThat(html)
                .contains(
                        """
                        <a th:href="@{/nuevo}" \
                        th:if="${canCreateProducts}" \
                        class="btn btn-primary w-100">
                        """.strip()
                )
                .contains(
                        """
                        <a th:href="@{/nuevo}" \
                        th:if="${canCreateProducts}" \
                        class="btn btn-primary"> \
                        Crear primer producto
                        """.strip()
                );
    }

    @Test
    void newProductMustAllowExistingCategoryWithoutManagePermission()
            throws IOException {

        String html = normalize(
                read("templates/admin/subirProducto.html")
        );

        int categorySelect =
                html.indexOf(
                        "<select id=\"categoriaSelect\""
                );

        int manageGuard =
                html.indexOf(
                        "<div th:if=\"${canManageCategories}\">"
                );

        int newCategoryLabel =
                html.indexOf(
                        "Crear nueva categoría"
                );

        assertThat(categorySelect)
                .isGreaterThanOrEqualTo(0);

        assertThat(manageGuard)
                .isGreaterThan(categorySelect);

        assertThat(newCategoryLabel)
                .isGreaterThan(manageGuard);

        assertThat(html)
                .contains(
                        "<select id=\"categoriaSelect\""
                )
                .contains(
                        "<div th:if=\"${canManageCategories}\">"
                )
                .contains(
                        "id=\"nuevaCategoria\""
                );
    }

    @Test
    void newProductCategoryJavascriptMustSupportMissingCreateInput()
            throws IOException {

        String html = normalize(
                read("templates/admin/subirProducto.html")
        );

        assertThat(html)
                .contains(
                        "categorySelect.value && newCategoryInput"
                )
                .contains(
                        "newCategoryInput ?.value ?.trim()"
                )
                .contains(
                        "Selecciona una categoría o escribe una nueva."
                )
                .contains(
                        "Selecciona una categoría."
                );
    }

    @Test
    void newProductMustAllowExistingBrandWithoutManagePermission()
            throws IOException {

        String html = normalize(
                read("templates/admin/subirProducto.html")
        );

        int brandSelect =
                html.indexOf(
                        "<select id=\"marcaSelect\""
                );

        int existingBrands =
                html.indexOf(
                        "th:each=\"marca : ${marcas}\""
                );

        int createBrand =
                html.indexOf(
                        "＋ Crear nueva marca"
                );

        int optionGuard =
                html.lastIndexOf(
                        "th:if=\"${canManageBrands}\"",
                        createBrand
                );

        int brandCreateBox =
                html.indexOf(
                        "id=\"brandCreateBox\""
                );

        int boxGuard =
                html.indexOf(
                        "th:if=\"${canManageBrands}\"",
                        brandCreateBox
                );

        assertThat(brandSelect)
                .isGreaterThanOrEqualTo(0);

        assertThat(existingBrands)
                .isGreaterThan(brandSelect);

        assertThat(optionGuard)
                .isGreaterThan(existingBrands);

        assertThat(createBrand)
                .isGreaterThan(optionGuard);

        assertThat(brandCreateBox)
                .isGreaterThan(createBrand);

        assertThat(boxGuard)
                .isGreaterThan(brandCreateBox);
    }

    @Test
    void newProductBrandJavascriptMustSupportMissingCreateControls()
            throws IOException {

        String html = normalize(
                read("templates/admin/subirProducto.html")
        );

        assertThat(html)
                .contains(
                        "!brandCreateBox || !newBrandInput"
                )
                .contains(
                        "brandSelect.value === \"-1\""
                )
                .contains(
                        "newBrandInput ?.value ?.trim()"
                )
                .contains(
                        "newBrandInput?.focus()"
                );
    }

    @Test
    void productEditControlsMustRequireEditPermission()
            throws IOException {

        String html = normalize(
                read("templates/admin/VerProductos.html")
        );

        assertThat(html)
                .contains(
                        """
                        <a th:href="@{/modificar-precios}" \
                        th:if="${canEditProducts}"
                        """.strip()
                )
                .contains(
                        """
                        <a th:href="@{/editar/{id}(id=${producto.id})}" \
                        th:if="${canEditProducts}"
                        """.strip()
                )
                .contains(
                        """
                        <button type="button" \
                        th:if="${canEditProducts}" \
                        class="btn btn-sm btn-outline-success toggle-promotion"
                        """.strip()
                )
                .contains(
                        """
                        <div class="form-check form-switch m-0" \
                        th:if="${canEditProducts}" \
                        onclick="event.stopPropagation()">
                        """.strip()
                )
                .contains(
                        """
                        <div class="visibility-switch" \
                        th:if="${canEditProducts}">
                        """.strip()
                );
    }

    @Test
    void productDeleteControlMustRequireDeletePermission()
            throws IOException {

        String html = normalize(
                read("templates/admin/VerProductos.html")
        );

        assertThat(html)
                .contains(
                        """
                        <button type="button" \
                        th:if="${canDeleteProducts}" \
                        class="btn btn-sm btn-outline-danger delete-product"
                        """.strip()
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
