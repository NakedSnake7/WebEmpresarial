package com.webempresarial.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.PilotOffer;

public interface PilotOfferRepository
        extends JpaRepository<PilotOffer, Long> {

    @EntityGraph(attributePaths = {"store", "lead"})
    Optional<PilotOffer> findByIdAndStore_Id(
            Long id,
            Long storeId
    );

    @EntityGraph(attributePaths = {"store", "lead"})
    List<PilotOffer> findByStore_IdOrderByCreatedAtDesc(
            Long storeId
    );

    Optional<PilotOffer> findByStripeCheckoutSessionId(
            String sessionId
    );

    Optional<PilotOffer> findByStripePaymentIntentId(
            String paymentIntentId
    );
}
