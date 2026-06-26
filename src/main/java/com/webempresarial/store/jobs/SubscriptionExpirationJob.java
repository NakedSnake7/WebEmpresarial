package com.webempresarial.store.jobs;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubscriptionExpirationJob {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionExpirationJob(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireTrials() {

        LocalDateTime now = LocalDateTime.now();

        List<Subscription> expiredTrials =
                subscriptionRepository.findByStatusAndCurrentPeriodEndBefore(
                        SubscriptionStatus.TRIAL,
                        now
                );

        for (Subscription subscription : expiredTrials) {

            if (subscription.isBillingExempt()) {
                continue;
            }

            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setEndsAt(now);
            subscription.setNextBillingDate(null);

            subscription.getStore().setActiva(false);
        }
    }
}