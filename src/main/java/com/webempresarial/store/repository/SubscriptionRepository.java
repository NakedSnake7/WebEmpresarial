package com.webempresarial.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.Subscription;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByStoreId(Long storeId);

    Optional<Subscription> findByStripeCustomerId(
            String stripeCustomerId
    );

    Optional<Subscription> findByStripeSubscriptionId(
            String stripeSubscriptionId
    );
}