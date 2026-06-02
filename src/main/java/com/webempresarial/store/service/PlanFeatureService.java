package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

@Service
public class PlanFeatureService {

    public boolean canUseCRM(Store store) {
        return store != null && store.isActiva();
    }

    public boolean canUsePipeline(Store store) {
        return hasPlan(store, StorePlan.PRO)
                || hasPlan(store, StorePlan.PREMIUM);
    }

    public boolean canUseProposals(Store store) {
        return hasPlan(store, StorePlan.PRO)
                || hasPlan(store, StorePlan.PREMIUM);
    }

    public boolean canUseAutomations(Store store) {
        return hasPlan(store, StorePlan.PREMIUM);
    }

    public boolean canUseWhiteLabel(Store store) {
        return hasPlan(store, StorePlan.PREMIUM);
    }

    public boolean canUseCustomDomain(Store store) {
        return hasPlan(store, StorePlan.PRO)
                || hasPlan(store, StorePlan.PREMIUM);
    }

    private boolean hasPlan(Store store, StorePlan plan) {
        return store != null
                && store.isActiva()
                && store.getPlan() == plan;
    }
}