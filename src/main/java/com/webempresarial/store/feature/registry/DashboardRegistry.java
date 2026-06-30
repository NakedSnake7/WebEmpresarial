package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.dashboard.DashboardWidgetDTO;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.ModuleDefinition;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DashboardRegistry {

    private final List<FeatureDefinition> features = new ArrayList<>();
    private final FeatureAccessService featureAccessService;

    public DashboardRegistry(FeatureAccessService featureAccessService) {
        this.featureAccessService = featureAccessService;
    }

    public void register(ModuleDefinition moduleDefinition) {
        if (moduleDefinition == null) {
            return;
        }

        features.addAll(moduleDefinition.getFeatures());
    }

    public List<DashboardWidgetDTO> widgets(Store store) {
        return features.stream()
                .filter(feature -> feature.getPresentation().isShowInDashboard())
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .map(feature -> toDto(feature, store))
                .toList();
    }

    private DashboardWidgetDTO toDto(
            FeatureDefinition feature,
            Store store
    ) {
        boolean locked = !featureAccessService.canUse(
                store,
                feature.getFeature()
        );

        return new DashboardWidgetDTO(
                feature.getDisplayName(),
                feature.getDescription(),
                locked ? "🔒" : feature.getIcon(),
                locked
                        ? "/admin/upgrade?feature=" + feature.getFeature().name()
                        : feature.getUrl(),
                feature.getFeature(),
                locked,
                locked ? "Upgrade" : ""
        );
    }
}