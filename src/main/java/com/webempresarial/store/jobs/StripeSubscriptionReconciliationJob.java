package com.webempresarial.store.jobs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.repository.SubscriptionRepository;
import com.webempresarial.store.service.SubscriptionService;

@Component
public class StripeSubscriptionReconciliationJob {

    private static final Logger log =
            LoggerFactory.getLogger(
                    StripeSubscriptionReconciliationJob.class
            );

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    public StripeSubscriptionReconciliationJob(
            SubscriptionRepository subscriptionRepository,
            SubscriptionService subscriptionService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(
            cron = "${stripe.reconciliation.cron:0 0 3 * * *}"
    )
    public void reconcile() {

        List<Subscription> subscriptions =
                subscriptionRepository.findByStripeSubscriptionIdIsNotNull();

        log.info(
                "[Stripe Sync] {} suscripciones encontradas",
                subscriptions.size()
        );

        int synchronizedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (Subscription subscription : subscriptions) {

            if (!subscription.isActive()
                    && !subscription.isPastDue()
                    && !subscription.isTrial()) {
                skippedCount++;
                continue;
            }

            if (!subscription.isBillable()) {
                skippedCount++;
                continue;
            }

            try {
                subscriptionService.reconcileStripeSubscription(
                        subscription.getStripeSubscriptionId()
                );

                synchronizedCount++;

                log.debug(
                        "[Stripe Sync] Suscripción sincronizada: {}",
                        subscription.getStripeSubscriptionId()
                );

            } catch (IllegalStateException ex) {
                failedCount++;

                log.warn(
                        "[Stripe Sync] No fue posible sincronizar {}: {}",
                        subscription.getStripeSubscriptionId(),
                        ex.getMessage()
                );
            }
        }

        log.info(
                "[Stripe Sync] Finalizada. Sincronizadas: {}, omitidas: {}, fallidas: {}",
                synchronizedCount,
                skippedCount,
                failedCount
        );
    }
}