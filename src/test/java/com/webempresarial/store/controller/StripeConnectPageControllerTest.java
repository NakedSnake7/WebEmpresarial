package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeConnectService;

class StripeConnectPageControllerTest {

    private StripeConnectService stripeConnectService;
    private StoreContextService storeContextService;
    private HttpServletRequest request;
    private Store store;

    private StripeConnectPageController controller;

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
                new StripeConnectPageController(
                        stripeConnectService,
                        storeContextService
                );

        when(storeContextService
                .getCurrentStore(request))
                .thenReturn(store);
    }

    @Test
    void connectReturn_shouldSyncCurrentStoreAndRedirectConnected() {

        when(stripeConnectService
                .syncConnectionStatus(store))
                .thenReturn(true);

        var result =
                controller.connectReturn(request);

        assertThat(result.getUrl())
                .isEqualTo(
                        "/admin/store/settings"
                                + "?stripeConnected"
                );

        verify(storeContextService)
                .getCurrentStore(request);

        verify(stripeConnectService)
                .syncConnectionStatus(store);
    }

    @Test
    void connectReturn_shouldRedirectPendingWhenStripeIsNotReady() {

        when(stripeConnectService
                .syncConnectionStatus(store))
                .thenReturn(false);

        var result =
                controller.connectReturn(request);

        assertThat(result.getUrl())
                .isEqualTo(
                        "/admin/store/settings"
                                + "?stripePending"
                );

        verify(storeContextService)
                .getCurrentStore(request);

        verify(stripeConnectService)
                .syncConnectionStatus(store);
    }

    @Test
    void connectRefresh_shouldRedirectToSettingsWithoutSyncingStripe() {

        var result =
                controller.connectRefresh(request);

        assertThat(result.getUrl())
                .isEqualTo(
                        "/admin/store/settings"
                                + "?stripeRefresh"
                );

        verifyNoInteractions(
                storeContextService,
                stripeConnectService
        );
    }
}