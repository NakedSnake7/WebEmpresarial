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

        String rawHost = request.getHeader("X-Forwarded-Host");

        if (rawHost == null || rawHost.isBlank()) {
            rawHost = request.getServerName();
        }

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