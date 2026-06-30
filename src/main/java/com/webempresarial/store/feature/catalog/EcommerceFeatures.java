package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureAccessPolicy; 
import com.webempresarial.store.feature.FeatureCategory;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.feature.ModuleDefinition;
import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Component;

@Component
public class EcommerceFeatures implements PlatformModule {

    @Override
    public ModuleDefinition definition() {
        return ModuleDefinition.builder("Ecommerce")
                .description("Módulos base para productos, inventario, pedidos y checkout.")
                .feature(products())
                .feature(categories())
                .feature(inventory())
                .feature(orders())
                .feature(checkout())
                .sidebarSection(
                        SidebarSectionDefinition.builder("Ecommerce", "🛒")
                                .item("Productos", "📦", "/admin/productos", Feature.PRODUCTS)
                                .item("Categorías", "🏷️", "/admin/categorias", Feature.CATEGORIES)
                                .item("Inventario", "📊", "/admin/productos", Feature.INVENTORY)
                                .item("Pedidos", "🧾", "/orders", Feature.ORDERS)
                                .build()
                )
          
                .build();
    }

    private FeatureDefinition products() {
        return FeatureDefinition.builder(Feature.PRODUCTS)
                .displayName("Productos")
                .description("Administra el catálogo de productos.")
                .category(FeatureCategory.ECOMMERCE)
                .icon("📦")
                .color("primary")
                .url("/admin/productos")
                .healthWeight(6)
                .order(10)
                .accessPolicy(basic())
                .presentation(standardBasic(true))
                .build();
    }

    private FeatureDefinition categories() {
        return FeatureDefinition.builder(Feature.CATEGORIES)
                .displayName("Categorías")
                .description("Organiza productos por categorías.")
                .category(FeatureCategory.ECOMMERCE)
                .icon("🏷️")
                .color("primary")
                .url("/admin/categorias")
                .healthWeight(3)
                .order(20)
                .accessPolicy(basic())
                .presentation(standardBasic(false))
                .build();
    }

    private FeatureDefinition inventory() {
        return FeatureDefinition.builder(Feature.INVENTORY)
                .displayName("Inventario")
                .description("Controla stock y disponibilidad.")
                .category(FeatureCategory.ECOMMERCE)
                .icon("📊")
                .color("primary")
                .url("/admin/productos")
                .healthWeight(5)
                .order(30)
                .accessPolicy(basic())
                .presentation(standardBasic(true))
                .build();
    }

    private FeatureDefinition orders() {
        return FeatureDefinition.builder(Feature.ORDERS)
                .displayName("Pedidos")
                .description("Gestiona pedidos de clientes.")
                .category(FeatureCategory.ECOMMERCE)
                .icon("🧾")
                .color("success")
                .url("/orders")
                .healthWeight(6)
                .order(40)
                .accessPolicy(basic())
                .presentation(standardBasic(true))
                .build();
    }

    private FeatureDefinition checkout() {
        return FeatureDefinition.builder(Feature.CHECKOUT)
                .displayName("Checkout")
                .description("Permite pagos y finalización de compra.")
                .category(FeatureCategory.ECOMMERCE)
                .icon("🛒")
                .color("success")
                .url("/checkout")
                .healthWeight(5)
                .order(50)
                .accessPolicy(basic())
                .presentation(
                        FeaturePresentation.builder()
                                .showInSidebar(false)
                                .showInDashboard(false)
                                .showInBilling(true)
                                .showUpgradeCard(false)
                                .trackUsage(true)
                                .build()
                )
                .build();
    }

    private FeatureAccessPolicy basic() {
        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.BASIC)
                .premium(false)
                .enabled(true)
                .build();
    }

    private FeaturePresentation standardBasic(boolean showInDashboard) {
        return FeaturePresentation.builder()
                .showInSidebar(true)
                .showInDashboard(showInDashboard)
                .showInBilling(true)
                .showUpgradeCard(false)
                .trackUsage(true)
                .build();
    }
}