package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeatureRegistry;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import org.springframework.stereotype.Service;

@Service
public class FeatureAccessService {

    private final PlanFeatureService planFeatureService;
    private final FeatureRegistry featureRegistry;

    public FeatureAccessService(
            PlanFeatureService planFeatureService,
            FeatureRegistry featureRegistry
    ) {
        this.planFeatureService = planFeatureService;
        this.featureRegistry = featureRegistry;
    }

    public boolean canUse(Store store, Feature feature) {
        return planFeatureService.hasFeature(store, feature);
    }

    public void requireFeature(Store store, Feature feature) {
        if (!canUse(store, feature)) {
            throw new FeatureLockedException(feature);
        }
    }

    public FeatureDefinition getDefinition(Feature feature) {
        return featureRegistry.get(feature);
    }
}