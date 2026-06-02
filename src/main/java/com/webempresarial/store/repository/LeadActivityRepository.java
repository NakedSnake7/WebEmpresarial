package com.webempresarial.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.LeadActivity;

public interface LeadActivityRepository extends JpaRepository<LeadActivity, Long> {

    List<LeadActivity> findByLeadIdOrderByCreatedAtDesc(Long leadId);
    List<LeadActivity> findTop8ByLeadStoreIdOrderByCreatedAtDesc(Long storeId);
}
