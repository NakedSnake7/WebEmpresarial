package com.webempresarial.store.service;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class AdminAccountInvitationResendE2EIT {

    @Autowired
    private AdminAccountInvitationResendService resendService;

    @Test
    void shouldResendRealPendingActivationInvitation()
            throws Exception {

        String adminIdValue =
                System.getenv(
                        "WEBEMPRESARIAL_TEST_ADMIN_ID"
                );

        if (adminIdValue == null
                || adminIdValue.isBlank()) {

            throw new IllegalStateException(
                    "Define WEBEMPRESARIAL_TEST_ADMIN_ID "
                    + "con el ID de un administrador "
                    + "de prueba antes de ejecutar "
                    + "esta prueba"
            );
        }

        long adminId;

        try {
            adminId =
                    Long.parseLong(
                            adminIdValue.trim()
                    );
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "WEBEMPRESARIAL_TEST_ADMIN_ID "
                    + "debe ser un número entero válido",
                    ex
            );
        }

        resendService.resend(adminId);
    }
}
