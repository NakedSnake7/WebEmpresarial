package com.webempresarial.store.repository;

import com.webempresarial.store.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreSettingsRepository
        extends JpaRepository<StoreSettings, Long> {

    Optional<StoreSettings> findByStoreId(Long storeId);
}