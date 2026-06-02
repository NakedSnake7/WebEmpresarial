package com.webempresarial.store.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.SubscriptionRepository;

import jakarta.transaction.Transactional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public Subscription createTrial(Store store, StorePlan plan) {
        Subscription subscription = new Subscription();

        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setStartsAt(LocalDateTime.now());
        subscription.setEndsAt(LocalDateTime.now().plusDays(14));
        subscription.setNextBillingDate(LocalDateTime.now().plusDays(14));

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription activate(
            Store store,
            StorePlan plan,
            String stripeCustomerId,
            String stripeSubscriptionId
    ) {
        Subscription subscription = subscriptionRepository
                .findByStoreId(store.getId())
                .orElseGet(Subscription::new);

        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscription.setStartsAt(LocalDateTime.now());
        subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));

        store.setPlan(plan);
        store.setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription cancel(Long storeId) {
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndsAt(LocalDateTime.now());

        subscription.getStore().setActiva(false);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription markPastDue(String stripeSubscriptionId) {
        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new RuntimeException("Suscripción Stripe no encontrada"));

        subscription.setStatus(SubscriptionStatus.PAST_DUE);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription expire(Long storeId) {
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setEndsAt(LocalDateTime.now());

        subscription.getStore().setActiva(false);

        return subscriptionRepository.save(subscription);
    }
}