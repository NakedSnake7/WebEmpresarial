package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

@Service
public class PlanFeatureService {

    public boolean hasFeature(Store store, Feature feature) {

        if (store == null || feature == null) {
            return false;
        }

        if (!store.isActiva()) {
            return false;
        }

        return switch (feature) {

        case PRODUCTS,
             CATEGORIES,
             INVENTORY,
             ORDERS,
             CHECKOUT,
             REVIEWS -> true;

        case CRM,
             LEADS,
             TASKS,
             COUPONS,
             PIPELINE,
             PROPOSALS,
             CUSTOM_DOMAIN,
             STRIPE_CONNECT,
             ANALYTICS -> isProOrPremium(store);

        case EMAIL_MARKETING,
             WHATSAPP_AUTOMATION,
             AUTOMATIONS,
             MULTI_USER,
             API_ACCESS,
             WHITE_LABEL_FULL -> isPremium(store);
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

    private boolean isProOrPremium(Store store) {
        return store.getPlan() == StorePlan.PRO
                || store.getPlan() == StorePlan.PREMIUM;
    }

    private boolean isPremium(Store store) {
        return store.getPlan() == StorePlan.PREMIUM;
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
    
    
    
}