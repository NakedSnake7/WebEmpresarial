package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.marketplace.MarketplaceModuleDTO;
import com.webempresarial.store.feature.ModuleDefinition;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarketplaceRegistry {

    private final ModuleRegistry moduleRegistry;

    public MarketplaceRegistry(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public List<MarketplaceModuleDTO> modules() {
        return moduleRegistry.all()
                .stream()
                .map(module -> new MarketplaceModuleDTO(
                        module.getName(),
                        module.getDescription(),
                        module.getFeatures().size(),
                        module.getSidebarSections().size(),
                        module.getFeatures()
                                .stream()
                                .filter(f -> f.getPresentation().isShowInDashboard())
                                .toList()
                                .size(),
                        true,
                        "Instalado"
                ))
                .toList();
    }
}