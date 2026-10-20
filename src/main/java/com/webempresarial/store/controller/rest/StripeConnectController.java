package com.webempresarial.store.controller.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.dto.billing.StripeConnectOnboardingResponseDTO;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeConnectService;

@RestController
@RequestMapping("/api/admin/stripe/connect")
public class StripeConnectController {

    private final StripeConnectService stripeConnectService;
    private final StoreContextService storeContextService;
    private final FeatureAccessService featureAccessService;

    public StripeConnectController(
            StripeConnectService stripeConnectService,
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService
    ) {
        this.stripeConnectService = stripeConnectService;
        this.storeContextService = storeContextService;
        this.featureAccessService = featureAccessService;
    }

    @PostMapping("/onboarding")
    public ResponseEntity<StripeConnectOnboardingResponseDTO>
            createOnboarding(
                    HttpServletRequest request
            ) {

        Store store =
                storeContextService.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.STRIPE_CONNECT
        );

        String baseUrl =
                resolveBaseUrl(
                        request,
                        store
                );

        String onboardingUrl =
                stripeConnectService.createOnboardingLink(
                        store,
                        baseUrl
                );

        return ResponseEntity.ok(
                new StripeConnectOnboardingResponseDTO(
                        onboardingUrl
                )
        );
    }

    private String resolveBaseUrl(
            HttpServletRequest request,
            Store store
    ) {

        String domain = store.getDominio();

        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException(
                    "La tienda no tiene un dominio configurado"
            );
        }

        String normalizedDomain =
                domain.trim().toLowerCase();

        boolean local =
                normalizedDomain.endsWith(".local")
                || normalizedDomain.equals("localhost")
                || normalizedDomain.equals("127.0.0.1");

        if (!local) {
            return "https://" + normalizedDomain;
        }

        String scheme =
                request.getScheme() == null
                        || request.getScheme().isBlank()
                        ? "http"
                        : request.getScheme();

        int port = request.getServerPort();

        boolean standardPort =
                ("http".equalsIgnoreCase(scheme) && port == 80)
                ||
                ("https".equalsIgnoreCase(scheme) && port == 443);

        return scheme
                + "://"
                + normalizedDomain
                + (standardPort ? "" : ":" + port);
    }
}
