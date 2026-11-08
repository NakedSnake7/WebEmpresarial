package com.webempresarial.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.webempresarial.store.service.StoreLookupService;
import com.webempresarial.store.theme.StoreResolver;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

class PilotLandingControllerTest {

    private final StoreLookupService lookup =
            mock(StoreLookupService.class);

    private final PilotLandingController controller =
            new PilotLandingController(new StoreResolver(lookup));

    private MockHttpServletRequest request(String host) {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/piloto");

        request.addHeader("Host", host);
        return request;
    }

    @Test
    void rendersForPlatformDomainsWithoutTenantLookup() {
        for (String host : new String[] {
                "webempresarial.local:18080",
                "web-empresarial.com",
                "www.web-empresarial.com"
        }) {
            ExtendedModelMap model = new ExtendedModelMap();

            String view = controller.showPilot(
                    model,
                    request(host)
            );

            assertEquals(
                    "themes/WebEmpresarial/index",
                    view
            );

            assertEquals(
                    "WebEmpresarial",
                    model.get("theme")
            );

            assertNull(model.get("store"));
            assertEquals(false, model.get("showCart"));
            assertEquals(false, model.get("showCheckout"));
            assertEquals(false, model.get("showAuth"));
            assertEquals(true, model.get("showScripts"));
        }

        verifyNoInteractions(lookup);
    }

    @Test
    void neverServesPlatformLandingOnTenantHosts() {
        for (String host : new String[] {
                "stride.local:18080",
                "barleypunch.local:18080",
                "localhost:18080"
        }) {
            ResponseStatusException error = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.showPilot(
                            new ExtendedModelMap(),
                            request(host)
                    )
            );

            assertEquals(
                    HttpStatus.NOT_FOUND,
                    error.getStatusCode()
            );
        }

        verifyNoInteractions(lookup);
    }

    @Test
    void doesNotRequireAStoreToRenderPlatformLanding() {
        ExtendedModelMap model = new ExtendedModelMap();

        controller.showPilot(
                model,
                request("webempresarial.local")
        );

        assertNull(model.get("store"));

        assertEquals(
                "themes/WebEmpresarial/index",
                controller.showPilot(
                        new ExtendedModelMap(),
                        request("webempresarial.local")
                )
        );

        verifyNoInteractions(lookup);
    }
}
