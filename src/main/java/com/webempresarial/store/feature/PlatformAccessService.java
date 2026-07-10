package com.webempresarial.store.feature;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.SubscriptionRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PlatformAccessService {

    private static final String ATTR_PREFIX = "PLATFORM_EFFECTIVE_PLAN_";

    private final SubscriptionRepository subscriptionRepository;
    private final HttpServletRequest request;

    public PlatformAccessService(
            SubscriptionRepository subscriptionRepository,
            HttpServletRequest request
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.request = request;
    }

    public StorePlan resolveEffectivePlan(Store store) {

        if (store == null || store.getId() == null) {
            return StorePlan.BASIC;
        }

        String key = ATTR_PREFIX + store.getId();

        Object cached = request.getAttribute(key);

        if (cached instanceof StorePlan plan) {
            return plan;
        }

        StorePlan effectivePlan = resolveEffectivePlanFromDatabase(store);

        request.setAttribute(key, effectivePlan);

        return effectivePlan;
    }

    private StorePlan resolveEffectivePlanFromDatabase(Store store) {

        Subscription subscription = getEffectiveSubscription(store);

        if (subscription == null) {
            return store.getPlan() != null ? store.getPlan() : StorePlan.BASIC;
        }

        if (!canAccessPlatform(subscription)) {
            return StorePlan.BASIC;
        }

        return subscription.getPlan() != null
                ? subscription.getPlan()
                : StorePlan.BASIC;
    }

    public boolean hasPlatformAccess(Store store) {

        if (store == null || store.getId() == null || !store.isActiva()) {
            return false;
        }

        Subscription subscription = getEffectiveSubscription(store);

        return canAccessPlatform(subscription);
    }

    public boolean canAccessPlatform(Subscription subscription) {

        if (subscription == null || subscription.getStatus() == null) {
            return false;
        }

        return subscription.canAccessPlatform();
    }

    public Subscription getEffectiveSubscription(Store store) {

        if (store == null || store.getId() == null) {
            return null;
        }

        String key = "PLATFORM_SUBSCRIPTION_" + store.getId();

        Object cached = request.getAttribute(key);

        if (cached instanceof Subscription subscription) {
            return subscription;
        }

        Subscription subscription = subscriptionRepository
                .findByStoreId(store.getId())
                .orElse(null);

        if (subscription != null) {
            request.setAttribute(key, subscription);
        }

        return subscription;
    }
}