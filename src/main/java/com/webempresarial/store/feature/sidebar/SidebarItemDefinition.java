package com.webempresarial.store.feature.sidebar;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;

import java.util.Set;

public record SidebarItemDefinition(
        String title,
        String icon,
        String url,
        Feature feature,
        AdminPermission requiredPermission,
        Set<AdminRole> allowedRoles
) {

    private static final Set<AdminRole> DEFAULT_OPERATIONAL_ROLES =
            Set.of(
                    AdminRole.SUPER_ADMIN,
                    AdminRole.STORE_OWNER,
                    AdminRole.STORE_ADMIN,
                    AdminRole.STORE_STAFF
            );

    /*
     * Compatibilidad con los módulos existentes.
     *
     * Mientras un item no declare un permiso granular,
     * conserva la política histórica del sidebar.
     */
    public SidebarItemDefinition(
            String title,
            String icon,
            String url,
            Feature feature
    ) {
        this(
                title,
                icon,
                url,
                feature,
                null,
                DEFAULT_OPERATIONAL_ROLES
        );
    }

    /*
     * Compatibilidad con constructores existentes
     * que declaran roles explícitos.
     */
    public SidebarItemDefinition(
            String title,
            String icon,
            String url,
            Feature feature,
            Set<AdminRole> allowedRoles
    ) {
        this(
                title,
                icon,
                url,
                feature,
                null,
                allowedRoles
        );
    }

    /*
     * Nueva variante permission-first.
     *
     * No replica roles: cualquier rol que realmente posea
     * la authority correspondiente puede ver el item.
     */
    public SidebarItemDefinition(
            String title,
            String icon,
            String url,
            Feature feature,
            AdminPermission requiredPermission
    ) {
        this(
                title,
                icon,
                url,
                feature,
                requiredPermission,
                Set.of()
        );
    }

    public SidebarItemDefinition {
        allowedRoles = allowedRoles == null
                ? Set.of()
                : Set.copyOf(allowedRoles);
    }

    public boolean allows(AdminRole role) {
        return allowedRoles.isEmpty()
                || (
                    role != null
                    && allowedRoles.contains(role)
                );
    }
}
