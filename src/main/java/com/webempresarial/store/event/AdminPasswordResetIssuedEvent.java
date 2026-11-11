package com.webempresarial.store.event;

/*
 * El token original solamente debe viajar en memoria.
 * Nunca incluirlo en logs ni respuestas HTTP.
 */
public record AdminPasswordResetIssuedEvent(
        String email,
        String fullName,
        String rawToken
) {

    @Override
    public String toString() {
        return "AdminPasswordResetIssuedEvent[REDACTED]";
    }
}
