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

    private static final int
            STRIPE_EVENT_PRIORITY_INVOICE_FAILED = 100;

    private static final int
            STRIPE_EVENT_PRIORITY_INVOICE_PAID = 200;

    private static final int
            STRIPE_EVENT_PRIORITY_SUBSCRIPTION_CREATED = 300;

    private static final int
            STRIPE_EVENT_PRIORITY_SUBSCRIPTION_UPDATED = 350;

    private static final int
            STRIPE_EVENT_PRIORITY_SUBSCRIPTION_DELETED = 400;
	
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

    private Subscription findByStripeSubscriptionOrCustomerForUpdate(
            com.stripe.model.Subscription stripeSubscription
    ) {
        String stripeSubscriptionId =
                stripeSubscription.getId();

        if (stripeSubscriptionId != null
                && !stripeSubscriptionId.isBlank()) {

            Subscription subscription =
                    subscriptionRepository
                            .findByStripeSubscriptionIdForUpdate(
                                    stripeSubscriptionId
                            )
                            .orElse(null);

            if (subscription != null) {
                return subscription;
            }
        }

        String stripeCustomerId =
                stripeSubscription.getCustomer();

        if (stripeCustomerId == null
                || stripeCustomerId.isBlank()) {
            return null;
        }

        return subscriptionRepository
                .findByStripeCustomerIdForUpdate(
                        stripeCustomerId
                )
                .orElse(null);
    }

    private Subscription requireStripeSubscriptionForUpdate(
            String stripeSubscriptionId
    ) {
        Subscription subscription =
                subscriptionRepository
                        .findByStripeSubscriptionIdForUpdate(
                                stripeSubscriptionId
                        )
                        .orElse(null);

        if (subscription == null) {
            throw new IllegalStateException(
                    "Suscripción Stripe local no encontrada: "
                            + stripeSubscriptionId
            );
        }

        return subscription;
    }

    private boolean shouldApplyStripeEvent(
            Subscription subscription,
            Long eventCreated,
            int eventPriority,
            String eventType
    ) {
        if (eventCreated == null) {
            throw new IllegalStateException(
                    "Stripe event sin created: "
                            + eventType
            );
        }

        Long lastCreated =
                subscription.getLastStripeEventCreated();

        if (lastCreated == null) {
            return true;
        }

        int createdComparison =
                eventCreated.compareTo(lastCreated);

        if (createdComparison > 0) {
            return true;
        }

        if (createdComparison < 0) {
            return false;
        }

        Integer lastPriority =
                subscription.getLastStripeEventPriority();

        if (lastPriority == null) {
            return true;
        }

        /*
         * Stripe Event.created tiene precisión de segundos.
         *
         * En empate, eventos semánticamente más fuertes ganan:
         *
         * deleted > subscription update > paid > payment_failed.
         */
        return eventPriority >= lastPriority;
    }

    private void recordStripeEventOrder(
            Subscription subscription,
            Long eventCreated,
            int eventPriority,
            String eventType
    ) {
        subscription.setLastStripeEventCreated(
                eventCreated
        );

        subscription.setLastStripeEventPriority(
                eventPriority
        );

        subscription.setLastStripeEventType(
                eventType
        );
    }

    private int stripeSubscriptionEventPriority(
            String eventType
    ) {
        if ("customer.subscription.updated"
                .equals(eventType)) {

            return STRIPE_EVENT_PRIORITY_SUBSCRIPTION_UPDATED;
        }

        if ("customer.subscription.created"
                .equals(eventType)) {

            return STRIPE_EVENT_PRIORITY_SUBSCRIPTION_CREATED;
        }

        throw new IllegalArgumentException(
                "Tipo de evento Stripe de suscripción no soportado: "
                        + eventType
        );
    }

    private boolean isStripeCancellationStatus(
            com.stripe.model.Subscription stripeSubscription
    ) {
        String status =
                stripeSubscription.getStatus();

        return status != null
                && (
                    "canceled".equalsIgnoreCase(status)
                    || "cancelled".equalsIgnoreCase(status)
                );
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
	    
	    @Transactional
	    public boolean hasPendingDowngrade(Long storeId) {
	
	        return subscriptionRepository
	                .findByStoreId(storeId)
	                .map(s -> s.getPendingPlan() != null)
	                .orElse(false);
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
	        
	        subscription.setMonthlyAmount(BigDecimal.ZERO);
	        subscription.setCurrency("MXN");
	        subscription.setBillingExempt(true);

	        subscription.setStripeCustomerId(null);
	        subscription.setStripeSubscriptionId(null);
	        subscription.setStripePriceId(null);

	        subscription.setPendingPlan(null);
	        subscription.setPendingPlanEffectiveAt(null);
	        subscription.setCancelAtPeriodEnd(false);
	
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
	
	        subscription.setPendingPlan(null);
	        subscription.setPendingPlanEffectiveAt(null);
	
	        subscription.setCancelAtPeriodEnd(false);
	        subscription.setBillingExempt(false);
	        
	
	
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
	    public Subscription forceCancel(Long storeId) {
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
	        
	        subscription.setStripeCustomerId(null);
	        subscription.setStripeSubscriptionId(null);
	        subscription.setStripePriceId(null);
	
	        subscription.setPendingPlan(null);
	        subscription.setPendingPlanEffectiveAt(null);
	
	        subscription.setCancelAtPeriodEnd(false);
	
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
	        
	        BigDecimal monthlyAmount =
	                resolveMonthlyAmount(stripeSubscription);
	
	        if (monthlyAmount != null) {
	            subscription.setMonthlyAmount(monthlyAmount);
	        }
	        String stripeCurrency =
	                resolveStripeCurrency(stripeSubscription);

	        if (stripeCurrency != null) {
	            subscription.setCurrency(stripeCurrency);
	        }
	        if (stripePriceId != null && !stripePriceId.isBlank()) {
	
	            StorePlan synchronizedPlan =
	                    stripePlanMapper.getPlanByPriceId(stripePriceId);
	
	            StorePlan pendingPlan =
	                    subscription.getPendingPlan();
	
	            /*
	             * CASO 1:
	             * No existe cambio pendiente.
	             * Checkout nuevo o upgrade inmediato.
	             */
	            if (pendingPlan == null) {
	
	                subscription.setStripePriceId(stripePriceId);
	                subscription.setPlan(synchronizedPlan);
	                subscription.getStore().setPlan(synchronizedPlan);
	            }
	
	            /*
	             * CASO 2:
	             * Existe downgrade pendiente, pero Stripe todavía
	             * conserva el precio del plan actual.
	             *
	             * Ejemplo:
	             * plan local       = PRO
	             * pendingPlan      = BASIC
	             * synchronizedPlan = PRO
	             *
	             * No limpiamos ni adelantamos el downgrade.
	             */
	            else if (synchronizedPlan == subscription.getPlan()) {
	
	                subscription.setStripePriceId(stripePriceId);
	            }
	
	            /*
	             * CASO 3:
	             * Stripe inició la siguiente fase y el precio activo
	             * ya corresponde al plan pendiente.
	             *
	             * Aquí materializamos definitivamente el downgrade.
	             */
	            else if (synchronizedPlan == pendingPlan) {
	
	                subscription.setStripePriceId(stripePriceId);
	                subscription.setPlan(synchronizedPlan);
	                subscription.getStore().setPlan(synchronizedPlan);
	
	                subscription.setPendingPlan(null);
	                subscription.setPendingPlanEffectiveAt(null);
	            }
	
	            /*
	             * CASO 4:
	             * Precio inesperado mientras existe un cambio pendiente.
	             * No cambiamos permisos automáticamente.
	             */
	            else {
	                throw new IllegalStateException(
	                        "El precio activo de Stripe no coincide con "
	                        + "el plan actual ni con el plan pendiente. "
	                        + "stripePriceId=" + stripePriceId
	                        + ", currentPlan=" + subscription.getPlan()
	                        + ", pendingPlan=" + pendingPlan
	                );
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
	
	
	        boolean cancelAtPeriodEnd =
	                Boolean.TRUE.equals(
	                        stripeSubscription.getCancelAtPeriodEnd()
	                );
	
	        subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
	        
	        if (stripeStatus == null || stripeStatus.isBlank()) {
	            return subscriptionRepository.save(subscription);
	        }
	        
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
	    
	

    @Transactional
    public Subscription markPastDueFromStripeWebhook(
            String stripeSubscriptionId,
            Long eventCreated,
            String eventType
    ) {
        Subscription locked =
                requireStripeSubscriptionForUpdate(
                        stripeSubscriptionId
                );

        /*
         * Un invoice atrasado nunca debe resucitar ni degradar
         * una suscripción ya cancelada por Stripe.
         */
        if (locked.getStatus()
                == SubscriptionStatus.CANCELLED) {
            return locked;
        }

        if (!shouldApplyStripeEvent(
                locked,
                eventCreated,
                STRIPE_EVENT_PRIORITY_INVOICE_FAILED,
                eventType
        )) {
            return locked;
        }

        Subscription updated =
                markPastDue(stripeSubscriptionId);

        recordStripeEventOrder(
                updated,
                eventCreated,
                STRIPE_EVENT_PRIORITY_INVOICE_FAILED,
                eventType
        );

        return subscriptionRepository.save(updated);
    }

    @Transactional
    public Subscription registerSuccessfulPaymentFromStripeWebhook(
            String stripeSubscriptionId,
            Long eventCreated,
            String eventType
    ) {
        Subscription locked =
                requireStripeSubscriptionForUpdate(
                        stripeSubscriptionId
                );

        /*
         * invoice.paid no es autoridad suficiente para
         * reactivar una suscripción que Stripe ya eliminó.
         */
        if (locked.getStatus()
                == SubscriptionStatus.CANCELLED) {
            return locked;
        }

        if (!shouldApplyStripeEvent(
                locked,
                eventCreated,
                STRIPE_EVENT_PRIORITY_INVOICE_PAID,
                eventType
        )) {
            return locked;
        }

        Subscription updated =
                registerSuccessfulPayment(
                        stripeSubscriptionId
                );

        recordStripeEventOrder(
                updated,
                eventCreated,
                STRIPE_EVENT_PRIORITY_INVOICE_PAID,
                eventType
        );

        return subscriptionRepository.save(updated);
    }

    @Transactional
    public Subscription cancelByStripeSubscriptionIdFromWebhook(
            String stripeSubscriptionId,
            Long eventCreated,
            String eventType
    ) {
        Subscription locked =
                requireStripeSubscriptionForUpdate(
                        stripeSubscriptionId
                );

        if (!shouldApplyStripeEvent(
                locked,
                eventCreated,
                STRIPE_EVENT_PRIORITY_SUBSCRIPTION_DELETED,
                eventType
        )) {
            return locked;
        }

        Subscription updated =
                cancelByStripeSubscriptionId(
                        stripeSubscriptionId
                );

        recordStripeEventOrder(
                updated,
                eventCreated,
                STRIPE_EVENT_PRIORITY_SUBSCRIPTION_DELETED,
                eventType
        );

        return subscriptionRepository.save(updated);
    }

    @Transactional
    public Subscription syncStripeSubscriptionUpdatedFromWebhook(
            com.stripe.model.Subscription stripeSubscription,
            Long eventCreated,
            String eventType
    ) {
        Subscription locked =
                findByStripeSubscriptionOrCustomerForUpdate(
                        stripeSubscription
                );

        if (locked == null) {
            /*
             * Puede ocurrir si customer.subscription.created
             * llega antes de checkout.session.completed.
             *
             * Fallar permite que el mecanismo FAILED + retry
             * vuelva a procesarlo después del provisioning.
             */
            throw new IllegalStateException(
                    "Suscripción Stripe local no encontrada: "
                            + stripeSubscription.getId()
            );
        }

        /*
         * Una subscription.deleted ya aplicada es terminal
         * para ese mismo stripeSubscriptionId.
         *
         * La reconciliación explícita sigue disponible mediante
         * syncStripeSubscriptionUpdated() sin este guard.
         */
        boolean sameStripeSubscription =
                java.util.Objects.equals(
                        locked.getStripeSubscriptionId(),
                        stripeSubscription.getId()
                );

        if (sameStripeSubscription
                && locked.getStatus()
                        == SubscriptionStatus.CANCELLED
                && !isStripeCancellationStatus(
                        stripeSubscription
                )) {
            return locked;
        }

        int eventPriority =
                stripeSubscriptionEventPriority(
                        eventType
                );

        if (!shouldApplyStripeEvent(
                locked,
                eventCreated,
                eventPriority,
                eventType
        )) {
            return locked;
        }

        Subscription updated =
                syncStripeSubscriptionUpdated(
                        stripeSubscription
                );

        if (updated == null) {
            throw new IllegalStateException(
                    "No se pudo sincronizar la suscripción Stripe: "
                            + stripeSubscription.getId()
            );
        }

        recordStripeEventOrder(
                updated,
                eventCreated,
                eventPriority,
                eventType
        );

        return subscriptionRepository.save(updated);
    }

	    @Transactional
	    public Subscription reconcileStripeSubscription(
	            String stripeSubscriptionId
	    ) {
	
	        if (stripeSubscriptionId == null
	                || stripeSubscriptionId.isBlank()) {
	            return null;
	        }
	
	        try {
	            com.stripe.model.Subscription stripeSubscription =
	                    com.stripe.model.Subscription.retrieve(
	                            stripeSubscriptionId
	                    );
	
	            return syncStripeSubscriptionUpdated(
	                    stripeSubscription
	            );
	
	        } catch (com.stripe.exception.StripeException e) {
	            throw new IllegalStateException(
	                    "No se pudo reconciliar la suscripción Stripe: "
	                            + stripeSubscriptionId,
	                    e
	            );
	        }
	    }
	    @Transactional
	    public Subscription clearPendingPlanByStripeSubscriptionId(
	            String stripeSubscriptionId
	    ) {
	
	        Subscription subscription =
	                subscriptionRepository
	                        .findByStripeSubscriptionId(
	                                stripeSubscriptionId
	                        )
	                        .orElse(null);
	
	        if (subscription == null) {
	            return null;
	        }
	
	        subscription.setPendingPlan(null);
	        subscription.setPendingPlanEffectiveAt(null);
	        subscription.setCancelAtPeriodEnd(false);
	        return subscriptionRepository.save(subscription);
	    }
	
	    private BigDecimal resolveMonthlyAmount(
	            com.stripe.model.Subscription stripeSubscription
	    ) {
	        if (stripeSubscription.getItems() == null
	                || stripeSubscription.getItems().getData() == null
	                || stripeSubscription.getItems().getData().isEmpty()) {
	            return null;
	        }
	
	        var item = stripeSubscription
	                .getItems()
	                .getData()
	                .get(0);
	
	        var price = item.getPrice();
	
	        if (price == null) {
	            return null;
	        }
	
	        Long unitAmount = price.getUnitAmount();
	
	        if (unitAmount == null) {
	            return null;
	        }
	
	        long quantity =
	                item.getQuantity() != null
	                        ? item.getQuantity()
	                        : 1L;
	
	        BigDecimal billingAmount = BigDecimal
	                .valueOf(unitAmount)
	                .multiply(BigDecimal.valueOf(quantity))
	                .movePointLeft(2);
	
	        var recurring = price.getRecurring();
	
	        if (recurring == null
	                || recurring.getInterval() == null) {
	            return billingAmount.setScale(
	                    2,
	                    java.math.RoundingMode.HALF_UP
	            );
	        }
	
	        long intervalCount =
	                recurring.getIntervalCount() != null
	                        ? recurring.getIntervalCount()
	                        : 1L;
	
	        if (intervalCount <= 0) {
	            intervalCount = 1L;
	        }
	
	        BigDecimal monthlyAmount =
	                switch (recurring.getInterval()) {
	
	                    case "month" -> billingAmount.divide(
	                            BigDecimal.valueOf(intervalCount),
	                            2,
	                            java.math.RoundingMode.HALF_UP
	                    );
	
	                    case "year" -> billingAmount.divide(
	                            BigDecimal.valueOf(12L * intervalCount),
	                            2,
	                            java.math.RoundingMode.HALF_UP
	                    );
	
	                    /*
	                     * Aproximación mensual para precios semanales o diarios.
	                     * Actualmente tus planes son mensuales, pero dejamos
	                     * el sincronizador preparado.
	                     */
	                    case "week" -> billingAmount
	                            .multiply(new BigDecimal("52"))
	                            .divide(
	                                    BigDecimal.valueOf(
	                                            12L * intervalCount
	                                    ),
	                                    2,
	                                    java.math.RoundingMode.HALF_UP
	                            );
	
	                    case "day" -> billingAmount
	                            .multiply(new BigDecimal("365"))
	                            .divide(
	                                    BigDecimal.valueOf(
	                                            12L * intervalCount
	                                    ),
	                                    2,
	                                    java.math.RoundingMode.HALF_UP
	                            );
	
	                    default -> billingAmount;
	                };
	
	        return monthlyAmount.setScale(
	                2,
	                java.math.RoundingMode.HALF_UP
	        );
	    }
	    private String resolveStripeCurrency(
	            com.stripe.model.Subscription stripeSubscription
	    ) {
	        if (stripeSubscription == null
	                || stripeSubscription.getItems() == null
	                || stripeSubscription.getItems().getData() == null
	                || stripeSubscription.getItems().getData().isEmpty()) {
	            return null;
	        }

	        var item = stripeSubscription
	                .getItems()
	                .getData()
	                .get(0);

	        var price = item.getPrice();

	        if (price == null
	                || price.getCurrency() == null
	                || price.getCurrency().isBlank()) {
	            return null;
	        }

	        return price.getCurrency()
	                .trim()
	                .toUpperCase();
	    }
	}