package com.webempresarial.store.knowledge.domain.value;

import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeCodeTest {

    @Test
    void shouldCreateValidKnowledgeCode() {
        KnowledgeCode code = KnowledgeCode.of("KS-100");

        assertEquals("KS-100", code.getValue());
        assertEquals("KS", code.getPrefix());
        assertEquals(100, code.getSequence());
        assertEquals("KS-100", code.toString());
    }

    @Test
    void shouldNormalizeLowercaseAndSpaces() {
        KnowledgeCode code =
                KnowledgeCode.of("  crm-015  ");

        assertEquals("CRM-015", code.getValue());
    }

    @Test
    void shouldAcceptDifferentValidPrefixes() {
        KnowledgeCode architecture =
                KnowledgeCode.of("ARCH-020");

        KnowledgeCode policy =
                KnowledgeCode.of("POLICY-001");

        assertEquals("ARCH-020", architecture.getValue());
        assertEquals("POLICY-001", policy.getValue());
    }

    @Test
    void shouldIdentifyPrefix() {
        KnowledgeCode code =
                KnowledgeCode.of("CRM-105");

        assertTrue(code.hasPrefix("CRM"));
        assertTrue(code.hasPrefix(" crm "));
        assertFalse(code.hasPrefix("KS"));
        assertFalse(code.hasPrefix(null));
        assertFalse(code.hasPrefix(" "));
    }

    @Test
    void shouldRejectNullOrBlankCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of(" ")
        );
    }

    @Test
    void shouldRejectCodeWithoutSeparator() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("KS100")
        );
    }

    @Test
    void shouldRejectCodeWithShortNumericPart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("KS-10")
        );
    }

    @Test
    void shouldRejectCodeWithLettersInSequence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("KS-ABC")
        );
    }

    @Test
    void shouldRejectPrefixWithOneCharacter() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("K-100")
        );
    }

    @Test
    void shouldRejectPrefixWithMoreThanTenCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("KNOWLEDGEOBJ-100")
        );
    }

    @Test
    void shouldRejectUnsupportedCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("KS_100")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeCode.of("KS 100")
        );
    }

    @Test
    void shouldCompareByValue() {
        KnowledgeCode first =
                KnowledgeCode.of("ks-100");

        KnowledgeCode second =
                KnowledgeCode.of("KS-100");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}