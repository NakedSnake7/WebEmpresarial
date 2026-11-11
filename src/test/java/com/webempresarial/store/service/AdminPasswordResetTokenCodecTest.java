package com.webempresarial.store.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AdminPasswordResetTokenCodecTest {

    private final AdminPasswordResetTokenCodec codec =
            new AdminPasswordResetTokenCodec();

    @Test
    void generates256BitUrlSafeToken() {

        String token = codec.generateRawToken();

        assertEquals(43, token.length());

        assertTrue(
                token.matches("[A-Za-z0-9_-]{43}")
        );

        assertEquals(
                32,
                Base64.getUrlDecoder()
                        .decode(token)
                        .length
        );
    }

    @Test
    void producesDeterministicSha256Hash() {

        String token = codec.generateRawToken();

        String first = codec.hashToken(token);
        String second = codec.hashToken(token);

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertTrue(first.matches("[0-9a-f]{64}"));
        assertNotEquals(token, first);
    }

    @Test
    void distinctTokensProduceDistinctHashes() {

        String first = codec.generateRawToken();
        String second = codec.generateRawToken();

        assertNotEquals(first, second);

        assertNotEquals(
                codec.hashToken(first),
                codec.hashToken(second)
        );
    }

    @Test
    void rejectsMalformedAndNoncanonicalTokens() {

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.hashToken(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.hashToken("short")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.hashToken("a".repeat(43))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.hashToken("!".repeat(43))
        );
    }
}
