package com.webempresarial.store.model;

/**
 * Acciones que un usuario administrativo puede realizar dentro
 * de WebEmpresarial.
 *
 * IMPORTANTE:
 * AdminPermission responde a "¿el rol puede hacer esto?".
 * No determina si el plan contratado incluye la funcionalidad.
 */
public enum AdminPermission {

    // Dashboard
    DASHBOARD_VIEW,

    // Productos
    PRODUCT_VIEW,
    PRODUCT_CREATE,
    PRODUCT_EDIT,
    PRODUCT_DELETE,

    // Categorías
    CATEGORY_VIEW,
    CATEGORY_MANAGE,

    // Marcas
    BRAND_VIEW,
    BRAND_MANAGE,

    // Inventario
    INVENTORY_VIEW,
    INVENTORY_EDIT,

    // Pedidos
    ORDER_VIEW,
    ORDER_UPDATE,
    ORDER_CANCEL,

    // Clientes
    CUSTOMER_VIEW,
    CUSTOMER_EDIT,
    CUSTOMER_DELETE,

    // CRM
    CRM_VIEW,
    CRM_EDIT,

    // Pipeline
    PIPELINE_VIEW,
    PIPELINE_MANAGE,

    // Propuestas
    PROPOSAL_VIEW,
    PROPOSAL_CREATE,
    PROPOSAL_EDIT,
    PROPOSAL_DELETE,

    // Automatizaciones
    AUTOMATION_VIEW,
    AUTOMATION_MANAGE,

    // Analítica
    ANALYTICS_VIEW,
    REPORT_VIEW,

    // Configuración de tienda
    STORE_VIEW,
    STORE_EDIT,
    STORE_BRANDING_EDIT,
    STORE_COMMERCIAL_INFO_EDIT,

    // Métodos de pago
    PAYMENT_METHOD_VIEW,
    PAYMENT_METHOD_MANAGE,
    STRIPE_CONNECT_MANAGE,

    // Dominio
    DOMAIN_VIEW,
    DOMAIN_MANAGE,

    // Integraciones
    INTEGRATION_VIEW,
    INTEGRATION_MANAGE,

    // Equipo
    TEAM_VIEW,
    TEAM_INVITE,
    TEAM_EDIT,
    TEAM_REMOVE,

    // Roles administrativos
    ROLE_VIEW,
    ROLE_MANAGE,

    // Suscripción
    SUBSCRIPTION_VIEW,
    SUBSCRIPTION_MANAGE,

    // Administración de la plataforma SaaS
    PLATFORM_DASHBOARD_VIEW,

    PLATFORM_STORE_VIEW,
    PLATFORM_STORE_MANAGE,

    PLATFORM_USER_VIEW,
    PLATFORM_USER_MANAGE,

    PLATFORM_SUBSCRIPTION_VIEW,
    PLATFORM_SUBSCRIPTION_MANAGE,

    PLATFORM_METRICS_VIEW,
    PLATFORM_CONFIG_MANAGE;

    /**
     * Nombre de authority que posteriormente expondremos a Spring Security.
     *
     * Ejemplo:
     * PRODUCT_EDIT -> PERM_PRODUCT_EDIT
     */
    public String authority() {
        return "PERM_" + name();
    }
}
