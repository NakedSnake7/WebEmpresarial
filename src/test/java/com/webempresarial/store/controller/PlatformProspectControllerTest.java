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
    private final PilotPrivacyReadiness approvedPrivacy =
            new PilotPrivacyReadiness(true, "Operador autorizado", "Domicilio valido", "legal@example.test");
    private final PlatformProspectController controller =
            new PlatformProspectController(new StoreResolver(lookup), repository,
                    approvedPrivacy);

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
    void samePhoneBlockedEvenWhenRemoteIpChanges() {
        for (int i = 1; i <= 3; i++) {
            MockHttpServletRequest req = request("webempresarial.local:18080");
            req.setRemoteAddr("198.51.100." + i);
            assertEquals(HttpStatus.CREATED,
                    controller.capture(dto("main_form", null), req)
                            .getStatusCode());
        }
        MockHttpServletRequest fourth = request("webempresarial.local:18080");
        fourth.setRemoteAddr("198.51.100.4");
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.capture(dto("main_form", null), fourth));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.getStatusCode());
        verify(repository, times(3)).save(any(PlatformProspect.class));
    }

    @Test
    void privacyNotApprovedRejectsRealCaptureWithoutSaving() {
        PlatformProspectController closed = new PlatformProspectController(
                new StoreResolver(lookup), repository,
                new PilotPrivacyReadiness(false, "", "", ""));
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> closed.capture(dto("main_form", null),
                        request("webempresarial.local:18080")));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void invalidWhatsappIsNotPersisted() {
        PlatformProspectRequest original = dto("main_form", null);
        PlatformProspectRequest invalid = new PlatformProspectRequest(
                original.nombre(), "abc", original.empresa(),
                original.instagram(), original.servicio(),
                original.presupuesto(), original.objetivo(),
                original.preferenciaPagoPiloto(), original.cohortHint(),
                original.form(), original.website());
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.capture(invalid,
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
