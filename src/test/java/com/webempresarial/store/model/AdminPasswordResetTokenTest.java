package com.webempresarial.store.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminPasswordResetTokenTest {

    private static final String HASH =
            "ab".repeat(32);

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 10, 12, 0);

    private AdminPasswordResetToken token() {

        return AdminPasswordResetToken.issue(
                new AdminUser(),
                HASH,
                NOW,
                NOW.plusMinutes(20)
        );
    }

    @Test
    void tokenIsUsableWithinItsLifetime() {

        var token = token();

        assertEquals(HASH, token.getTokenHash());
        assertEquals(NOW, token.getCreatedAt());
        assertNull(token.getConsumedAt());

        assertTrue(
                token.isUsableAt(NOW.plusMinutes(10))
        );
    }

    @Test
    void tokenExpiresAtExactBoundary() {

        var token = token();

        assertTrue(
                token.isUsableAt(
                        NOW.plusMinutes(20).minusNanos(1)
                )
        );

        assertFalse(
                token.isUsableAt(NOW.plusMinutes(20))
        );
    }

    @Test
    void tokenCannotBeConsumedTwice() {

        var token = token();

        token.consume(NOW.plusMinutes(1));

        assertNotNull(token.getConsumedAt());

        assertFalse(
                token.isUsableAt(NOW.plusMinutes(2))
        );

        assertThrows(
                IllegalStateException.class,
                () -> token.consume(NOW.plusMinutes(2))
        );
    }

    @Test
    void tokenCannotBeConsumedAfterExpiration() {

        var token = token();

        assertThrows(
                IllegalStateException.class,
                () -> token.consume(NOW.plusMinutes(21))
        );

        assertNull(token.getConsumedAt());
    }

    @Test
    void rejectsInvalidHash() {

        assertThrows(
                IllegalArgumentException.class,
                () -> AdminPasswordResetToken.issue(
                        new AdminUser(),
                        "token-original-inseguro",
                        NOW,
                        NOW.plusMinutes(20)
                )
        );
    }

    @Test
    void rejectsInvalidExpiration() {

        assertThrows(
                IllegalArgumentException.class,
                () -> AdminPasswordResetToken.issue(
                        new AdminUser(),
                        HASH,
                        NOW,
                        NOW
                )
        );
    }

    @Test
    void tokenCannotBeUsedBeforeCreation() {

        assertFalse(
                token().isUsableAt(
                        NOW.minusSeconds(1)
                )
        );
    }

    @Test
    void capturesCredentialVersionAtIssuance() {

        AdminUser admin = new AdminUser();
        admin.rotateCredentialVersion();

        AdminPasswordResetToken issued =
                AdminPasswordResetToken.issue(
                        admin,
                        HASH,
                        NOW,
                        NOW.plusMinutes(20)
                );

        assertEquals(
                1L,
                issued.getCredentialVersionAtIssue()
        );

        admin.rotateCredentialVersion();

        assertEquals(
                1L,
                issued.getCredentialVersionAtIssue()
        );
    }

}
