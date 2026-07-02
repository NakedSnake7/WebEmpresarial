package com.webempresarial.store.feature.dependency;

import com.webempresarial.store.feature.runtime.ModuleRuntime;
import com.webempresarial.store.feature.registry.ModuleRuntimeRegistry;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PlatformBootPlanner {

    private final ModuleRuntimeRegistry runtimeRegistry;
    private final ModuleDependencyResolver dependencyResolver;

    public PlatformBootPlanner(
            ModuleRuntimeRegistry runtimeRegistry,
            ModuleDependencyResolver dependencyResolver
    ) {
        this.runtimeRegistry = runtimeRegistry;
        this.dependencyResolver = dependencyResolver;
    }

    public PlatformBootPlan plan() {
        ModuleDependencyReport report = dependencyResolver.validate();

        if (!report.valid()) {
            throw new IllegalStateException(
                    "Error de dependencias en Platform Kernel. Missing="
                            + report.missingDependencies()
                            + ", Cycles="
                            + report.cycles()
            );
        }

        List<ModuleRuntime> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (ModuleRuntime runtime : runtimeRegistry.all()) {
            visit(runtime, visited, ordered);
        }

        return new PlatformBootPlan(ordered);
    }

    private void visit(
            ModuleRuntime runtime,
            Set<String> visited,
            List<ModuleRuntime> ordered
    ) {
        if (visited.contains(runtime.name())) {
            return;
        }

        visited.add(runtime.name());

        for (String dependency : runtime.descriptor().getManifest().getDependencies()) {
            runtimeRegistry.findByName(dependency)
                    .ifPresent(dep -> visit(dep, visited, ordered));
        }

        ordered.add(runtime);
    }
}