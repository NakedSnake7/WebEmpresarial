package com.webempresarial.store.service;

import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import org.springframework.stereotype.Service;

@Service
public class FeatureAccessService {

    private final PlanFeatureService planFeatureService;

    public FeatureAccessService(PlanFeatureService planFeatureService) {
        this.planFeatureService = planFeatureService;
    }

    public void requireFeature(Store store, Feature feature) {
        if (!planFeatureService.hasFeature(store, feature)) {
            throw new FeatureLockedException(feature);
        }
    }
}