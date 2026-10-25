package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.exceptions.StoreNotFoundException;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@Service
public class StoreLookupService {

    private final StoreRepository storeRepository;
    private final PlatformAccessService platformAccessService;
    private final FeatureAccessService featureAccessService;

    public StoreLookupService(
            StoreRepository storeRepository,
            PlatformAccessService platformAccessService,
            FeatureAccessService featureAccessService
    ) {
        this.storeRepository = storeRepository;
        this.platformAccessService = platformAccessService;
        this.featureAccessService = featureAccessService;
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

        var platformStore =
                storeRepository
                        .findByDominioAndActivaTrue(domain);

        /*
         * El dominio estable de plataforma tiene precedencia
         * absoluta sobre cualquier alias personalizado.
         */
        if (platformStore.isPresent()) {
            return platformStore.get();
        }

        Store customDomainStore =
                storeRepository
                        .findByCustomDomainAndCustomDomainVerifiedTrueAndActivaTrue(
                                domain
                        )
                        .orElseThrow(() ->
                                new StoreNotFoundException(domain)
                        );

        /*
         * Un custom domain sólo está activo mientras el tenant
         * conserve acceso efectivo a la plataforma y entitlement
         * para Feature.CUSTOM_DOMAIN.
         *
         * No dependemos de Store.plan como fuente de autorización.
         */
        if (!platformAccessService.hasPlatformAccess(
                customDomainStore
        )) {
            throw new StoreNotFoundException(domain);
        }

        if (!featureAccessService.canUse(
                customDomainStore,
                Feature.CUSTOM_DOMAIN
        )) {
            throw new StoreNotFoundException(domain);
        }

        return customDomainStore;
    }
}
