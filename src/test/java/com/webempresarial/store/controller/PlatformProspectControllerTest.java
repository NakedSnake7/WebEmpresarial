package com.webempresarial.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.webempresarial.store.dto.PlatformProspectRequest;
import com.webempresarial.store.entity.PlatformProspect;
import com.webempresarial.store.repository.PlatformProspectRepository;
import com.webempresarial.store.service.StoreLookupService;
import com.webempresarial.store.theme.StoreResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class PlatformProspectControllerTest {

    private final StoreLookupService lookup = mock(StoreLookupService.class);
    private final PlatformProspectRepository repository =
            mock(PlatformProspectRepository.class);
    private final PlatformProspectController controller =
            new PlatformProspectController(new StoreResolver(lookup), repository);

    private MockHttpServletRequest request(String host) {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/platform/prospects");
        request.addHeader("Host", host);
        request.setRemoteAddr("192.0.2.10");
        return request;
    }

    private PlatformProspectRequest dto(String form, String website) {
        return new PlatformProspectRequest(
                "Negocio prueba", "522220000000", "Comercio",
                null, "piloto", "por-definir", "Solicito diagnóstico",
                "transferencia", "A", form, website);
    }

    @Test
    void platformMainFormCreatesIndependentProspect() {
        assertEquals(HttpStatus.CREATED,
                controller.capture(dto("main_form", null),
                        request("webempresarial.local:18080")).getStatusCode());
        ArgumentCaptor<PlatformProspect> captor =
                ArgumentCaptor.forClass(PlatformProspect.class);
        verify(repository).save(captor.capture());
        PlatformProspect saved = captor.getValue();
        assertEquals("A", saved.getCohortHint());
        assertEquals("transferencia", saved.getPaymentPreference());
        assertEquals("main_form", saved.getSource());
        assertEquals("NEW", saved.getStatus());
        verifyNoInteractions(lookup);
    }

    @Test
    void tenantHostRejectedWithoutSaving() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.capture(dto("main_form", null),
                        request("stride.local:18080")));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void invalidFormRejectedWithoutSaving() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.capture(dto("unsafe_form", null),
                        request("webempresarial.local:18080")));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void honeypotIsAcceptedButNotStored() {
        assertEquals(HttpStatus.ACCEPTED,
                controller.capture(dto("hero_micro", "spam.example"),
                        request("webempresarial.local:18080")).getStatusCode());
        verifyNoInteractions(repository);
    }
}
