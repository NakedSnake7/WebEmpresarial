package com.webempresarial.store.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webempresarial.store.entity.Proposal;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByLeadIdAndLeadStoreIdOrderByCreatedAtDesc(
            Long leadId,
            Long storeId
    );

    List<Proposal> findByLeadStoreIdOrderByCreatedAtDesc(Long storeId);

    Optional<Proposal> findByIdAndLeadStoreId(Long id, Long storeId);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Proposal p
        WHERE p.lead.store.id = :storeId
        AND p.status IN (
            com.webempresarial.store.model.ProposalStatus.SENT,
            com.webempresarial.store.model.ProposalStatus.VIEWED,
            com.webempresarial.store.model.ProposalStatus.ACCEPTED
        )
    """)
    BigDecimal getTotalProposalValue(Long storeId);

    @Query("""
        SELECT COALESCE(SUM(p.amount * p.closeProbability / 100), 0)
        FROM Proposal p
        WHERE p.lead.store.id = :storeId
        AND p.status IN (
            com.webempresarial.store.model.ProposalStatus.SENT,
            com.webempresarial.store.model.ProposalStatus.VIEWED
        )
    """)
    BigDecimal getRevenueForecast(Long storeId);

    @Query("""
        SELECT COUNT(p)
        FROM Proposal p
    """)
    long countAllPlatformProposals();

    @Query("""
        SELECT COALESCE(SUM(p.amount * p.closeProbability / 100), 0)
        FROM Proposal p
        WHERE p.status IN (
            com.webempresarial.store.model.ProposalStatus.SENT,
            com.webempresarial.store.model.ProposalStatus.VIEWED
        )
    """)
    BigDecimal getGlobalRevenueForecast();
}