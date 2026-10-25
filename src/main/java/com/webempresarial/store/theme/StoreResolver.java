package com.webempresarial.store.theme;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreLookupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class StoreResolver {

    private final StoreLookupService storeLookupService;

    public StoreResolver(StoreLookupService storeLookupService) {
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
            throw new RuntimeException("No se pudo resolver el dominio actual");
        }

        String host = normalizeHost(rawHost);

        return storeLookupService.getStoreByDomain(host);
    }

    public String getCurrentTheme(HttpServletRequest request) {
        Store store = getCurrentStore(request);

        if (store.getTheme() != null && !store.getTheme().isBlank()) {
            return store.getTheme();
        }

        return "WebEmpresarial";
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