package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.FeatureLockedException; 
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.PlatformKernel;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;

import org.springframework.stereotype.Service;

@Service
public class FeatureAccessService {

    private final PlatformKernel platformKernel;

    public FeatureAccessService(
            PlatformKernel platformKernel
    ) {
        this.platformKernel = platformKernel;
    }

    public boolean canUse(Store store, String featureCode) {
        try {
            return canUse(
                    store,
                    Feature.valueOf(featureCode)
            );
        } catch (Exception ex) {
            return false;
        }
    }
    public boolean canUse(Store store, Feature feature) {
        return platformKernel.hasFeature(store, feature);
    }

    public void requireFeature(Store store, Feature feature) {
        if (!canUse(store, feature)) {
            throw new FeatureLockedException(feature);
        }
    }

    public FeatureDefinition definition(Feature feature) {
        return platformKernel.get(feature);
    }

    public boolean isLocked(Store store, Feature feature) {
        return platformKernel.isLocked(store, feature);
    }
}