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
    public ResponseEntity<StripeConnectOnboardingResponseDTO> createOnboarding(
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        String onboardingUrl = stripeConnectService.createOnboardingLink(store);

        return ResponseEntity.ok(
                new StripeConnectOnboardingResponseDTO(onboardingUrl)
        );
    }
}