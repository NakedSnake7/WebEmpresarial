package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.SubscriptionStatus;

@Service
public class SubscriptionAccessService {

    public boolean canAccessPlatform(Subscription subscription) {

        if (subscription == null) {
            return false;
        }

        return switch (subscription.getStatus()) {

            case ACTIVE,
                 TRIAL -> true;

            case EXPIRED,
                 CANCELLED,
                 PAST_DUE -> false;
        };
    }
}