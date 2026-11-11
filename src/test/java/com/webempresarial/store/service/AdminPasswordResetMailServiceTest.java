package com.webempresarial.store.service;

import com.webempresarial.store.event.AdminPasswordResetIssuedEvent;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminPasswordResetMailServiceTest {

    private static final String RAW =
            "A".repeat(43);

    @Test
    void sendsRecoveryEmailThroughExistingProvider()
            throws IOException {

        EmailService email = mock(EmailService.class);

        AdminPasswordResetMailService service =
                new AdminPasswordResetMailService(
                        email,
                        "https://web-empresarial.com/"
                );

        service.sendRecoveryEmail(
                new AdminPasswordResetIssuedEvent(
                        "admin@example.invalid",
                        "Admin <Test>",
                        RAW
                )
        );

        var html = org.mockito.ArgumentCaptor
                .forClass(String.class);

        verify(email).enviarCorreoHTML(
                eq("admin@example.invalid"),
                eq("Recupera tu contraseña de WebEmpresarial"),
                html.capture()
        );

        assertTrue(
                html.getValue().contains(
                        "https://web-empresarial.com/"
                        + "admin/password/reset?token="
                        + RAW
                )
        );

        assertTrue(
                html.getValue().contains(
                        "Admin &lt;Test&gt;"
                )
        );

        assertFalse(
                html.getValue().contains(
                        "Admin <Test>"
                )
        );
    }

    @Test
    void rejectsUntrustedOrInsecureBaseUrl() {

        EmailService email = mock(EmailService.class);

        assertThrows(
                IllegalStateException.class,
                () -> new AdminPasswordResetMailService(
                        email,
                        "http://example.com"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> new AdminPasswordResetMailService(
                        email,
                        "https://example.com?redirect=evil"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> new AdminPasswordResetMailService(
                        email,
                        "https://user:pass@example.com"
                )
        );

        verifyNoInteractions(email);
    }

    @Test
    void rejectsInvalidRecoveryEvent()
            throws IOException {

        EmailService email = mock(EmailService.class);

        AdminPasswordResetMailService service =
                new AdminPasswordResetMailService(
                        email,
                        "https://web-empresarial.com"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendRecoveryEmail(
                        new AdminPasswordResetIssuedEvent(
                                "admin@example.invalid",
                                "Admin",
                                "invalid"
                        )
                )
        );

        verifyNoInteractions(email);
    }
}
