package com.webempresarial.store.controller.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webempresarial.store.dto.billing.StripeConnectOnboardingResponseDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeConnectService;

class StripeConnectControllerTest {

    private StripeConnectService stripeConnectService;
    private StoreContextService storeContextService;
    private HttpServletRequest request;
    private Store store;

    private StripeConnectController controller;

    @BeforeEach
    void setUp() {

        stripeConnectService =
                mock(StripeConnectService.class);

        storeContextService =
                mock(StoreContextService.class);

        request =
                mock(HttpServletRequest.class);

        store =
                mock(Store.class);

        controller =
                new StripeConnectController(
                        stripeConnectService,
                        storeContextService
                );

        when(storeContextService
                .getCurrentStore(request))
                .thenReturn(store);
    }

    @Test
    void createOnboarding_shouldUseCurrentTenantAndRequestOrigin() {

        when(request.getHeader("X-Forwarded-Proto"))
                .thenReturn(null);

        when(request.getHeader("X-Forwarded-Host"))
                .thenReturn(null);

        when(request.getScheme())
                .thenReturn("https");

        when(request.getHeader("Host"))
                .thenReturn("stride.web-empresarial.com");

        when(stripeConnectService
                .createOnboardingLink(
                        store,
                        "https://stride.web-empresarial.com"
                ))
                .thenReturn(
                        "https://connect.stripe.com/onboarding"
                );

        var response =
                controller.createOnboarding(request);

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        StripeConnectOnboardingResponseDTO body =
                response.getBody();

        assertThat(body)
                .isNotNull();

        verify(storeContextService)
                .getCurrentStore(request);

        verify(stripeConnectService)
                .createOnboardingLink(
                        store,
                        "https://stride.web-empresarial.com"
                );
    }

    @Test
    void createOnboarding_shouldHonorForwardedProtoAndHost() {

        when(request.getHeader("X-Forwarded-Proto"))
                .thenReturn("https");

        when(request.getHeader("X-Forwarded-Host"))
                .thenReturn(
                        "stride.web-empresarial.com"
                );

        when(stripeConnectService
                .createOnboardingLink(
                        store,
                        "https://stride.web-empresarial.com"
                ))
                .thenReturn(
                        "https://connect.stripe.com/onboarding"
                );

        controller.createOnboarding(request);

        verify(stripeConnectService)
                .createOnboardingLink(
                        store,
                        "https://stride.web-empresarial.com"
                );
    }

    @Test
    void createOnboarding_shouldUseFirstForwardedValue() {

        when(request.getHeader("X-Forwarded-Proto"))
                .thenReturn("https, http");

        when(request.getHeader("X-Forwarded-Host"))
                .thenReturn(
                        "stride.web-empresarial.com, proxy.internal"
                );

        when(stripeConnectService
                .createOnboardingLink(
                        store,
                        "https://stride.web-empresarial.com"
                ))
                .thenReturn(
                        "https://connect.stripe.com/onboarding"
                );

        controller.createOnboarding(request);

        verify(stripeConnectService)
                .createOnboardingLink(
                        store,
                        "https://stride.web-empresarial.com"
                );
    }

    @Test
    void createOnboarding_shouldIncludeNonStandardLocalPort() {

        when(request.getHeader("X-Forwarded-Proto"))
                .thenReturn(null);

        when(request.getHeader("X-Forwarded-Host"))
                .thenReturn(null);

        when(request.getScheme())
                .thenReturn("http");

        when(request.getHeader("Host"))
                .thenReturn(null);

        when(request.getServerName())
                .thenReturn("stride.local");

        when(request.getServerPort())
                .thenReturn(8080);

        when(stripeConnectService
                .createOnboardingLink(
                        store,
                        "http://stride.local:8080"
                ))
                .thenReturn(
                        "https://connect.stripe.com/onboarding"
                );

        controller.createOnboarding(request);

        verify(stripeConnectService)
                .createOnboardingLink(
                        store,
                        "http://stride.local:8080"
                );
    }
}