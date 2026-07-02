package com.webempresarial.store.feature.registry;

import com.webempresarial.store.feature.PlatformModuleDescriptor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ModuleRegistry {

    private final List<PlatformModuleDescriptor> modules = new ArrayList<>();

    public void register(PlatformModuleDescriptor module) {
        if (module == null) {
            return;
        }

        modules.add(module);
    }

    public List<PlatformModuleDescriptor> all() {
        return modules.stream()
                .sorted(Comparator.comparing(PlatformModuleDescriptor::getName))
                .toList();
    }

    public Optional<PlatformModuleDescriptor> findByName(String name) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public int count() {
        return modules.size();
    }

    public int featureCount() {
        return modules.stream()
                .mapToInt(module -> module.getFeatures().size())
                .sum();
    }

    public int sidebarSectionCount() {
        return modules.stream()
                .mapToInt(module -> module.getSidebarSections().size())
                .sum();
    }

    public int dashboardWidgetCount() {
        return modules.stream()
                .mapToInt(module ->
                        (int) module.getFeatures()
                                .stream()
                                .filter(feature -> feature.getPresentation().isShowInDashboard())
                                .count()
                )
                .sum();
    }
}