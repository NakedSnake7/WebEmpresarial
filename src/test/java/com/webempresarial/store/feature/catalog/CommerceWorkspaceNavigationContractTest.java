package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommerceWorkspaceNavigationContractTest {

    @Test
    void commerceSidebarShouldExposeOnlyTopLevelWorkspaces() {

        SidebarSectionDefinition commerce =
                new EcommerceFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .get(0);

        List<SidebarItemDefinition> items =
                commerce.getItems();

        assertThat(commerce.getTitle())
                .isEqualTo("Comercio");

        assertThat(items)
                .extracting(SidebarItemDefinition::title)
                .containsExactly(
                        "Productos",
                        "Pedidos",
                        "Inventario"
                );

        assertThat(items)
                .extracting(SidebarItemDefinition::url)
                .containsExactly(
                        "/admin/productos",
                        "/orders",
                        "/admin/inventory"
                );

        assertThat(items)
                .extracting(SidebarItemDefinition::feature)
                .containsExactly(
                        Feature.PRODUCTS,
                        Feature.ORDERS,
                        Feature.INVENTORY
                );

        assertThat(items)
                .extracting(
                        SidebarItemDefinition::requiredPermission
                )
                .containsExactly(
                        AdminPermission.PRODUCT_VIEW,
                        AdminPermission.ORDER_VIEW,
                        AdminPermission.INVENTORY_VIEW
                );
    }

    @Test
    void commerceSidebarShouldNotPromoteActionsOrSubsections() {

        List<String> titles =
                new EcommerceFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .get(0)
                        .getItems()
                        .stream()
                        .map(SidebarItemDefinition::title)
                        .toList();

        assertThat(titles)
                .doesNotContain(
                        "Nuevo producto",
                        "Precios y promociones",
                        "Dashboard de inventario",
                        "Kardex de inventario",
                        "Alertas de inventario"
                );
    }
}
