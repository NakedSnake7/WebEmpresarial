package com.webempresarial.store.service;

import org.springframework.cache.annotation.Cacheable; 
import org.springframework.stereotype.Service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;


@Service
public class StoreLookupService {

    private final StoreRepository storeRepository;

    public StoreLookupService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Cacheable(value = "storesByDomain", key = "#domain")
    public Store getStoreByDomain(String domain) {
        return storeRepository.findByDominioAndActivaTrue(domain)
                .orElseThrow(() -> new RuntimeException(
                        "Tienda no encontrada para dominio: " + domain
                ));
    }
}