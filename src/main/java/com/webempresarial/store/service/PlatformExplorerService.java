package com.webempresarial.store.service;

import com.webempresarial.store.dto.platform.PlatformConsoleDTO;
import com.webempresarial.store.dto.platform.PlatformHealthDTO;
import com.webempresarial.store.dto.platform.PlatformMetricDTO;
import com.webempresarial.store.dto.platform.PlatformModuleDTO;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.health.HealthEngine;
import com.webempresarial.store.feature.metrics.MetricsEngine;
import com.webempresarial.store.feature.registry.AutomationRegistry;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.ModuleRuntimeRegistry;
import com.webempresarial.store.feature.registry.PermissionRegistry;
import com.webempresarial.store.feature.runtime.ModuleRuntime;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformExplorerService {

    private final ModuleRegistry moduleRegistry;
    private final ModuleRuntimeRegistry runtimeRegistry;
    private final PermissionRegistry permissionRegistry;
    private final AutomationRegistry automationRegistry;
    private final HealthEngine healthEngine;
    private final MetricsEngine metricsEngine;

    public PlatformExplorerService(
            ModuleRegistry moduleRegistry,
            ModuleRuntimeRegistry runtimeRegistry,
            PermissionRegistry permissionRegistry,
            AutomationRegistry automationRegistry,
            HealthEngine healthEngine,
            MetricsEngine metricsEngine
    ) {
        this.moduleRegistry = moduleRegistry;
        this.runtimeRegistry = runtimeRegistry;
        this.permissionRegistry = permissionRegistry;
        this.automationRegistry = automationRegistry;
        this.healthEngine = healthEngine;
        this.metricsEngine = metricsEngine;
    }

    public PlatformConsoleDTO console() {
        List<PlatformModuleDTO> modules = moduleRegistry.all()
                .stream()
                .map(this::toModuleDTO)
                .toList();

        List<PlatformHealthDTO> healthResults = healthEngine.checkAll()
                .stream()
                .map(result -> new PlatformHealthDTO(
                        result.name(),
                        result.status().name(),
                        result.message(),
                        result.checkedAt().toString()
                ))
                .toList();
        
        List<PlatformMetricDTO> metrics = metricsEngine.collectAll()
                .stream()
                .map(metric -> new PlatformMetricDTO(
                        metric.code(),
                        metric.name(),
                        metric.value(),
                        metric.unit(),
                        metric.measuredAt().toString()
                ))
                .toList();

        return new PlatformConsoleDTO(
                moduleRegistry.count(),
                moduleRegistry.featureCount(),
                moduleRegistry.dashboardWidgetCount(),
                moduleRegistry.sidebarSectionCount(),
                permissionRegistry.all().size(),
                automationRegistry.all().size(),
                modules,
                healthResults,
                metrics
        );
    }

    private PlatformModuleDTO toModuleDTO(PlatformModuleDescriptor module) {
        int dashboardWidgets = (int) module.getFeatures()
                .stream()
                .filter(feature -> feature.getPresentation().isShowInDashboard())
                .count();

        ModuleRuntime runtime = runtimeRegistry
                .findByName(module.getName())
                .orElse(null);

        return new PlatformModuleDTO(
                module.getName(),
                module.getDescription(),
                module.getFeatures().size(),
                dashboardWidgets,
                module.getSidebarSections().size(),
                module.getPermissions().size(),
                module.getAutomations().size(),
                runtime != null ? runtime.status().name() : "UNKNOWN",
                runtime != null ? runtime.errorMessage() : ""
        );
    }
}