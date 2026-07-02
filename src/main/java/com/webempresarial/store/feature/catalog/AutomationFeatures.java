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
public class AutomationFeatures implements PlatformModule {

    @Override
    public PlatformModuleDescriptor descriptor() {
        return PlatformModuleDescriptor.builder("Automation")
                .description("Automatizaciones para ventas, WhatsApp, seguimiento y flujos comerciales.")

                .feature(whatsappAutomation())
                .feature(automations())

                .sidebarSection(
                        SidebarSectionDefinition.builder("Automatización", "⚡")
                                .item("WhatsApp", "💬", "/admin/whatsapp", Feature.WHATSAPP_AUTOMATION)
                                .item("Automatizaciones", "⚡", "/admin/automations", Feature.AUTOMATIONS)
                                .build()
                )

                .build();
    }

    private FeatureDefinition whatsappAutomation() {
        return FeatureDefinition.builder(Feature.WHATSAPP_AUTOMATION)
                .displayName("WhatsApp Automation")
                .shortName("WhatsApp")
                .slug("whatsapp-automation")
                .description("Automatiza mensajes por WhatsApp.")
                .category(FeatureCategory.AUTOMATION)
                .icon("💬")
                .color("success")
                .url("/admin/whatsapp")
                .healthWeight(5)
                .order(220)
                .accessPolicy(premium())
                .presentation(premiumPresentation())
                .build();
    }

    private FeatureDefinition automations() {
        return FeatureDefinition.builder(Feature.AUTOMATIONS)
                .displayName("Automatizaciones")
                .shortName("Automations")
                .slug("automations")
                .description("Crea flujos automáticos para ventas y seguimiento.")
                .category(FeatureCategory.AUTOMATION)
                .icon("⚡")
                .color("warning")
                .url("/admin/automations")
                .healthWeight(8)
                .order(230)
                .accessPolicy(premium())
                .presentation(premiumPresentation())
                .build();
    }

    private FeatureAccessPolicy premium() {
        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.PREMIUM)
                .premium(true)
                .enabled(true)
                .build();
    }

    private FeaturePresentation premiumPresentation() {
        return FeaturePresentation.builder()
                .showInSidebar(true)
                .showInDashboard(true)
                .showInBilling(true)
                .showUpgradeCard(true)
                .trackUsage(true)
                .build();
    }
}