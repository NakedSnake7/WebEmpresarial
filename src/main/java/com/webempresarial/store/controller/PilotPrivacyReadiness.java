package com.webempresarial.store.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Bloqueo compartido: no se capturan datos si el aviso no está listo. */
@Component
public class PilotPrivacyReadiness {

    private final boolean approved;
    private final String responsibleName;
    private final String responsibleAddress;
    private final String privacyEmail;

    public PilotPrivacyReadiness(
            @Value("${webempresarial.pilot.privacy.approved:false}") boolean approved,
            @Value("${webempresarial.pilot.privacy.responsible-name:}") String responsibleName,
            @Value("${webempresarial.pilot.privacy.responsible-address:}") String responsibleAddress,
            @Value("${webempresarial.pilot.privacy.contact-email:}") String privacyEmail) {
        this.approved = approved;
        this.responsibleName = responsibleName;
        this.responsibleAddress = responsibleAddress;
        this.privacyEmail = privacyEmail;
    }

    public void requireApproved() {
        if (!approved || blank(responsibleName) || blank(responsibleAddress)
                || blank(privacyEmail)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Aviso de privacidad del piloto no habilitado");
        }
    }

    public String responsibleName() { return responsibleName; }
    public String responsibleAddress() { return responsibleAddress; }
    public String privacyEmail() { return privacyEmail; }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
