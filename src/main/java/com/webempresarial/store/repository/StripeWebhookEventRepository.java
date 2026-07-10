package com.webempresarial.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.StripeWebhookEvent;

public interface StripeWebhookEventRepository
        extends JpaRepository<StripeWebhookEvent, Long> {

    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);

    boolean existsByStripeEventIdAndStatus(
            String stripeEventId,
            com.webempresarial.store.model.StripeWebhookEventStatus status
    );
}