package com.webempresarial.store.controller.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.dto.billing.StripeConnectOnboardingResponseDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeConnectService;

@RestController
@RequestMapping("/api/admin/stripe/connect")
public class StripeConnectController {

    private final StripeConnectService stripeConnectService;
    private final StoreContextService storeContextService;

    public StripeConnectController(
            StripeConnectService stripeConnectService,
            StoreContextService storeContextService
    ) {
        this.stripeConnectService = stripeConnectService;
        this.storeContextService = storeContextService;
    }

    @PostMapping("/onboarding")
    public ResponseEntity<StripeConnectOnboardingResponseDTO>
            createOnboarding(
                    HttpServletRequest request
            ) {

        Store store =
                storeContextService.getCurrentStore(request);

        String baseUrl =
                resolveBaseUrl(request);

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
            HttpServletRequest request
    ) {

        String forwardedProto =
                request.getHeader("X-Forwarded-Proto");

        String forwardedHost =
                request.getHeader("X-Forwarded-Host");

        String scheme =
                forwardedProto != null
                        && !forwardedProto.isBlank()
                        ? forwardedProto.split(",")[0].trim()
                        : request.getScheme();

        String host =
                forwardedHost != null
                        && !forwardedHost.isBlank()
                        ? forwardedHost.split(",")[0].trim()
                        : request.getHeader("Host");

        if (host == null || host.isBlank()) {

            host = request.getServerName();

            int port = request.getServerPort();

            boolean standardPort =
                    ("http".equalsIgnoreCase(scheme)
                            && port == 80)
                    ||
                    ("https".equalsIgnoreCase(scheme)
                            && port == 443);

            if (!standardPort) {
                host += ":" + port;
            }
        }

        return scheme + "://" + host;
    }
}