package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.sidebar.SidebarItemDTO; 
import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.Store;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SidebarRegistry {

    private final List<SidebarSectionDefinition> sections = new ArrayList<>();
    private final FeatureAccessService featureAccessService;

    public SidebarRegistry(FeatureAccessService featureAccessService) {
        this.featureAccessService = featureAccessService;
    }

    public void register(PlatformModuleDescriptor module) {
        if (module == null) {
            return;
        }

        sections.addAll(module.getSidebarSections());
    }

    public List<SidebarSectionDTO> sections(Store store) {
        return sections.stream()
                .map(section -> toDto(section, store))
                .filter(section -> !section.items().isEmpty())
                .toList();
    }

    private SidebarSectionDTO toDto(
            SidebarSectionDefinition section,
            Store store
    ) {
        List<SidebarItemDTO> visibleItems =
                section.getItems()
                        .stream()
                        .map(item -> toDto(item, store))
                        .toList();

        return new SidebarSectionDTO(
                section.getTitle(),
                section.getIcon(),
                visibleItems
        );
    }

    private SidebarItemDTO toDto(
            SidebarItemDefinition item,
            Store store
    ) {
        boolean locked = !featureAccessService.canUse(
                store,
                item.feature()
        );

        return new SidebarItemDTO(
                item.title(),
                locked ? "🔒" : item.icon(),
                locked
                        ? "/admin/upgrade?feature=" + item.feature().name()
                        : item.url(),
                item.feature(),
                locked,
                locked ? "Upgrade" : ""
        );
    }

}