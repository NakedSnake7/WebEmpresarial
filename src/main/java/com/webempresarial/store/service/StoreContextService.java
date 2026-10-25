package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class StoreContextService {

    private final StoreLookupService storeLookupService;

    public StoreContextService(StoreLookupService storeLookupService) {
        this.storeLookupService = storeLookupService;
    }

    public Store getCurrentStore(HttpServletRequest request) {

        /*
         * La identidad del tenant proviene del Host solicitado.
         *
         * No usar X-Forwarded-Host aquí: ese header puede llegar
         * controlado por el cliente a través del proxy y no debe
         * seleccionar otro tenant.
         *
         * Tampoco usamos getServerName() como fallback porque con
         * ForwardedHeaderFilter puede haber sido derivado del mismo
         * X-Forwarded-Host.
         */
        String rawHost = request.getHeader("Host");

        if (rawHost == null || rawHost.isBlank()) {
            throw new RuntimeException("No se pudo detectar el dominio actual");
        }

        String host = normalizeHost(rawHost);

        return storeLookupService.getStoreByDomain(host);
    }

    private String normalizeHost(String host) {
        return host
                .trim()
                .toLowerCase()
                .replace("https://", "")
                .replace("http://", "")
                .split(":")[0]
                .replace("/", "");
    }
}