package com.webempresarial.store.feature.metrics.providers;

import com.webempresarial.store.feature.metrics.MetricValue;
import com.webempresarial.store.feature.metrics.MetricsProvider;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.PermissionRegistry;
import com.webempresarial.store.feature.registry.AutomationRegistry;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class KernelMetricsProvider implements MetricsProvider {

    private final ModuleRegistry moduleRegistry;
    private final PermissionRegistry permissionRegistry;
    private final AutomationRegistry automationRegistry;

    public KernelMetricsProvider(
            ModuleRegistry moduleRegistry,
            PermissionRegistry permissionRegistry,
            AutomationRegistry automationRegistry
    ) {
        this.moduleRegistry = moduleRegistry;
        this.permissionRegistry = permissionRegistry;
        this.automationRegistry = automationRegistry;
    }

    @Override
    public List<MetricValue> collect() {
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                new MetricValue("kernel.modules", "Módulos", moduleRegistry.count(), "count", now),
                new MetricValue("kernel.features", "Features", moduleRegistry.featureCount(), "count", now),
                new MetricValue("kernel.permissions", "Permisos", permissionRegistry.all().size(), "count", now),
                new MetricValue("kernel.automations", "Automations", automationRegistry.all().size(), "count", now)
        );
    }
}