package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.runtime.ModuleRuntime;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ModuleRuntimeRegistry {

    private final List<ModuleRuntime> runtimes = new ArrayList<>();

    public void register(ModuleRuntime runtime) {
        if (runtime != null) {
            runtimes.add(runtime);
        }
    }

    public List<ModuleRuntime> all() {
        return runtimes.stream()
                .sorted(Comparator.comparing(ModuleRuntime::name))
                .toList();
    }

    public Optional<ModuleRuntime> findByName(String name) {
        return runtimes.stream()
                .filter(runtime -> runtime.name().equalsIgnoreCase(name))
                .findFirst();
    }
}