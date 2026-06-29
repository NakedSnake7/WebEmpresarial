package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.ModuleDefinition;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ModuleRegistry {

    private final List<ModuleDefinition> modules = new ArrayList<>();

    public void register(ModuleDefinition module) {
        if (module == null) {
            return;
        }

        modules.add(module);
    }

    public List<ModuleDefinition> all() {
        return modules.stream()
                .sorted(Comparator.comparing(ModuleDefinition::getName))
                .toList();
    }
}