package com.webempresarial.store.feature.sidebar;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;

import java.util.Set;

public record SidebarItemDefinition(
        String title,
        String icon,
        String url,
        Feature feature,
        Set<AdminRole> allowedRoles
) {

    /*
     * Compatibilidad con todos los módulos existentes.
     *
     * Si un item no declara restricciones de rol,
     * pertenece al panel operacional y puede ser
     * mostrado a los tres roles administrativos.
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
                Set.of(
                        AdminRole.SUPER_ADMIN,
                        AdminRole.STORE_ADMIN,
                        AdminRole.STORE_STAFF
                )
        );
    }

    public SidebarItemDefinition {
        allowedRoles = allowedRoles == null
                ? Set.of()
                : Set.copyOf(allowedRoles);
    }

    public boolean allows(AdminRole role) {
        return role != null
                && allowedRoles.contains(role);
    }
}