package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.AdminPasswordChangeService;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminPasswordChangeControllerTest {

    private static final String EMAIL = "admin@example.com";
    private static final String CURRENT = "ClaveActual2026!";
    private static final String NEW_PASSWORD =
            "NuevaClaveSegura2026!";

    private final AdminPasswordChangeService service =
            mock(AdminPasswordChangeService.class);

    private final AdminPasswordChangeController controller =
            new AdminPasswordChangeController(service);

    private Authentication principal() {
        return new UsernamePasswordAuthenticationToken(
                EMAIL,
                null
        );
    }

    @Test
    void showsPasswordForm() {
        assertEquals(
                "admin/account/change-password",
                controller.showForm()
        );
    }

    @Test
    void rejectsPasswordConfirmationMismatch() {
        Model model = new ExtendedModelMap();

        String result = controller.changePassword(
                CURRENT,
                NEW_PASSWORD,
                "OtraClaveSegura2026!",
                principal(),
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                model
        );

        assertEquals(
                "admin/account/change-password",
                result
        );

        assertNotNull(
                model.getAttribute("passwordError")
        );

        verifyNoInteractions(service);
    }

    @Test
    void changesPasswordAndLogsOutCurrentSession() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        String result = controller.changePassword(
                CURRENT,
                NEW_PASSWORD,
                NEW_PASSWORD,
                principal(),
                request,
                new MockHttpServletResponse(),
                new ExtendedModelMap()
        );

        assertEquals(
                "redirect:/admin/login?passwordChanged",
                result
        );

        verify(service).changeOwnPassword(
                EMAIL,
                CURRENT,
                NEW_PASSWORD
        );

        assertTrue(session.isInvalid());
    }

    @Test
    void incorrectCurrentPasswordKeepsFormOpen() {
        doThrow(new IllegalArgumentException(
                "La contraseña actual es incorrecta"
        )).when(service).changeOwnPassword(
                EMAIL,
                CURRENT,
                NEW_PASSWORD
        );

        Model model = new ExtendedModelMap();

        String result = controller.changePassword(
                CURRENT,
                NEW_PASSWORD,
                NEW_PASSWORD,
                principal(),
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                model
        );

        assertEquals(
                "admin/account/change-password",
                result
        );

        assertEquals(
                "La contraseña actual es incorrecta",
                model.getAttribute("passwordError")
        );
    }

    @Test
    void accessDeniedIsNotConvertedToFormError() {
        doThrow(new AccessDeniedException(
                "Cuenta administrativa no disponible"
        )).when(service).changeOwnPassword(
                EMAIL,
                CURRENT,
                NEW_PASSWORD
        );

        assertThrows(
                AccessDeniedException.class,
                () -> controller.changePassword(
                        CURRENT,
                        NEW_PASSWORD,
                        NEW_PASSWORD,
                        principal(),
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse(),
                        new ExtendedModelMap()
                )
        );
    }
}
