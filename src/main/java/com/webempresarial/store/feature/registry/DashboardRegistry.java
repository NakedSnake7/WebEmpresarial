package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.dashboard.DashboardWidgetDTO;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DashboardRegistry {

    private final List<FeatureDefinition> features =
            new ArrayList<>();

    private final FeatureAccessService featureAccessService;

    private final CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    @Autowired
    public DashboardRegistry(
            FeatureAccessService featureAccessService,
            CurrentAdminAuthorizationService
                    currentAdminAuthorizationService
    ) {
        this.featureAccessService = featureAccessService;
        this.currentAdminAuthorizationService =
                currentAdminAuthorizationService;
    }

    /*
     * Compatibilidad para pruebas/construcciones legacy.
     */
    DashboardRegistry(
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

        features.addAll(module.getFeatures());
    }

    public List<DashboardWidgetDTO> widgets(Store store) {
        return features.stream()
                .filter(feature ->
                        feature
                                .getPresentation()
                                .isShowInDashboard()
                )
                .filter(this::hasDashboardPermission)
                .sorted(
                        Comparator.comparingInt(
                                FeatureDefinition::getOrder
                        )
                )
                .map(feature ->
                        toDto(
                                feature,
                                store
                        )
                )
                .toList();
    }

    private boolean hasDashboardPermission(
            FeatureDefinition feature
    ) {

        if (feature.getDashboardPermission() == null) {
            return true;
        }

        return currentAdminAuthorizationService != null
                && currentAdminAuthorizationService
                        .hasPermission(
                                feature.getDashboardPermission()
                        );
    }

    private DashboardWidgetDTO toDto(
            FeatureDefinition feature,
            Store store
    ) {

        boolean locked =
                !featureAccessService.canUse(
                        store,
                        feature.getFeature()
                );

        return new DashboardWidgetDTO(
                feature.getDisplayName(),
                feature.getDescription(),
                locked
                        ? "🔒"
                        : feature.getIcon(),
                locked
                        ? "/admin/upgrade?feature="
                            + feature.getFeature().name()
                        : feature.getUrl(),
                feature.getFeature(),
                locked,
                locked
                        ? "Upgrade"
                        : ""
        );
    }
}
