package com.webempresarial.store.feature;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class FeatureRegistryConfiguration {

    private final FeatureRegistry registry;

    public FeatureRegistryConfiguration(FeatureRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void registerFeatures() {

        register(
                Feature.PRODUCTS,
                "Productos",
                "Administra el catálogo de productos.",
                FeatureCategory.ECOMMERCE,
                StorePlan.BASIC,
                "📦",
                "primary",
                "/admin/productos",
                6,
                false
        );

        register(
                Feature.CATEGORIES,
                "Categorías",
                "Organiza productos por categorías.",
                FeatureCategory.ECOMMERCE,
                StorePlan.BASIC,
                "🏷️",
                "primary",
                "/admin/categorias",
                3,
                false
        );

        register(
                Feature.INVENTORY,
                "Inventario",
                "Controla stock y disponibilidad.",
                FeatureCategory.ECOMMERCE,
                StorePlan.BASIC,
                "📊",
                "primary",
                "/admin/productos",
                5,
                false
        );

        register(
                Feature.ORDERS,
                "Pedidos",
                "Gestiona pedidos de clientes.",
                FeatureCategory.ECOMMERCE,
                StorePlan.BASIC,
                "🧾",
                "success",
                "/orders",
                6,
                false
        );

        register(
                Feature.CHECKOUT,
                "Checkout",
                "Permite pagos y finalización de compra.",
                FeatureCategory.ECOMMERCE,
                StorePlan.BASIC,
                "🛒",
                "success",
                "/checkout",
                5,
                false
        );

        register(
                Feature.REVIEWS,
                "Reseñas",
                "Muestra opiniones de clientes.",
                FeatureCategory.MARKETING,
                StorePlan.BASIC,
                "⭐",
                "warning",
                "/resenas/nueva",
                4,
                false
        );

        register(
                Feature.CRM,
                "CRM",
                "Gestiona leads y oportunidades comerciales.",
                FeatureCategory.CRM,
                StorePlan.PRO,
                "📊",
                "primary",
                "/crm/dashboard",
                8,
                true
        );

        register(
                Feature.LEADS,
                "Leads",
                "Captura y administra prospectos.",
                FeatureCategory.CRM,
                StorePlan.PRO,
                "🗂️",
                "primary",
                "/admin/leads",
                6,
                true
        );

        register(
                Feature.TASKS,
                "Tareas",
                "Gestiona seguimiento comercial.",
                FeatureCategory.CRM,
                StorePlan.PRO,
                "✅",
                "primary",
                "/crm/tasks",
                4,
                true
        );

        register(
                Feature.PIPELINE,
                "Pipeline",
                "Visualiza oportunidades por etapa.",
                FeatureCategory.CRM,
                StorePlan.PRO,
                "🎯",
                "primary",
                "/crm/pipeline",
                6,
                true
        );

        register(
                Feature.PROPOSALS,
                "Propuestas",
                "Genera propuestas comerciales.",
                FeatureCategory.CRM,
                StorePlan.PRO,
                "📄",
                "primary",
                "/crm/proposals",
                6,
                true
        );

        register(
                Feature.COUPONS,
                "Cupones",
                "Crea descuentos y promociones.",
                FeatureCategory.MARKETING,
                StorePlan.PRO,
                "🏷️",
                "warning",
                "/admin/coupons",
                3,
                true
        );

        register(
                Feature.STRIPE_CONNECT,
                "Stripe Connect",
                "Permite que la tienda reciba pagos propios.",
                FeatureCategory.BILLING,
                StorePlan.PRO,
                "💳",
                "success",
                "/admin/store/settings",
                8,
                true
        );

        register(
                Feature.ANALYTICS,
                "Analytics",
                "Consulta reportes y métricas avanzadas.",
                FeatureCategory.PLATFORM,
                StorePlan.PRO,
                "📈",
                "info",
                "/crm/reports",
                5,
                true
        );

        register(
                Feature.CUSTOM_DOMAIN,
                "Dominio personalizado",
                "Conecta un dominio propio.",
                FeatureCategory.PLATFORM,
                StorePlan.PRO,
                "🌐",
                "info",
                "/admin/domains",
                8,
                true
        );

        register(
                Feature.EMAIL_MARKETING,
                "Email Marketing",
                "Envía campañas y seguimientos por correo.",
                FeatureCategory.MARKETING,
                StorePlan.PREMIUM,
                "✉️",
                "warning",
                "/admin/email-marketing",
                5,
                true
        );

        register(
                Feature.WHATSAPP_AUTOMATION,
                "WhatsApp Automation",
                "Automatiza mensajes por WhatsApp.",
                FeatureCategory.AUTOMATION,
                StorePlan.PREMIUM,
                "💬",
                "success",
                "/admin/whatsapp",
                5,
                true
        );

        register(
                Feature.AUTOMATIONS,
                "Automatizaciones",
                "Crea flujos automáticos para ventas y seguimiento.",
                FeatureCategory.AUTOMATION,
                StorePlan.PREMIUM,
                "⚡",
                "warning",
                "/admin/automations",
                8,
                true
        );

        register(
                Feature.MULTI_USER,
                "Multiusuario",
                "Permite múltiples usuarios por tienda.",
                FeatureCategory.PLATFORM,
                StorePlan.PREMIUM,
                "👥",
                "info",
                "/admin/users",
                4,
                true
        );

        register(
                Feature.API_ACCESS,
                "API Access",
                "Acceso API para integraciones externas.",
                FeatureCategory.PLATFORM,
                StorePlan.PREMIUM,
                "🔌",
                "dark",
                "/admin/api",
                4,
                true
        );

        register(
                Feature.WHITE_LABEL_FULL,
                "White Label Full",
                "Personalización avanzada con CSS, JS y tracking.",
                FeatureCategory.PLATFORM,
                StorePlan.PREMIUM,
                "🎨",
                "dark",
                "/admin/store/settings",
                8,
                true
        );
    }

    private void register(
            Feature feature,
            String displayName,
            String description,
            FeatureCategory category,
            StorePlan minimumPlan,
            String icon,
            String color,
            String url,
            int healthWeight,
            boolean premium
    ) {
        registry.register(
                FeatureDefinition.builder(feature)
                        .displayName(displayName)
                        .description(description)
                        .category(category)
                        .minimumPlan(minimumPlan)
                        .icon(icon)
                        .color(color)
                        .url(url)
                        .healthWeight(healthWeight)
                        .premium(premium)
                        .order(resolveOrder(feature))
                        .showInSidebar(resolveShowInSidebar(feature))
                        .enabled(true)
                        .build()
        );
    }
    private int resolveOrder(Feature feature) {
        return switch (feature) {
            case PRODUCTS -> 10;
            case CATEGORIES -> 20;
            case INVENTORY -> 30;
            case ORDERS -> 40;
            case CHECKOUT -> 50;
            case REVIEWS -> 60;

            case CRM -> 100;
            case LEADS -> 110;
            case PIPELINE -> 120;
            case TASKS -> 130;
            case PROPOSALS -> 140;
            case ANALYTICS -> 150;

            case COUPONS -> 200;
            case EMAIL_MARKETING -> 210;
            case WHATSAPP_AUTOMATION -> 220;
            case AUTOMATIONS -> 230;

            case STRIPE_CONNECT -> 300;
            case CUSTOM_DOMAIN -> 310;
            case WHITE_LABEL_FULL -> 320;
            case MULTI_USER -> 330;
            case API_ACCESS -> 340;
        };
    }

    private boolean resolveShowInSidebar(Feature feature) {
        return switch (feature) {
            case CHECKOUT,
                 API_ACCESS -> false;

            default -> true;
        };
    }
}