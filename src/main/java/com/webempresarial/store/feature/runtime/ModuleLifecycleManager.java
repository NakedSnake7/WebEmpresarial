package com.webempresarial.store.feature.runtime;

import com.webempresarial.store.feature.ModuleLifecycleContext;
import com.webempresarial.store.feature.PlatformKernel;
import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.dependency.PlatformBootPlan;
import com.webempresarial.store.feature.dependency.PlatformBootPlanner;
import com.webempresarial.store.feature.registry.AutomationRegistry;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.feature.registry.EventRegistry;
import com.webempresarial.store.feature.registry.HealthRegistry;
import com.webempresarial.store.feature.registry.MarketplaceRegistry;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.ModuleRuntimeRegistry;
import com.webempresarial.store.feature.registry.PermissionRegistry;
import com.webempresarial.store.feature.registry.SidebarRegistry;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModuleLifecycleManager {

    private final PlatformKernel platformKernel;
    private final ModuleRegistry moduleRegistry;
    private final ModuleRuntimeRegistry runtimeRegistry;
    private final SidebarRegistry sidebarRegistry;
    private final DashboardRegistry dashboardRegistry;
    private final MarketplaceRegistry marketplaceRegistry;
    private final EventRegistry eventRegistry;
    private final PermissionRegistry permissionRegistry;
    private final AutomationRegistry automationRegistry;
    private final PlatformBootPlanner bootPlanner;
    private final HealthRegistry healthRegistry;
    private final List<PlatformModule> modules;

    public ModuleLifecycleManager(
            PlatformKernel platformKernel,
            ModuleRegistry moduleRegistry,
            ModuleRuntimeRegistry runtimeRegistry,
            SidebarRegistry sidebarRegistry,
            DashboardRegistry dashboardRegistry,
            MarketplaceRegistry marketplaceRegistry,
            EventRegistry eventRegistry,
            PermissionRegistry permissionRegistry,
            AutomationRegistry automationRegistry,
            PlatformBootPlanner bootPlanner,
            HealthRegistry healthRegistry,
            List<PlatformModule> modules
    ) {
        this.platformKernel = platformKernel;
        this.moduleRegistry = moduleRegistry;
        this.runtimeRegistry = runtimeRegistry;
        this.sidebarRegistry = sidebarRegistry;
        this.dashboardRegistry = dashboardRegistry;
        this.marketplaceRegistry = marketplaceRegistry;
        this.eventRegistry = eventRegistry;
        this.permissionRegistry = permissionRegistry;
        this.automationRegistry = automationRegistry;
        this.bootPlanner = bootPlanner;
        this.healthRegistry = healthRegistry;
        this.modules = modules;
    }

    public void startPlatform() {
        ModuleLifecycleContext context = new ModuleLifecycleContext(
                platformKernel,
                moduleRegistry,
                sidebarRegistry,
                dashboardRegistry,
                marketplaceRegistry,
                eventRegistry,
                permissionRegistry,
                healthRegistry,
                automationRegistry
        );

        discoverModules();

        PlatformBootPlan bootPlan = bootPlanner.plan();

        printBootOrder(bootPlan);

        loadRegistries(bootPlan);

        bootModules(bootPlan, context);
    }

    private void discoverModules() {
        modules.forEach(module -> {
            PlatformModuleDescriptor descriptor = module.descriptor();

            moduleRegistry.register(descriptor);
            runtimeRegistry.register(new ModuleRuntime(module, descriptor));
        });
    }

    private void loadRegistries(PlatformBootPlan bootPlan) {
    	bootPlan.bootOrder().forEach(runtime -> {
    	    PlatformModuleDescriptor descriptor = runtime.descriptor();

    	    descriptor.getFeatures()
    	            .forEach(platformKernel::register);

    	    sidebarRegistry.register(descriptor);
    	    dashboardRegistry.register(descriptor);

    	    descriptor.getPermissions()
    	            .forEach(permissionRegistry::register);

    	    descriptor.getAutomations()
    	            .forEach(automationRegistry::register);

    	    descriptor.getEvents()
            .forEach(eventRegistry::register);

    healthRegistry.register(descriptor);

    runtime.markLoaded();
    	});
    }

    private void bootModules(
            PlatformBootPlan bootPlan,
            ModuleLifecycleContext context
    ) {
        bootPlan.bootOrder().forEach(runtime -> {
        	long start = System.currentTimeMillis();

        	try {

        	    runtime.module().boot(context);

        	    runtime.markBooted();

        	    runtime.markBootCompleted(System.currentTimeMillis() - start);

        	}
        	catch (Exception ex) {

        	    runtime.incrementFailures();

        	    runtime.markFailed(ex);

        	    throw ex;

        	}
        });
        
    }

    private void printBootOrder(PlatformBootPlan bootPlan) {
        System.out.println("=== Platform Kernel Boot Order ===");

        bootPlan.bootOrder().forEach(runtime ->
                System.out.println("→ " + runtime.name())
        );
    }
}