package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.PlatformConsoleDTO;
import com.webempresarial.store.dto.platform.PlatformModuleDTO;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.registry.AutomationRegistry;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.PermissionRegistry;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformExplorerService {

    private final ModuleRegistry moduleRegistry;
    private final PermissionRegistry permissionRegistry;
    private final AutomationRegistry automationRegistry;

    public PlatformExplorerService(
            ModuleRegistry moduleRegistry,
            PermissionRegistry permissionRegistry,
            AutomationRegistry automationRegistry
    ) {
        this.moduleRegistry = moduleRegistry;
        this.permissionRegistry = permissionRegistry;
        this.automationRegistry = automationRegistry;
    }

    public PlatformConsoleDTO console() {
        List<PlatformModuleDTO> modules = moduleRegistry.all()
                .stream()
                .map(this::toModuleDTO)
                .toList();

        return new PlatformConsoleDTO(
                moduleRegistry.count(),
                moduleRegistry.featureCount(),
                moduleRegistry.dashboardWidgetCount(),
                moduleRegistry.sidebarSectionCount(),
                permissionRegistry.all().size(),
                automationRegistry.all().size(),
                modules
        );
    }

    private PlatformModuleDTO toModuleDTO(PlatformModuleDescriptor module) {
        int dashboardWidgets = (int) module.getFeatures()
                .stream()
                .filter(feature -> feature.getPresentation().isShowInDashboard())
                .count();

        return new PlatformModuleDTO(
                module.getName(),
                module.getDescription(),
                module.getFeatures().size(),
                dashboardWidgets,
                module.getSidebarSections().size(),
                module.getPermissions().size(),
                module.getAutomations().size()
        );
    }
}