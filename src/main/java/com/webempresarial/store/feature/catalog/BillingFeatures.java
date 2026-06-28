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
public class BillingFeatures implements PlatformModule {

    @Override
    public ModuleDefinition definition() {

        return ModuleDefinition.builder("Billing")

                .description("Servicios de facturación y pagos.")

                .feature(stripeConnect())

                .sidebarSection(
                        SidebarSectionDefinition.builder("Billing", "💳")
                                .item(
                                        "Stripe Connect",
                                        "💳",
                                        "/admin/store/settings",
                                        Feature.STRIPE_CONNECT
                                )
                                .build()
                )

                .build();
    }

    private FeatureDefinition stripeConnect() {

        return FeatureDefinition.builder(Feature.STRIPE_CONNECT)

                .displayName("Stripe Connect")
                .shortName("Stripe")
                .slug("stripe-connect")

                .description("Permite que la tienda reciba pagos directamente en su propia cuenta Stripe.")

                .category(FeatureCategory.BILLING)

                .icon("💳")
                .color("success")
                .url("/admin/store/settings")

                .healthWeight(8)
                .order(300)

                .accessPolicy(pro())

                .presentation(
                        FeaturePresentation.builder()
                                .showInSidebar(false)
                                .showInDashboard(true)
                                .showInBilling(true)
                                .showUpgradeCard(true)
                                .trackUsage(false)
                                .build()
                )

                .build();
    }

    private FeatureAccessPolicy pro() {

        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.PRO)
                .premium(true)
                .enabled(true)
                .build();
    }
}