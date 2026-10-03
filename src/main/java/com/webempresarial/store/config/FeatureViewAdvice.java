package com.webempresarial.store.config;

import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class FeatureViewAdvice {

    private static final String INVENTORY_ALERTS_URL =
            "/admin/inventory/alerts";

    private final StoreContextService storeContextService;
    private final SidebarRegistry sidebarRegistry;
    private final InventoryPersistentAlertService inventoryAlertService;

    public FeatureViewAdvice(
            StoreContextService storeContextService,
            SidebarRegistry sidebarRegistry,
            InventoryPersistentAlertService inventoryAlertService
    ) {
        this.storeContextService = storeContextService;
        this.sidebarRegistry = sidebarRegistry;
        this.inventoryAlertService = inventoryAlertService;
    }

    @ModelAttribute("currentPath")
    public String currentPath(
            HttpServletRequest request
    ) {
        return request.getRequestURI();
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

        long activeAlerts =
                inventoryAlertService.countActive(store);

        if (activeAlerts <= 0) {
            return Map.of();
        }

        return Map.of(
                INVENTORY_ALERTS_URL,
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

        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return AdminRole.SUPER_ADMIN;
        }

        if (hasRole(authentication, "ROLE_STORE_ADMIN")) {
            return AdminRole.STORE_ADMIN;
        }

        if (hasRole(authentication, "ROLE_STORE_STAFF")) {
            return AdminRole.STORE_STAFF;
        }

        return null;
    }

    private boolean hasRole(
            Authentication authentication,
            String role
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        role.equals(
                                authority.getAuthority()
                        )
                );
    }
}