package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureAccessPolicy; 
import com.webempresarial.store.feature.FeatureCategory;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.health.checks.KernelHealthCheck;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Component;

@Component
public class PlatformFeatures implements PlatformModule {

    @Override
    public PlatformModuleDescriptor descriptor() {
        return PlatformModuleDescriptor.builder("Platform")
                .description("Módulos de plataforma, reportes, dominio, usuarios y white label.")
                .healthCheck(KernelHealthCheck.class)

                .feature(analytics())
                .feature(customDomain())
                .feature(whiteLabelFull())
                .feature(multiUser())
                .feature(apiAccess())

                .sidebarSection(
                        SidebarSectionDefinition.builder("Plataforma", "🧩")
                                .item("Analytics", "📈", "/crm/reports", Feature.ANALYTICS)
                                .item("Dominio", "🌐", "/admin/domains", Feature.CUSTOM_DOMAIN)
                                .item("Usuarios", "👥", "/admin/users", Feature.MULTI_USER)
                                .build()
                )

                .build();
    }

    private FeatureDefinition analytics() {
        return FeatureDefinition.builder(Feature.ANALYTICS)
                .displayName("Analytics")
                .shortName("Analytics")
                .slug("analytics")
                .description("Consulta reportes y métricas avanzadas.")
                .category(FeatureCategory.PLATFORM)
                .icon("📈")
                .color("info")
                .url("/crm/reports")
                .healthWeight(5)
                .order(150)
                .accessPolicy(pro())
                .presentation(visible(true, true, true))
                .build();
    }

    private FeatureDefinition customDomain() {
        return FeatureDefinition.builder(Feature.CUSTOM_DOMAIN)
                .displayName("Dominio personalizado")
                .shortName("Dominio")
                .slug("custom-domain")
                .description("Conecta un dominio propio.")
                .category(FeatureCategory.PLATFORM)
                .icon("🌐")
                .color("info")
                .url("/admin/domains")
                .healthWeight(8)
                .order(310)
                .accessPolicy(pro())
                .presentation(visible(true, true, false))
                .build();
    }

    private FeatureDefinition whiteLabelFull() {
        return FeatureDefinition.builder(Feature.WHITE_LABEL_FULL)
                .displayName("White Label Full")
                .shortName("White Label")
                .slug("white-label-full")
                .description("Personalización avanzada con CSS, JS y tracking.")
                .category(FeatureCategory.PLATFORM)
                .icon("🎨")
                .color("dark")
                .url("/admin/store/settings")
                .healthWeight(8)
                .order(320)
                .accessPolicy(premium())
                .presentation(hiddenFromSidebar(true, false))
                .build();
    }

    private FeatureDefinition multiUser() {
        return FeatureDefinition.builder(Feature.MULTI_USER)
                .displayName("Multiusuario")
                .shortName("Usuarios")
                .slug("multi-user")
                .description("Permite múltiples usuarios por tienda.")
                .category(FeatureCategory.PLATFORM)
                .icon("👥")
                .color("info")
                .url("/admin/users")
                .healthWeight(4)
                .order(330)
                .accessPolicy(premium())
                .presentation(visible(true, false, false))
                .build();
    }

    private FeatureDefinition apiAccess() {
        return FeatureDefinition.builder(Feature.API_ACCESS)
                .displayName("API Access")
                .shortName("API")
                .slug("api-access")
                .description("Acceso API para integraciones externas.")
                .category(FeatureCategory.PLATFORM)
                .icon("🔌")
                .color("dark")
                .url("/admin/api")
                .healthWeight(4)
                .order(340)
                .accessPolicy(premium())
                .presentation(hiddenFromSidebar(false, false))
                .build();
    }

    private FeatureAccessPolicy pro() {
        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.PRO)
                .premium(true)
                .enabled(true)
                .build();
    }

    private FeatureAccessPolicy premium() {
        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.PREMIUM)
                .premium(true)
                .enabled(true)
                .build();
    }

    private FeaturePresentation visible(
            boolean showInDashboard,
            boolean showUpgradeCard,
            boolean trackUsage
    ) {
        return FeaturePresentation.builder()
                .showInSidebar(true)
                .showInDashboard(showInDashboard)
                .showInBilling(true)
                .showUpgradeCard(showUpgradeCard)
                .trackUsage(trackUsage)
                .build();
    }

    private FeaturePresentation hiddenFromSidebar(
            boolean showInDashboard,
            boolean trackUsage
    ) {
        return FeaturePresentation.builder()
                .showInSidebar(false)
                .showInDashboard(showInDashboard)
                .showInBilling(true)
                .showUpgradeCard(true)
                .trackUsage(trackUsage)
                .build();
    }
}