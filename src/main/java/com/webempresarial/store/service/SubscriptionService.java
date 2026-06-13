package com.webempresarial.store.service;

import java.math.BigDecimal;
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
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = new Subscription();

        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.TRIAL);

        subscription.setStartsAt(now);
        subscription.setEndsAt(now.plusDays(14));
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusDays(14));
        subscription.setNextBillingDate(now.plusDays(14));

        store.setPlan(plan);
        store.setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription activate(
            Store store,
            StorePlan plan,
            String stripeCustomerId,
            String stripeSubscriptionId
    ) {
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = subscriptionRepository
                .findByStoreId(store.getId())
                .orElseGet(Subscription::new);

        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscription.setStripeCustomerId(stripeCustomerId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);

        subscription.setStartsAt(now);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));

        store.setPlan(plan);
        store.setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription changePlan(
            Long subscriptionId,
            StorePlan newPlan,
            BigDecimal monthlyAmount
    ) {
        Subscription subscription = findById(subscriptionId);

        subscription.setPlan(newPlan);
        subscription.setMonthlyAmount(monthlyAmount);

        Store store = subscription.getStore();
        store.setPlan(newPlan);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription activateManual(Long subscriptionId) {
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = findById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));

        subscription.getStore().setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription startTrial(
            Long subscriptionId,
            int trialDays
    ) {
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = findById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setStartsAt(now);
        subscription.setEndsAt(now.plusDays(trialDays));
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusDays(trialDays));
        subscription.setNextBillingDate(now.plusDays(trialDays));

        subscription.getStore().setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription cancel(Long storeId) {
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndsAt(LocalDateTime.now());
        subscription.setNextBillingDate(null);

        subscription.getStore().setActiva(false);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription cancelById(Long subscriptionId) {
        Subscription subscription = findById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndsAt(LocalDateTime.now());
        subscription.setNextBillingDate(null);

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
    public Subscription markPastDueById(Long subscriptionId) {
        Subscription subscription = findById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.PAST_DUE);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription expire(Long storeId) {
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setEndsAt(LocalDateTime.now());
        subscription.setNextBillingDate(null);

        subscription.getStore().setActiva(false);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription expireById(Long subscriptionId) {
        Subscription subscription = findById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setEndsAt(LocalDateTime.now());
        subscription.setNextBillingDate(null);

        subscription.getStore().setActiva(false);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription reactivate(Long subscriptionId) {
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = findById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));

        subscription.getStore().setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    public Subscription findById(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
    }
    
    @Transactional
    public Subscription createInternalSubscription(Store store, StorePlan plan) {
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = subscriptionRepository
                .findByStoreId(store.getId())
                .orElseGet(Subscription::new);

        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setMonthlyAmount(BigDecimal.ZERO);
        subscription.setCurrency("MXN");
        subscription.setBillingExempt(true);

        subscription.setStartsAt(now);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(null);
        subscription.setNextBillingDate(null);

        store.setPlan(plan);
        store.setActiva(true);

        return subscriptionRepository.save(subscription);
    }
    
    @Transactional
    public Subscription cancelByStripeSubscriptionId(String stripeSubscriptionId) {

        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new RuntimeException("Suscripción Stripe no encontrada"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndsAt(LocalDateTime.now());
        subscription.setNextBillingDate(null);

        subscription.getStore().setActiva(false);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription registerSuccessfulPayment(String stripeSubscriptionId) {

        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new RuntimeException("Suscripción Stripe no encontrada"));

        LocalDateTime now = LocalDateTime.now();

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));

        subscription.getStore().setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription syncStripeSubscriptionUpdated(
            com.stripe.model.Subscription stripeSubscription
    ) {

        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElseThrow(() -> new RuntimeException("Suscripción Stripe no encontrada"));

        String stripeStatus = stripeSubscription.getStatus();

        if ("active".equalsIgnoreCase(stripeStatus)) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.getStore().setActiva(true);
        }

        if ("past_due".equalsIgnoreCase(stripeStatus)) {
            subscription.setStatus(SubscriptionStatus.PAST_DUE);
        }

        if ("canceled".equalsIgnoreCase(stripeStatus)
                || "cancelled".equalsIgnoreCase(stripeStatus)) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setEndsAt(LocalDateTime.now());
            subscription.setNextBillingDate(null);
            subscription.getStore().setActiva(false);
        }

        if ("incomplete_expired".equalsIgnoreCase(stripeStatus)) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setEndsAt(LocalDateTime.now());
            subscription.setNextBillingDate(null);
            subscription.getStore().setActiva(false);
        }

        return subscriptionRepository.save(subscription);
    }
    
}