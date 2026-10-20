package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;

import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class AdminPermissionService {

    private static final Map<AdminRole, Set<AdminPermission>> PERMISSIONS_BY_ROLE =
            buildPermissions();

    /**
     * Devuelve los permisos efectivos del rol.
     */
    public Set<AdminPermission> permissionsFor(AdminRole role) {

        if (role == null) {
            return Set.of();
        }

        return PERMISSIONS_BY_ROLE.getOrDefault(role, Set.of());
    }

    /**
     * Comprueba si el rol posee un permiso determinado.
     */
    public boolean hasPermission(
            AdminRole role,
            AdminPermission permission
    ) {

        if (role == null || permission == null) {
            return false;
        }

        return permissionsFor(role).contains(permission);
    }

    private static Map<AdminRole, Set<AdminPermission>> buildPermissions() {

        EnumMap<AdminRole, Set<AdminPermission>> permissions =
                new EnumMap<>(AdminRole.class);

        /*
         * SUPER_ADMIN
         *
         * Administrador interno de WebEmpresarial.
         * Tiene todas las capacidades administrativas.
         */
        permissions.put(
                AdminRole.SUPER_ADMIN,
                immutable(EnumSet.allOf(AdminPermission.class))
        );

        /*
         * STORE_OWNER
         *
         * Propietario contractual de la tienda.
         * Tiene control completo de su tenant, pero no acceso
         * a las capacidades internas de administración SaaS.
         */
        permissions.put(
                AdminRole.STORE_OWNER,
                immutable(EnumSet.of(

                        AdminPermission.DASHBOARD_VIEW,

                        AdminPermission.PRODUCT_VIEW,
                        AdminPermission.PRODUCT_CREATE,
                        AdminPermission.PRODUCT_EDIT,
                        AdminPermission.PRODUCT_DELETE,

                        AdminPermission.CATEGORY_VIEW,
                        AdminPermission.CATEGORY_MANAGE,

                        AdminPermission.BRAND_VIEW,
                        AdminPermission.BRAND_MANAGE,

                        AdminPermission.INVENTORY_VIEW,
                        AdminPermission.INVENTORY_EDIT,

                        AdminPermission.ORDER_VIEW,
                        AdminPermission.ORDER_UPDATE,
                        AdminPermission.ORDER_CANCEL,

                        AdminPermission.CUSTOMER_VIEW,
                        AdminPermission.CUSTOMER_EDIT,
                        AdminPermission.CUSTOMER_DELETE,

                        AdminPermission.REVIEW_VIEW,
                        AdminPermission.REVIEW_CREATE,
                        AdminPermission.REVIEW_EDIT,
                        AdminPermission.REVIEW_DELETE,

                        AdminPermission.CRM_VIEW,
                        AdminPermission.CRM_EDIT,

                        AdminPermission.PIPELINE_VIEW,
                        AdminPermission.PIPELINE_MANAGE,

                        AdminPermission.PROPOSAL_VIEW,
                        AdminPermission.PROPOSAL_CREATE,
                        AdminPermission.PROPOSAL_EDIT,
                        AdminPermission.PROPOSAL_DELETE,

                        AdminPermission.AUTOMATION_VIEW,
                        AdminPermission.AUTOMATION_MANAGE,

                        AdminPermission.ANALYTICS_VIEW,
                        AdminPermission.REPORT_VIEW,

                        AdminPermission.STORE_VIEW,
                        AdminPermission.STORE_EDIT,
                        AdminPermission.STORE_BRANDING_EDIT,
                        AdminPermission.STORE_COMMERCIAL_INFO_EDIT,

                        AdminPermission.PAYMENT_METHOD_VIEW,
                        AdminPermission.PAYMENT_METHOD_MANAGE,
                        AdminPermission.STRIPE_CONNECT_MANAGE,

                        AdminPermission.DOMAIN_VIEW,
                        AdminPermission.DOMAIN_MANAGE,

                        AdminPermission.INTEGRATION_VIEW,
                        AdminPermission.INTEGRATION_MANAGE,

                        AdminPermission.WHITE_LABEL_MANAGE,

                        AdminPermission.TEAM_VIEW,
                        AdminPermission.TEAM_INVITE,
                        AdminPermission.TEAM_EDIT,
                        AdminPermission.TEAM_REMOVE,

                        AdminPermission.ROLE_VIEW,
                        AdminPermission.ROLE_MANAGE,

                        AdminPermission.SUBSCRIPTION_VIEW,
                        AdminPermission.SUBSCRIPTION_MANAGE
                ))
        );

        /*
         * STORE_ADMIN
         *
         * Administrador operativo.
         *
         * No puede:
         * - gestionar la suscripción,
         * - administrar Stripe Connect,
         * - administrar roles privilegiados.
         */
        permissions.put(
                AdminRole.STORE_ADMIN,
                immutable(EnumSet.of(

                        AdminPermission.DASHBOARD_VIEW,

                        AdminPermission.PRODUCT_VIEW,
                        AdminPermission.PRODUCT_CREATE,
                        AdminPermission.PRODUCT_EDIT,
                        AdminPermission.PRODUCT_DELETE,

                        AdminPermission.CATEGORY_VIEW,
                        AdminPermission.CATEGORY_MANAGE,

                        AdminPermission.BRAND_VIEW,
                        AdminPermission.BRAND_MANAGE,

                        AdminPermission.INVENTORY_VIEW,
                        AdminPermission.INVENTORY_EDIT,

                        AdminPermission.ORDER_VIEW,
                        AdminPermission.ORDER_UPDATE,
                        AdminPermission.ORDER_CANCEL,

                        AdminPermission.CUSTOMER_VIEW,
                        AdminPermission.CUSTOMER_EDIT,
                        AdminPermission.CUSTOMER_DELETE,

                        AdminPermission.REVIEW_VIEW,
                        AdminPermission.REVIEW_CREATE,
                        AdminPermission.REVIEW_EDIT,
                        AdminPermission.REVIEW_DELETE,

                        AdminPermission.CRM_VIEW,
                        AdminPermission.CRM_EDIT,

                        AdminPermission.PIPELINE_VIEW,
                        AdminPermission.PIPELINE_MANAGE,

                        AdminPermission.PROPOSAL_VIEW,
                        AdminPermission.PROPOSAL_CREATE,
                        AdminPermission.PROPOSAL_EDIT,
                        AdminPermission.PROPOSAL_DELETE,

                        AdminPermission.AUTOMATION_VIEW,
                        AdminPermission.AUTOMATION_MANAGE,

                        AdminPermission.ANALYTICS_VIEW,
                        AdminPermission.REPORT_VIEW,

                        AdminPermission.STORE_VIEW,
                        AdminPermission.STORE_EDIT,
                        AdminPermission.STORE_BRANDING_EDIT,
                        AdminPermission.STORE_COMMERCIAL_INFO_EDIT,

                        AdminPermission.PAYMENT_METHOD_VIEW,
                        AdminPermission.PAYMENT_METHOD_MANAGE,

                        AdminPermission.DOMAIN_VIEW,
                        AdminPermission.DOMAIN_MANAGE,

                        AdminPermission.INTEGRATION_VIEW,
                        AdminPermission.INTEGRATION_MANAGE,

                        AdminPermission.WHITE_LABEL_MANAGE,

                        AdminPermission.TEAM_VIEW,
                        AdminPermission.TEAM_INVITE,
                        AdminPermission.TEAM_EDIT,
                        AdminPermission.TEAM_REMOVE,

                        AdminPermission.ROLE_VIEW,

                        AdminPermission.SUBSCRIPTION_VIEW
                ))
        );

        /*
         * STORE_STAFF
         *
         * Operación cotidiana de la tienda.
         */
        permissions.put(
                AdminRole.STORE_STAFF,
                immutable(EnumSet.of(

                        AdminPermission.DASHBOARD_VIEW,

                        AdminPermission.PRODUCT_VIEW,
                        AdminPermission.PRODUCT_CREATE,
                        AdminPermission.PRODUCT_EDIT,

                        AdminPermission.CATEGORY_VIEW,
                        AdminPermission.BRAND_VIEW,

                        AdminPermission.INVENTORY_VIEW,
                        AdminPermission.INVENTORY_EDIT,

                        AdminPermission.ORDER_VIEW,
                        AdminPermission.ORDER_UPDATE,

                        AdminPermission.CUSTOMER_VIEW,

                        AdminPermission.REVIEW_VIEW,
                        AdminPermission.REVIEW_CREATE,
                        AdminPermission.REVIEW_EDIT
                ))
        );

        /*
         * SALES
         *
         * Perfil comercial:
         * clientes, CRM, pipeline y propuestas.
         *
         * Puede consultar productos y pedidos para obtener
         * contexto comercial, pero no modificar su operación.
         */
        permissions.put(
                AdminRole.SALES,
                immutable(EnumSet.of(

                        AdminPermission.DASHBOARD_VIEW,

                        AdminPermission.PRODUCT_VIEW,

                        AdminPermission.ORDER_VIEW,

                        AdminPermission.CUSTOMER_VIEW,
                        AdminPermission.CUSTOMER_EDIT,

                        AdminPermission.CRM_VIEW,
                        AdminPermission.CRM_EDIT,

                        AdminPermission.PIPELINE_VIEW,
                        AdminPermission.PIPELINE_MANAGE,

                        AdminPermission.PROPOSAL_VIEW,
                        AdminPermission.PROPOSAL_CREATE,
                        AdminPermission.PROPOSAL_EDIT,

                        AdminPermission.ANALYTICS_VIEW
                ))
        );

        return Map.copyOf(permissions);
    }

    private static Set<AdminPermission> immutable(
            EnumSet<AdminPermission> permissions
    ) {
        return Set.copyOf(permissions);
    }
}
