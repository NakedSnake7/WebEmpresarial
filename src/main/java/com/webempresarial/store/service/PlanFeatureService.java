package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.feature.FeatureRegistry;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

@Service
public class PlanFeatureService {

    private final FeatureRegistry featureRegistry;

    public PlanFeatureService(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    public boolean hasFeature(Store store, Feature feature) {

        if (store == null || feature == null) {
            return false;
        }

        if (!store.isActiva()) {
            return false;
        }

        if (!featureRegistry.isRegistered(feature)) {
            return false;
        }

        StorePlan currentPlan = store.getPlan();
        StorePlan requiredPlan = featureRegistry
                .get(feature)
                .getMinimumPlan();

        return hasPlanAccess(currentPlan, requiredPlan);
    }

    private boolean hasPlanAccess(
            StorePlan currentPlan,
            StorePlan requiredPlan
    ) {
        if (currentPlan == null || requiredPlan == null) {
            return false;
        }

        return planRank(currentPlan) >= planRank(requiredPlan);
    }

    private int planRank(StorePlan plan) {
        return switch (plan) {
            case BASIC -> 1;
            case PRO -> 2;
            case PREMIUM -> 3;
        };
    }

    public boolean canUseCRM(Store store) {
        return hasFeature(store, Feature.CRM);
    }

    public boolean canUsePipeline(Store store) {
        return hasFeature(store, Feature.PIPELINE);
    }

    public boolean canUseProposals(Store store) {
        return hasFeature(store, Feature.PROPOSALS);
    }

    public boolean canUseAutomations(Store store) {
        return hasFeature(store, Feature.AUTOMATIONS);
    }

    public boolean canUseWhiteLabel(Store store) {
        return hasFeature(store, Feature.WHITE_LABEL_FULL);
    }

    public boolean canUseCustomDomain(Store store) {
        return hasFeature(store, Feature.CUSTOM_DOMAIN);
    }

    public boolean canUseAnalytics(Store store) {
        return hasFeature(store, Feature.ANALYTICS);
    }

    public boolean canUseStripeConnect(Store store) {
        return hasFeature(store, Feature.STRIPE_CONNECT);
    }

    public boolean canUseEmailMarketing(Store store) {
        return hasFeature(store, Feature.EMAIL_MARKETING);
    }

    public boolean canUseMultiUser(Store store) {
        return hasFeature(store, Feature.MULTI_USER);
    }

    public boolean canUseApiAccess(Store store) {
        return hasFeature(store, Feature.API_ACCESS);
    }

    public boolean canUseWhatsappAutomation(Store store) {
        return hasFeature(store, Feature.WHATSAPP_AUTOMATION);
    }
}