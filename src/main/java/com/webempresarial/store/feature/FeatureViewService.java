package com.webempresarial.store.feature;

import com.webempresarial.store.dto.feature.FeatureCardDTO;
import com.webempresarial.store.dto.feature.FeatureSectionDTO;
import com.webempresarial.store.model.Store;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeatureViewService {

    private final FeatureCatalogService featureCatalogService;

    public FeatureViewService(FeatureCatalogService featureCatalogService) {
        this.featureCatalogService = featureCatalogService;
    }

    public List<FeatureCardDTO> sidebar(Store store) {
        return featureCatalogService.getSidebarFeatures(store)
                .stream()
                .map(definition -> toCard(definition, store))
                .toList();
    }

    public List<FeatureCardDTO> available(Store store) {
        return featureCatalogService.getAvailableFeatures(store)
                .stream()
                .map(definition -> toCard(definition, store))
                .toList();
    }

    public List<FeatureCardDTO> locked(Store store) {
        return featureCatalogService.getLockedFeatures(store)
                .stream()
                .map(definition -> toCard(definition, store))
                .toList();
    }

    public List<FeatureSectionDTO> sidebarSections(Store store) {

        List<FeatureCardDTO> features = sidebar(store);

        Map<String, List<FeatureCardDTO>> grouped =
                features.stream()
                        .collect(Collectors.groupingBy(
                                FeatureCardDTO::section,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return grouped.entrySet()
                .stream()
                .map(entry -> new FeatureSectionDTO(
                        entry.getKey(),
                        resolveSectionIcon(entry.getValue()),
                        entry.getValue()
                ))
                .toList();
    }

    private FeatureCardDTO toCard(
            FeatureDefinition definition,
            Store store
    ) {
        boolean available =
                featureCatalogService.hasFeature(
                        store,
                        definition.getFeature()
                );

        boolean locked = !available;

        return new FeatureCardDTO(
                definition.getFeature(),
                definition.getDisplayName(),
                definition.getDescription(),
                definition.getIcon(),
                definition.getColor(),
                definition.getUrl(),

                definition.getSection(),
                definition.getSectionIcon(),

                definition.getMinimumPlan(),

                available,
                definition.isPremium(),
                locked,
                definition.isShowInSidebar(),
                definition.isShowInDashboard(),
                definition.isShowInBilling(),
                definition.isShowUpgradeCard(),
                definition.isTrackUsage(),

                resolveBadge(available, definition)
        );
    }

    private String resolveSectionIcon(List<FeatureCardDTO> features) {
        if (features == null || features.isEmpty()) {
            return "📁";
        }

        return features.get(0).sectionIcon();
    }

    private String resolveBadge(
            boolean available,
            FeatureDefinition definition
    ) {
        if (available) {
            return "Disponible";
        }

        return "Requiere " + definition.getMinimumPlan().name();
    }
}