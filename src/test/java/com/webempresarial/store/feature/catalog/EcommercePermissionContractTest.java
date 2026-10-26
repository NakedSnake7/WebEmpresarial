package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EcommercePermissionContractTest {

    @Test
    void productWorkspaceNavigationMustRequireProductView() {

        PlatformModuleDescriptor descriptor =
                new EcommerceFeatures().descriptor();

        List<SidebarItemDefinition> items =
                descriptor
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .toList();

        assertPermission(
                items,
                "Productos",
                AdminPermission.PRODUCT_VIEW
        );

        assertThat(items)
                .extracting(SidebarItemDefinition::title)
                .doesNotContain(
                        "Nuevo producto",
                        "Precios y promociones"
                );
    }

    @Test
    void productDashboardWidgetMustRequireProductView() {

        FeatureDefinition products =
                new EcommerceFeatures()
                        .descriptor()
                        .getFeatures()
                        .stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.PRODUCTS
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(
                products.getDashboardPermission()
        ).isEqualTo(
                AdminPermission.PRODUCT_VIEW
        );
    }

    @Test
    void inventoryWorkspaceNavigationMustRequireInventoryView() {

        PlatformModuleDescriptor descriptor =
                new EcommerceFeatures().descriptor();

        List<SidebarItemDefinition> items =
                descriptor
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .toList();

        assertPermission(
                items,
                "Inventario",
                AdminPermission.INVENTORY_VIEW
        );

        assertThat(items)
                .extracting(SidebarItemDefinition::title)
                .doesNotContain(
                        "Dashboard de inventario",
                        "Kardex de inventario",
                        "Alertas de inventario"
                );
    }

    @Test
    void inventoryDashboardWidgetsMustRequireInventoryView() {

        List<FeatureDefinition> features =
                new EcommerceFeatures()
                        .descriptor()
                        .getFeatures();

        FeatureDefinition inventory =
                features.stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.INVENTORY
                        )
                        .findFirst()
                        .orElseThrow();

        FeatureDefinition kardex =
                features.stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.INVENTORY_KARDEX
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(
                inventory.getDashboardPermission()
        ).isEqualTo(
                AdminPermission.INVENTORY_VIEW
        );

        assertThat(
                kardex.getDashboardPermission()
        ).isEqualTo(
                AdminPermission.INVENTORY_VIEW
        );
    }

    @Test
    void orderNavigationMustRequireOrderView() {

        PlatformModuleDescriptor descriptor =
                new EcommerceFeatures().descriptor();

        List<SidebarItemDefinition> items =
                descriptor.getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .toList();

        assertPermission(
                items,
                "Pedidos",
                AdminPermission.ORDER_VIEW
        );
    }

    @Test
    void orderDashboardWidgetMustRequireOrderView() {

        FeatureDefinition orders =
                new EcommerceFeatures()
                        .descriptor()
                        .getFeatures()
                        .stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.ORDERS
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(
                orders.getDashboardPermission()
        ).isEqualTo(
                AdminPermission.ORDER_VIEW
        );
    }

    @Test
    void categoryModuleMustNotExposeUnimplementedAdminNavigation() {

        PlatformModuleDescriptor descriptor =
                new EcommerceFeatures().descriptor();

        FeatureDefinition categories =
                descriptor
                        .getFeatures()
                        .stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.CATEGORIES
                        )
                        .findFirst()
                        .orElseThrow();

        boolean categoryAdminLink =
                descriptor
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .anyMatch(item ->
                                "/admin/categorias".equals(
                                        item.url()
                                )
                        );

        assertThat(
                categories
                        .getPresentation()
                        .isShowInDashboard()
        ).isFalse();

        assertThat(categoryAdminLink)
                .isFalse();
    }

    private void assertPermission(
            List<SidebarItemDefinition> items,
            String title,
            AdminPermission permission
    ) {

        SidebarItemDefinition item =
                items.stream()
                        .filter(candidate ->
                                title.equals(
                                        candidate.title()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(
                item.requiredPermission()
        ).isEqualTo(permission);
    }
}
