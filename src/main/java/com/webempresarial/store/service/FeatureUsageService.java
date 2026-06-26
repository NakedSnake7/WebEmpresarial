package com.webempresarial.store.service;

import com.webempresarial.store.entity.FeatureUsage;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.FeatureUsageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeatureUsageService {

    private final FeatureUsageRepository featureUsageRepository;

    public FeatureUsageService(
            FeatureUsageRepository featureUsageRepository
    ) {
        this.featureUsageRepository = featureUsageRepository;
    }

    @Transactional
    public void track(Store store, Feature feature) {
        track(store, feature, null);
    }

    @Transactional
    public void track(
            Store store,
            Feature feature,
            String context
    ) {
        if (store == null || feature == null) {
            return;
        }

        FeatureUsage usage = new FeatureUsage();
        usage.setStore(store);
        usage.setFeature(feature);
        usage.setContext(context);

        featureUsageRepository.save(usage);
    }
}