package com.webempresarial.store.config;

import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

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

        return sidebarRegistry.sections(store);
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
}