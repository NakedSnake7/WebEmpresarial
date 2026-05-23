package com.webempresarial.store.repository;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByStoreOrderByCreatedAtDesc(Store store);

    Optional<Lead> findByIdAndStore(Long id, Store store);
}