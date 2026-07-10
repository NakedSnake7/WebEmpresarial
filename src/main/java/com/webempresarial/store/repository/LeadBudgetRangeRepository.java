package com.webempresarial.store.repository;

import com.webempresarial.store.entity.LeadBudgetRange;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeadBudgetRangeRepository
        extends JpaRepository<LeadBudgetRange, Long> {

    List<LeadBudgetRange> findByStoreIdAndActiveTrueOrderBySortOrderAsc(
            Long storeId
    );

    Optional<LeadBudgetRange> findFirstByStoreIdAndCodeAndActiveTrue(
            Long storeId,
            String code
    );
}