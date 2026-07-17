package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureAccessPolicy; 
import com.webempresarial.store.feature.FeatureCategory;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Component;

@Component
public class MarketingFeatures implements PlatformModule {

    @Override
    public PlatformModuleDescriptor descriptor() {
        return PlatformModuleDescriptor.builder("Marketing")
                .description("Módulos para reseñas, cupones y campañas comerciales.")

                .feature(reviews())
                .feature(coupons())
                .feature(emailMarketing())

                .sidebarSection(
                        SidebarSectionDefinition.builder("Marketing", "📣")
                                .item("Reseñas", "⭐", "/resenas", Feature.REVIEWS)
                                .item("Cupones", "🏷️", "/admin/coupons", Feature.COUPONS)
                                .item("Email Marketing", "✉️", "/admin/email-marketing", Feature.EMAIL_MARKETING)
                                .build()
                )

                .build();
    }

    private FeatureDefinition reviews() {
        return FeatureDefinition.builder(Feature.REVIEWS)
                .displayName("Reseñas")
                .shortName("Reseñas")
                .slug("reviews")
                .description("Muestra opiniones de clientes.")
                .category(FeatureCategory.MARKETING)
                .icon("⭐")
                .color("warning")
                .url("/resenas/nueva")
                .healthWeight(4)
                .order(60)
                .accessPolicy(basic())
                .presentation(basicPresentation(true))
                .build();
    }

    private FeatureDefinition coupons() {
        return FeatureDefinition.builder(Feature.COUPONS)
                .displayName("Cupones")
                .shortName("Cupones")
                .slug("coupons")
                .description("Crea descuentos y promociones.")
                .category(FeatureCategory.MARKETING)
                .icon("🏷️")
                .color("warning")
                .url("/admin/coupons")
                .healthWeight(3)
                .order(200)
                .accessPolicy(pro())
                .presentation(premiumPresentation(false))
                .build();
    }

    private FeatureDefinition emailMarketing() {
        return FeatureDefinition.builder(Feature.EMAIL_MARKETING)
                .displayName("Email Marketing")
                .shortName("Email")
                .slug("email-marketing")
                .description("Envía campañas y seguimientos por correo.")
                .category(FeatureCategory.MARKETING)
                .icon("✉️")
                .color("warning")
                .url("/admin/email-marketing")
                .healthWeight(5)
                .order(210)
                .accessPolicy(premium())
                .presentation(premiumPresentation(true))
                .build();
    }

    private FeatureAccessPolicy basic() {
        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.BASIC)
                .premium(false)
                .enabled(true)
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

    private FeaturePresentation basicPresentation(boolean showInDashboard) {
        return FeaturePresentation.builder()
                .showInSidebar(true)
                .showInDashboard(showInDashboard)
                .showInBilling(true)
                .showUpgradeCard(false)
                .trackUsage(true)
                .build();
    }

    private FeaturePresentation premiumPresentation(boolean showInDashboard) {
        return FeaturePresentation.builder()
                .showInSidebar(true)
                .showInDashboard(showInDashboard)
                .showInBilling(true)
                .showUpgradeCard(true)
                .trackUsage(true)
                .build();
    }
}