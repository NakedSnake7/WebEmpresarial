package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;


@Service
public class StoreLookupService {

    private final StoreRepository storeRepository;

    public StoreLookupService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /*
     * No cachear la entidad Store completa.
     *
     * Store contiene estado mutable relevante para autorización,
     * incluyendo plan y Subscription.
     *
     * Los webhooks de billing pueden cambiar ese estado fuera del
     * ciclo de una petición HTTP. Cachear la entidad podría dejar
     * decisiones de acceso operando sobre información obsoleta.
     *
     * Si más adelante se requiere optimización, deberá cachearse
     * una representación inmutable o implementarse invalidación
     * transaccional explícita.
     */
    public Store getStoreByDomain(String domain) {
        return storeRepository.findByDominioAndActivaTrue(domain)
                .orElseThrow(() -> new RuntimeException(
                        "Tienda no encontrada para dominio: " + domain
                ));
    }
}