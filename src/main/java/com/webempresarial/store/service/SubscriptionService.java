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
	private final StripePlanMapper stripePlanMapper;

	public SubscriptionService(
	        SubscriptionRepository subscriptionRepository,
	        StripePlanMapper stripePlanMapper
	) {
	    this.subscriptionRepository = subscriptionRepository;
	    this.stripePlanMapper = stripePlanMapper;
	}
    private Subscription findByStripeSubscriptionOrCustomer(
            com.stripe.model.Subscription stripeSubscription
    ) {
        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElse(null);

        if (subscription != null) {
            return subscription;
        }

        String stripeCustomerId = stripeSubscription.getCustomer();

        if (stripeCustomerId == null || stripeCustomerId.isBlank()) {
            return null;
        }

        return subscriptionRepository
                .findByStripeCustomerId(stripeCustomerId)
                .orElse(null);
    }
    private LocalDateTime fromStripeEpoch(Long epochSeconds) {
        if (epochSeconds == null) {
            return null;
        }

        return java.time.Instant
                .ofEpochSecond(epochSeconds)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    private String resolveStripePriceId(
            com.stripe.model.Subscription stripeSubscription
    ) {
        if (stripeSubscription.getItems() == null ||
                stripeSubscription.getItems().getData() == null ||
                stripeSubscription.getItems().getData().isEmpty()) {
            return null;
        }

        var item = stripeSubscription.getItems().getData().get(0);

        if (item.getPrice() == null) {
            return null;
        }

        return item.getPrice().getId();
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
            String stripeSubscriptionId,
            String stripePriceId
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
        subscription.setStripePriceId(stripePriceId);

        subscription.setStartsAt(now);
        subscription.setEndsAt(null);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusMonths(1));
        subscription.setNextBillingDate(now.plusMonths(1));

        subscription.setBillingExempt(false);
        subscription.setCancelAtPeriodEnd(false);

        subscription.setPendingPlan(null);
        subscription.setPendingPlanEffectiveAt(null);
        subscription.setPendingPlan(null);
        subscription.setPendingPlanEffectiveAt(null);

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
                .orElse(null);
        if (subscription == null) {
            return null;
        }
        
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
     // Valores temporales.
     // El webhook customer.subscription.updated los reemplazará
     // por los periodos reales enviados por Stripe.
     subscription.setCurrentPeriodStart(now);
     subscription.setCurrentPeriodEnd(now.plusMonths(1));
     subscription.setNextBillingDate(now.plusMonths(1));
     subscription.setBillingExempt(false);

     subscription.setPendingPlan(null);
     subscription.setPendingPlanEffectiveAt(null);

     subscription.setCancelAtPeriodEnd(false);

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
                .orElse(null);

        if (subscription == null) {
            return null;
        }

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
                .orElse(null);

        if (subscription == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndsAt(null);

        if (subscription.getCurrentPeriodStart() == null) {
            subscription.setCurrentPeriodStart(now);
        }

        subscription.setCurrentPeriodEnd(
                subscription.getCurrentPeriodEnd() != null
                        ? subscription.getCurrentPeriodEnd()
                        : now.plusMonths(1)
        );

        subscription.setNextBillingDate(
                subscription.getCurrentPeriodEnd()
        );

        subscription.setNextBillingDate(subscription.getCurrentPeriodEnd());
        subscription.getStore().setActiva(true);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription syncStripeSubscriptionUpdated(
            com.stripe.model.Subscription stripeSubscription
    ) {

        Subscription subscription =
                findByStripeSubscriptionOrCustomer(stripeSubscription);

        if (subscription == null) {
            return null;
        }

        subscription.setStripeSubscriptionId(
                stripeSubscription.getId()
        );

        if (stripeSubscription.getCustomer() != null &&
            !stripeSubscription.getCustomer().isBlank()) {

            subscription.setStripeCustomerId(
                    stripeSubscription.getCustomer()
            );
        }

        String stripeStatus = stripeSubscription.getStatus();

        String stripePriceId =
                resolveStripePriceId(stripeSubscription);

        if (stripePriceId != null && !stripePriceId.isBlank()) {

        	StorePlan synchronizedPlan =
        	        stripePlanMapper.getPlanByPriceId(stripePriceId);

        	subscription.setStripePriceId(stripePriceId);

        	if (subscription.getPendingPlan() == null) {

        	    subscription.setPlan(synchronizedPlan);
        	    subscription.getStore().setPlan(synchronizedPlan);

        	} else if (subscription.getPendingPlan() == synchronizedPlan) {

        	    subscription.setPlan(synchronizedPlan);
        	    subscription.getStore().setPlan(synchronizedPlan);

        	    subscription.setPendingPlan(null);
        	    subscription.setPendingPlanEffectiveAt(null);
        	}
        }

        LocalDateTime periodStart =
                fromStripeEpoch(
                        stripeSubscription.getCurrentPeriodStart()
                );

        LocalDateTime periodEnd =
                fromStripeEpoch(
                        stripeSubscription.getCurrentPeriodEnd()
                );

        subscription.setCurrentPeriodStart(periodStart);
        subscription.setCurrentPeriodEnd(periodEnd);
        subscription.setNextBillingDate(periodEnd);

        if (subscription.getStartsAt() == null) {
            subscription.setStartsAt(periodStart);
        }

        subscription.setNextBillingDate(
                fromStripeEpoch(
                        stripeSubscription.getCurrentPeriodEnd()
                )
        );

        boolean cancelAtPeriodEnd =
                Boolean.TRUE.equals(
                        stripeSubscription.getCancelAtPeriodEnd()
                );

        subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        
        switch (stripeStatus.toLowerCase()) {

            case "trialing" -> {
                subscription.setStatus(SubscriptionStatus.TRIAL);
                subscription.getStore().setActiva(true);
            }

            case "active" -> {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setEndsAt(null);
                subscription.getStore().setActiva(true);
            }

            case "past_due" -> {
                subscription.setStatus(SubscriptionStatus.PAST_DUE);
                subscription.getStore().setActiva(true);
            }

            case "canceled", "cancelled" -> {
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscription.setEndsAt(LocalDateTime.now());
                subscription.setNextBillingDate(null);
                subscription.getStore().setActiva(false);
            }

            case "unpaid", "incomplete_expired", "paused" -> {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscription.setEndsAt(LocalDateTime.now());
                subscription.setNextBillingDate(null);
                subscription.getStore().setActiva(false);
            }

            case "incomplete" -> {
                subscription.setStatus(SubscriptionStatus.PAST_DUE);
                subscription.getStore().setActiva(true);
            }

            default -> {
                return subscriptionRepository.save(subscription);
            }
        }

        return subscriptionRepository.save(subscription);
    }
    



    
}