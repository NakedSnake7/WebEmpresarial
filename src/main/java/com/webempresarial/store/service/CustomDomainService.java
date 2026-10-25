package com.webempresarial.store.service;

import java.net.IDN;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.exceptions.CustomDomainValidationException;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@Service
public class CustomDomainService {

    private static final String PLATFORM_DOMAIN =
            "web-empresarial.com";

    private final StoreRepository storeRepository;
    private final PlatformAccessService platformAccessService;
    private final FeatureAccessService featureAccessService;

    public CustomDomainService(
            StoreRepository storeRepository,
            PlatformAccessService platformAccessService,
            FeatureAccessService featureAccessService
    ) {
        this.storeRepository = storeRepository;
        this.platformAccessService = platformAccessService;
        this.featureAccessService = featureAccessService;
    }

    @Transactional
    public void requestDomain(
            Store store,
            String rawDomain
    ) {
        requireAccess(store);

        String domain =
                normalizeAndValidate(rawDomain);

        /*
         * Solicitar nuevamente exactamente el mismo dominio
         * es idempotente y no invalida una verificación previa.
         */
        if (domain.equals(
                normalizeExisting(
                        store.getCustomDomain()
                )
        )) {
            return;
        }

        ensureAvailable(domain);

        store.setCustomDomain(domain);

        /*
         * Todo cambio de hostname requiere nueva verificación
         * DNS/TLS antes de participar en tenant resolution.
         */
        store.setCustomDomainVerified(false);

        storeRepository.save(store);
    }

    @Transactional
    public void markVerified(Store store) {
        requireAccess(store);

        if (store.getCustomDomain() == null
                || store.getCustomDomain().isBlank()) {

            throw new CustomDomainValidationException(
                    "La tienda no tiene un dominio personalizado configurado"
            );
        }

        store.setCustomDomainVerified(true);

        storeRepository.save(store);
    }

    @Transactional
    public void disconnectDomain(Store store) {
        requireAccess(store);

        store.setCustomDomain(null);
        store.setCustomDomainVerified(false);

        storeRepository.save(store);
    }

    private void requireAccess(Store store) {

        if (store == null
                || !platformAccessService
                        .hasPlatformAccess(store)) {

            throw new FeatureLockedException(
                    Feature.CUSTOM_DOMAIN
            );
        }

        featureAccessService.requireFeature(
                store,
                Feature.CUSTOM_DOMAIN
        );
    }

    private void ensureAvailable(String domain) {

        if (storeRepository.existsByDominio(domain)
                || storeRepository
                        .existsByCustomDomain(domain)) {

            throw new CustomDomainValidationException(
                    "El dominio ya está en uso"
            );
        }
    }

    private String normalizeAndValidate(
            String rawDomain
    ) {
        if (rawDomain == null
                || rawDomain.isBlank()) {

            throw invalidDomain();
        }

        String domain = rawDomain.trim();

        /*
         * Sólo aceptamos un hostname.
         * No URL, puerto, path, query, fragment,
         * credenciales ni wildcard.
         */
        if (domain.contains("://")
                || domain.contains("/")
                || domain.contains("\\")
                || domain.contains(":")
                || domain.contains("?")
                || domain.contains("#")
                || domain.contains("@")
                || domain.contains("*")) {

            throw invalidDomain();
        }

        /*
         * Un único punto final representa la raíz DNS y puede
         * normalizarse. Más de uno indica un hostname malformado.
         */
        if (domain.endsWith("..")) {
            throw invalidDomain();
        }

        if (domain.endsWith(".")) {
            domain = domain.substring(
                    0,
                    domain.length() - 1
            );
        }

        if (domain.isBlank()) {
            throw invalidDomain();
        }

        final String ascii;

        try {
            ascii = IDN.toASCII(
                    domain,
                    IDN.USE_STD3_ASCII_RULES
            ).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            throw invalidDomain();
        }

        if (ascii.length() > 253) {
            throw invalidDomain();
        }

        /*
         * El MVP requiere un FQDN.
         * Esto excluye localhost y nombres single-label.
         */
        if (!ascii.contains(".")) {
            throw invalidDomain();
        }

        /*
         * No permitimos direcciones IPv4.
         * IPv6 ya queda excluido por ':'.
         */
        if (looksLikeIpv4(ascii)) {
            throw invalidDomain();
        }

        if (ascii.equals(PLATFORM_DOMAIN)
                || ascii.endsWith(
                        "." + PLATFORM_DOMAIN
                )) {

            throw new CustomDomainValidationException(
                    "El dominio pertenece al namespace reservado de WebEmpresarial"
            );
        }

        validateLabels(ascii);

        return ascii;
    }

    private void validateLabels(String domain) {

        String[] labels = domain.split("\\.");

        if (labels.length < 2) {
            throw invalidDomain();
        }

        for (String label : labels) {

            if (label.isBlank()
                    || label.length() > 63
                    || label.startsWith("-")
                    || label.endsWith("-")) {

                throw invalidDomain();
            }

            for (int i = 0; i < label.length(); i++) {

                char c = label.charAt(i);

                boolean valid =
                        (c >= 'a' && c <= 'z')
                        || (c >= '0' && c <= '9')
                        || c == '-';

                if (!valid) {
                    throw invalidDomain();
                }
            }
        }
    }

    private boolean looksLikeIpv4(String domain) {

        if (!domain.matches(
                "[0-9]+(?:\\.[0-9]+){3}"
        )) {
            return false;
        }

        String[] parts = domain.split("\\.");

        for (String part : parts) {
            try {
                int value =
                        Integer.parseInt(part);

                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        return true;
    }

    private String normalizeExisting(
            String domain
    ) {
        if (domain == null
                || domain.isBlank()) {
            return null;
        }

        String value =
                domain.trim()
                        .toLowerCase(Locale.ROOT);

        while (value.endsWith(".")) {
            value = value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }

    private CustomDomainValidationException
            invalidDomain() {

        return new CustomDomainValidationException(
                "Dominio personalizado inválido"
        );
    }
}
