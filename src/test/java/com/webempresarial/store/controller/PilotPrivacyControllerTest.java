package com.webempresarial.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.webempresarial.store.service.StoreLookupService;
import com.webempresarial.store.theme.StoreResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

class PilotPrivacyControllerTest {

    private final StoreResolver resolver =
            new StoreResolver(mock(StoreLookupService.class));

    private MockHttpServletRequest request(String host) {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/piloto/privacidad");
        request.addHeader("Host", host);
        return request;
    }

    @Test
    void tenantHostDeniedEvenWhenApproved() {
        PilotPrivacyController controller = new PilotPrivacyController(
                resolver, new PilotPrivacyReadiness(true, "Empresa", "Domicilio", "privacidad@example.test"));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.privacy(new ExtendedModelMap(), request("stride.local")));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void incompleteNoticeFailsClosed() {
        PilotPrivacyController controller = new PilotPrivacyController(
                resolver, new PilotPrivacyReadiness(false, "", "", ""));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.privacy(new ExtendedModelMap(), request("webempresarial.local")));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }

    @Test
    void approvedAndConfiguredNoticeRendersOwnTemplate() {
        PilotPrivacyController controller = new PilotPrivacyController(
                resolver, new PilotPrivacyReadiness(true, "Operador", "Domicilio comercial", "privacidad@example.test"));
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("themes/WebEmpresarial/pilot-privacy",
                controller.privacy(model, request("webempresarial.local")));
        assertEquals("Operador", model.get("responsibleName"));
        assertEquals("Domicilio comercial", model.get("responsibleAddress"));
        assertEquals("privacidad@example.test", model.get("privacyEmail"));
    }

    @Test
    void missingContactKeepsNoticeUnavailable() {
        PilotPrivacyController controller = new PilotPrivacyController(
                resolver, new PilotPrivacyReadiness(true, "Operador", "Domicilio comercial", ""));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.privacy(new ExtendedModelMap(), request("webempresarial.local")));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
