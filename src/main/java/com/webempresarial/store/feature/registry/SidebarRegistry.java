package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.sidebar.SidebarItemDTO;
import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SidebarRegistry {

    private final List<SidebarSectionDefinition> sections =
            new ArrayList<>();

    private final FeatureAccessService featureAccessService;

    private final CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    @Autowired
    public SidebarRegistry(
            FeatureAccessService featureAccessService,
            CurrentAdminAuthorizationService
                    currentAdminAuthorizationService
    ) {
        this.featureAccessService = featureAccessService;
        this.currentAdminAuthorizationService =
                currentAdminAuthorizationService;
    }

    /*
     * Constructor conservado para pruebas unitarias legacy.
     *
     * Los items que no declaran requiredPermission siguen
     * funcionando exactamente como antes.
     */
    SidebarRegistry(
            FeatureAccessService featureAccessService
    ) {
        this(
                featureAccessService,
                null
        );
    }

    public void register(PlatformModuleDescriptor module) {
        if (module == null) {
            return;
        }

        sections.addAll(module.getSidebarSections());
    }

    public List<SidebarSectionDTO> sections(
            Store store,
            AdminRole role
    ) {
        return sections.stream()
                .map(section ->
                        toDto(
                                section,
                                store,
                                role
                        )
                )
                .filter(section ->
                        !section.items().isEmpty()
                )
                .toList();
    }

    private SidebarSectionDTO toDto(
            SidebarSectionDefinition section,
            Store store,
            AdminRole role
    ) {

        List<SidebarItemDTO> visibleItems =
                section.getItems()
                        .stream()
                        .filter(item ->
                                item.allows(role)
                        )
                        .filter(this::hasRequiredPermission)
                        .map(item ->
                                toDto(
                                        item,
                                        store
                                )
                        )
                        .toList();

        return new SidebarSectionDTO(
                section.getTitle(),
                section.getIcon(),
                visibleItems
        );
    }

    private boolean hasRequiredPermission(
            SidebarItemDefinition item
    ) {
        if (item.requiredPermission() == null) {
            return true;
        }

        return currentAdminAuthorizationService != null
                && currentAdminAuthorizationService
                        .hasPermission(
                                item.requiredPermission()
                        );
    }

    private SidebarItemDTO toDto(
            SidebarItemDefinition item,
            Store store
    ) {

        boolean locked =
                !featureAccessService.canUse(
                        store,
                        item.feature()
                );

        return new SidebarItemDTO(
                item.title(),
                locked
                        ? "🔒"
                        : item.icon(),
                locked
                        ? "/admin/upgrade?feature="
                            + item.feature().name()
                        : item.url(),
                item.feature(),
                locked,
                locked
                        ? "Upgrade"
                        : ""
        );
    }
}
