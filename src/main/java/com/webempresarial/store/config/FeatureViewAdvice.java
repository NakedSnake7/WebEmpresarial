package com.webempresarial.store.config;

import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class FeatureViewAdvice {

    private static final String INVENTORY_URL =
            "/admin/inventory";

    private final StoreContextService storeContextService;
    private final SidebarRegistry sidebarRegistry;
    private final InventoryPersistentAlertService inventoryAlertService;
    private final ObjectProvider<FeatureAccessService>
            featureAccessServiceProvider;

    public FeatureViewAdvice(
            StoreContextService storeContextService,
            SidebarRegistry sidebarRegistry,
            InventoryPersistentAlertService inventoryAlertService,
            ObjectProvider<FeatureAccessService>
                    featureAccessServiceProvider
    ) {
        this.storeContextService = storeContextService;
        this.sidebarRegistry = sidebarRegistry;
        this.inventoryAlertService = inventoryAlertService;
        this.featureAccessServiceProvider =
                featureAccessServiceProvider;
    }

    @ModelAttribute("currentPath")
    public String currentPath(
            HttpServletRequest request
    ) {
        return request.getRequestURI();
    }

    @ModelAttribute("sidebarActivePath")
    public String sidebarActivePath(
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();

        if (path == null) {
            return null;
        }

        /*
         * Commerce Workspace v1:
         *
         * Estas URLs siguen existiendo y conservan exactamente
         * sus reglas de seguridad. Únicamente pertenecen
         * visualmente al workspace Productos.
         */
        if (path.equals("/nuevo")
                || path.equals("/modificar-precios")
                || path.startsWith("/editar/")) {
            return "/admin/productos";
        }

        /*
         * CRM Workspace v1:
         *
         * Las subsecciones mantienen sus URLs y sus reglas
         * independientes de feature/permisos. Únicamente
         * comparten la identidad visual del workspace CRM.
         *
         * /crm/reports queda fuera porque pertenece a Analytics.
         * /admin/store/crm/** queda fuera porque pertenece
         * a Store / Settings.
         */
        if (path.equals("/crm/pipeline")
                || path.startsWith("/crm/pipeline/")
                || path.equals("/crm/tasks")
                || path.startsWith("/crm/tasks/")
                || path.equals("/admin/leads")
                || path.startsWith("/admin/leads/")
                || path.startsWith("/crm/leads/")) {
            return "/crm/dashboard";
        }

        return path;
    }

    @ModelAttribute("sidebarSections")
    public List<SidebarSectionDTO> sidebarSections(
            HttpServletRequest request
    ) {
        Store store = currentStore(request);

        if (store == null) {
            return List.of();
        }

        AdminRole role = currentAdminRole();

        if (role == null) {
            return List.of();
        }

        return sidebarRegistry.sections(
                store,
                role
        );

    }

    @ModelAttribute("sidebarBadges")
    public Map<String, Long> sidebarBadges(
            HttpServletRequest request
    ) {
        /*
         * Evita consultar alertas en vistas públicas.
         * FeatureViewAdvice se ejecuta para todos los controladores MVC.
         */
        if (!isAdminRequest(request)) {
            return Map.of();
        }

        Store store = currentStore(request);

        if (store == null) {
            return Map.of();
        }

        /*
         * El badge forma parte de Inventory.
         *
         * No debe consultar el subsistema si el usuario no
         * puede ver inventario o si el plan no habilita la
         * feature.
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!hasAuthority(
                authentication,
                AdminPermission.INVENTORY_VIEW.authority()
        )) {
            return Map.of();
        }

        FeatureAccessService featureAccessService =
                featureAccessServiceProvider
                        .getIfAvailable();

        /*
         * @WebMvcTest y otros slices pueden cargar este
         * ControllerAdvice sin cargar el catálogo completo.
         *
         * Si el servicio de features no existe, el badge queda
         * fail-closed: no se consulta Inventory.
         */
        if (featureAccessService == null) {
            return Map.of();
        }

        if (!featureAccessService.canUse(
                store,
                Feature.INVENTORY
        )) {
            return Map.of();
        }

        long activeAlerts =
                inventoryAlertService.countActive(store);

        if (activeAlerts <= 0) {
            return Map.of();
        }

        return Map.of(
                INVENTORY_URL,
                activeAlerts
        );
    }

    private boolean isAdminRequest(
            HttpServletRequest request
    ) {
        String uri = request.getRequestURI();

        return uri != null
                && (
                    uri.equals("/admin")
                    || uri.startsWith("/admin/")
                );
    }

    private Store currentStore(
            HttpServletRequest request
    ) {
        try {
            return storeContextService.getCurrentStore(request);
        } catch (Exception e) {
            return null;
        }
    }

    private AdminRole currentAdminRole() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        if (hasAuthority(authentication, "ROLE_SUPER_ADMIN")) {
            return AdminRole.SUPER_ADMIN;
        }

        if (hasAuthority(authentication, "ROLE_STORE_OWNER")) {
            return AdminRole.STORE_OWNER;
        }

        if (hasAuthority(authentication, "ROLE_STORE_ADMIN")) {
            return AdminRole.STORE_ADMIN;
        }

        if (hasAuthority(authentication, "ROLE_STORE_STAFF")) {
            return AdminRole.STORE_STAFF;
        }

        if (hasAuthority(authentication, "ROLE_SALES")) {
            return AdminRole.SALES;
        }

        return null;
    }

    private boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(granted ->
                        authority.equals(
                                granted.getAuthority()
                        )
                );
    }
}