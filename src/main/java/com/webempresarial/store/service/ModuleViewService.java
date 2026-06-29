package com.webempresarial.store.service;

import com.webempresarial.store.dto.module.ModuleCardDTO;
import com.webempresarial.store.feature.registry.ModuleRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleViewService {

    private final ModuleRegistry moduleRegistry;

    public ModuleViewService(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public List<ModuleCardDTO> modules() {
        return moduleRegistry.all()
                .stream()
                .map(module -> new ModuleCardDTO(
                        module.getName(),
                        module.getDescription(),
                        module.getFeatures().size(),
                        module.getSidebarSections().size()
                ))
                .toList();
    }
}