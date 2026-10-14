package com.webempresarial.store.model;

public enum AdminRole {

    /**
     * Administrador interno de WebEmpresarial.
     * Tiene alcance sobre toda la plataforma y no pertenece
     * necesariamente a una tienda.
     */
    SUPER_ADMIN,

    /**
     * Propietario contractual de una tienda.
     * Tiene acceso a configuración sensible, equipo,
     * facturación y suscripción.
     */
    STORE_OWNER,

    /**
     * Administrador operativo de una tienda.
     * Puede administrar la operación, pero no acciones
     * reservadas exclusivamente al propietario.
     */
    STORE_ADMIN,

    /**
     * Personal operativo.
     * Catálogo, inventario, pedidos, etc.
     */
    STORE_STAFF,

    /**
     * Personal comercial.
     * Clientes, CRM, pipeline y propuestas.
     */
    SALES;

    public boolean isPlatformRole() {
        return this == SUPER_ADMIN;
    }

    public boolean isTenantRole() {
        return !isPlatformRole();
    }
}
