package com.webempresarial.store.service;

import com.webempresarial.store.event.AdminAccountCreatedEvent;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class AdminAccountInvitationMailerSendIT {

    @Autowired
    private AdminAccountInvitationService
            invitationService;

    @Test
    void shouldSendRealActivationEmail()
            throws Exception {

        String recipient =
                System.getenv(
                        "WEBEMPRESARIAL_TEST_EMAIL"
                );

        if (recipient == null
                || recipient.isBlank()) {

            throw new IllegalStateException(
                    "Define WEBEMPRESARIAL_TEST_EMAIL "
                    + "con un correo controlado antes "
                    + "de ejecutar esta prueba"
            );
        }

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        999999L,
                        recipient,
                        "Jovani",
                        "WebEmpresarial Test Store",
                        "test.web-empresarial.com",
                        "manual-test-token-123"
                );

        invitationService
                .sendActivationInvitation(event);
    }
}