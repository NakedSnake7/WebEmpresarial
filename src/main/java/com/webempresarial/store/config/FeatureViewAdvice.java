package com.webempresarial.store.config;

import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class FeatureViewAdvice {

    private final StoreContextService storeContextService;
    private final SidebarRegistry sidebarRegistry;

    public FeatureViewAdvice(
            StoreContextService storeContextService,
            SidebarRegistry sidebarRegistry
    ) {
        this.storeContextService = storeContextService;
        this.sidebarRegistry = sidebarRegistry;
    }
    
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
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

    private Store currentStore(HttpServletRequest request) {
        try {
            return storeContextService.getCurrentStore(request);
        } catch (Exception e) {
            return null;
        }
    }
}