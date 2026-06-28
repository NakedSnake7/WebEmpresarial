package com.webempresarial.store.feature;

import com.webempresarial.store.feature.registry.SidebarRegistry;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeatureRegistryConfiguration {

    private final PlatformKernel platformKernel;
    private final SidebarRegistry sidebarRegistry;
    private final List<PlatformModule> modules;

    public FeatureRegistryConfiguration(
            PlatformKernel platformKernel,
            SidebarRegistry sidebarRegistry,
            List<PlatformModule> modules
    ) {
        this.platformKernel = platformKernel;
        this.sidebarRegistry = sidebarRegistry;
        this.modules = modules;
    }

    @PostConstruct
    public void registerModules() {
        modules.forEach(module -> {
            ModuleDefinition definition = module.definition();

            definition.getFeatures()
                    .forEach(platformKernel::register);

            sidebarRegistry.register(definition);
        });
    }
}