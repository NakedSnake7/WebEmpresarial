package com.webempresarial.store.feature;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureCatalogService {

    private final FeatureRegistry featureRegistry;

    public FeatureCatalogService(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    public boolean hasFeature(Store store, Feature feature) {
        return featureRegistry.hasFeature(store, feature);
    }

    public boolean isLocked(Store store, Feature feature) {
        return featureRegistry.isLocked(store, feature);
    }

    public FeatureDefinition getDefinition(Feature feature) {
        return featureRegistry.get(feature);
    }

    public StorePlan getRequiredPlan(Feature feature) {
        return getDefinition(feature).getMinimumPlan();
    }

    public List<FeatureDefinition> getAllFeatures() {
        return featureRegistry.getAllOrdered();
    }

    public List<FeatureDefinition> getAvailableFeatures(Store store) {
        return featureRegistry.available(store);
    }

    public List<FeatureDefinition> getLockedFeatures(Store store) {
        return featureRegistry.locked(store);
    }

    public List<FeatureDefinition> getSidebarFeatures(Store store) {
        return featureRegistry.sidebar(store);
    }

    public List<FeatureDefinition> getDashboardFeatures(Store store) {
        return featureRegistry.dashboard(store);
    }

    public List<FeatureDefinition> getUpgradeFeatures(Store store) {
        return featureRegistry.upgrades(store);
    }

    public List<FeatureDefinition> getTrackableFeatures(Store store) {
        return featureRegistry.trackable(store);
    }

    public List<FeatureDefinition> getHealthFeatures(Store store) {
        return featureRegistry.health(store);
    }

    public List<FeatureDefinition> getBillingFeatures(Store store) {
        return featureRegistry.billing(store);
    }

    // Temporal: útil para pintar planes en /admin/billing
    public List<FeatureDefinition> getBillingFeatures(StorePlan plan) {
        if (plan == null) {
            return List.of();
        }

        return featureRegistry.getAvailableForPlan(plan)
                .stream()
                .filter(FeatureDefinition::isShowInBilling)
                .toList();
    }
}