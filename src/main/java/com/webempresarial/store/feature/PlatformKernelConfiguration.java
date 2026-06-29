package com.webempresarial.store.feature;

import com.webempresarial.store.feature.registry.ModuleRegistry;
import com.webempresarial.store.feature.registry.SidebarRegistry;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlatformKernelConfiguration {

    private final PlatformKernel platformKernel;
    private final ModuleRegistry moduleRegistry;
    private final SidebarRegistry sidebarRegistry;
    private final List<PlatformModule> modules;

    public PlatformKernelConfiguration(
            PlatformKernel platformKernel,
            ModuleRegistry moduleRegistry,
            SidebarRegistry sidebarRegistry,
            List<PlatformModule> modules
    ) {
        this.platformKernel = platformKernel;
        this.moduleRegistry = moduleRegistry;
        this.sidebarRegistry = sidebarRegistry;
        this.modules = modules;
    }

    @PostConstruct
    public void registerModules() {
        modules.forEach(module -> {
            ModuleDefinition definition = module.definition();

            moduleRegistry.register(definition);

            definition.getFeatures()
                    .forEach(platformKernel::register);

            sidebarRegistry.register(definition);
        });
    }
}