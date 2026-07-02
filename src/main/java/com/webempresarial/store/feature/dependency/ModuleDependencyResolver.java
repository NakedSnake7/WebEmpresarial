package com.webempresarial.store.feature.dependency;

import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.registry.ModuleRegistry;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ModuleDependencyResolver {

    private final ModuleRegistry moduleRegistry;

    public ModuleDependencyResolver(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public ModuleDependencyReport validate() {
        List<String> missing = new ArrayList<>();
        List<String> cycles = new ArrayList<>();

        List<PlatformModuleDescriptor> modules = moduleRegistry.all();

        Set<String> moduleNames = new HashSet<>(
                modules.stream()
                        .map(PlatformModuleDescriptor::getName)
                        .toList()
        );

        for (PlatformModuleDescriptor module : modules) {
            for (String dependency : module.getManifest().getDependencies()) {
                if (!moduleNames.contains(dependency)) {
                    missing.add(module.getName() + " depende de " + dependency);
                }
            }
        }

        // Detección simple de ciclos directos
        for (PlatformModuleDescriptor module : modules) {
            for (String dependency : module.getManifest().getDependencies()) {
                moduleRegistry.findByName(dependency).ifPresent(dep -> {
                    if (dep.getManifest().getDependencies().contains(module.getName())) {
                        cycles.add(module.getName() + " ↔ " + dep.getName());
                    }
                });
            }
        }

        return new ModuleDependencyReport(
                missing.isEmpty() && cycles.isEmpty(),
                missing,
                cycles
        );
    }
}