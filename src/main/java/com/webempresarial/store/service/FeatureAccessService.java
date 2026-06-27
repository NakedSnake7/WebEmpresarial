package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeatureCatalogService;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;

import org.springframework.stereotype.Service;

@Service
public class FeatureAccessService {

    private final FeatureCatalogService featureCatalogService;

    public FeatureAccessService(
            FeatureCatalogService featureCatalogService
    ) {
        this.featureCatalogService = featureCatalogService;
    }

    public boolean canUse(Store store, Feature feature) {
        return featureCatalogService.hasFeature(store, feature);
    }

    public void requireFeature(Store store, Feature feature) {
        if (!canUse(store, feature)) {
            throw new FeatureLockedException(feature);
        }
    }

    public FeatureDefinition getDefinition(Feature feature) {
        return featureCatalogService.getDefinition(feature);
    }
}