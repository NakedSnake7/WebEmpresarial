package com.webempresarial.store.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class AdminPasswordResetTokenCodec {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public String generateRawToken() {

        byte[] bytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String hashToken(String rawToken) {

        validateRawToken(rawToken);

        try {

            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                            rawToken.getBytes(
                                    StandardCharsets.US_ASCII
                            )
                    );

            return HexFormat.of()
                    .formatHex(digest);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 no disponible",
                    ex
            );
        }
    }

    private void validateRawToken(String rawToken) {

        if (rawToken == null
                || !rawToken.matches("[A-Za-z0-9_-]{43}")) {

            throw new IllegalArgumentException(
                    "Token de recuperación inválido"
            );
        }

        try {

            byte[] decoded = Base64
                    .getUrlDecoder()
                    .decode(rawToken);

            String canonical = Base64
                    .getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(decoded);

            if (decoded.length != TOKEN_BYTES
                    || !canonical.equals(rawToken)) {

                throw new IllegalArgumentException(
                        "Token de recuperación inválido"
                );
            }

        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException(
                    "Token de recuperación inválido"
            );
        }
    }
}
