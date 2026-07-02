package com.webempresarial.store.feature;

import com.webempresarial.store.feature.registry.AutomationRegistry;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.feature.registry.EventRegistry;
import com.webempresarial.store.feature.registry.MarketplaceRegistry;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.PermissionRegistry;
import com.webempresarial.store.feature.registry.SidebarRegistry;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlatformKernelConfiguration {

    private final PlatformKernel platformKernel;
    private final ModuleRegistry moduleRegistry;
    private final SidebarRegistry sidebarRegistry;
    private final DashboardRegistry dashboardRegistry;
    private final MarketplaceRegistry marketplaceRegistry;
    private final EventRegistry eventRegistry;
    private final PermissionRegistry permissionRegistry;
    private final AutomationRegistry automationRegistry;
    private final List<PlatformModule> modules;

    public PlatformKernelConfiguration(
            PlatformKernel platformKernel,
            ModuleRegistry moduleRegistry,
            SidebarRegistry sidebarRegistry,
            DashboardRegistry dashboardRegistry,
            MarketplaceRegistry marketplaceRegistry,
            EventRegistry eventRegistry,
            PermissionRegistry permissionRegistry,
            AutomationRegistry automationRegistry,
            List<PlatformModule> modules
    ) {
        this.platformKernel = platformKernel;
        this.moduleRegistry = moduleRegistry;
        this.sidebarRegistry = sidebarRegistry;
        this.dashboardRegistry = dashboardRegistry;
        this.marketplaceRegistry = marketplaceRegistry;
        this.eventRegistry = eventRegistry;
        this.permissionRegistry = permissionRegistry;
        this.automationRegistry = automationRegistry;
        this.modules = modules;
    }

    @PostConstruct
    public void registerModules() {
    	ModuleLifecycleContext context = new ModuleLifecycleContext(
    	        platformKernel,
    	        moduleRegistry,
    	        sidebarRegistry,
    	        dashboardRegistry,
    	        marketplaceRegistry,
    	        eventRegistry,
    	        permissionRegistry,
    	        automationRegistry
    	);

        modules.forEach(module -> {
            ModuleDefinition definition = module.definition();

            moduleRegistry.register(definition);

            definition.getFeatures()
                    .forEach(platformKernel::register);

            sidebarRegistry.register(definition);
            dashboardRegistry.register(definition);

            module.boot(context);
        });
    }
}