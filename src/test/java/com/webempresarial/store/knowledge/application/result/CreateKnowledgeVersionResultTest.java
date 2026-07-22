package com.webempresarial.store.knowledge.application.result;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateKnowledgeVersionResultTest {

    @Test
    void shouldCreateResult() {
        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        22,
                        30
                );

        CreateKnowledgeVersionResult result =
                new CreateKnowledgeVersionResult(
                        200L,
                        100L,
                        15L,
                        " 1.0.0 ",
                        " Knowledge Objects Specification ",
                        " Specification summary ",
                        " MARKDOWN ",
                        new BigDecimal("0.9500"),
                        " https://example.com/source ",
                        " admin@webempresarial.com ",
                        createdAt,
                        0L
                );

        assertEquals(200L, result.id());
        assertEquals(100L, result.knowledgeObjectId());
        assertEquals(15L, result.storeId());
        assertEquals("1.0.0", result.semanticVersion());

        assertEquals(
                "Knowledge Objects Specification",
                result.title()
        );

        assertEquals(
                "Specification summary",
                result.summary()
        );

        assertEquals(
                "MARKDOWN",
                result.contentFormat()
        );

        assertEquals(
                new BigDecimal("0.9500"),
                result.confidence()
        );

        assertEquals(
                "https://example.com/source",
                result.sourceReference()
        );

        assertEquals(
                "admin@webempresarial.com",
                result.createdBy()
        );

        assertEquals(createdAt, result.createdAt());
        assertEquals(0L, result.lockVersion());
    }

    @Test
    void shouldAllowMissingSourceReference() {
        CreateKnowledgeVersionResult result =
                new CreateKnowledgeVersionResult(
                        200L,
                        100L,
                        15L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        " ",
                        "admin",
                        LocalDateTime.now(),
                        0L
                );

        assertNull(result.sourceReference());
    }

    @Test
    void shouldRejectInvalidVersionId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validResult(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validResult(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validResult(-1L)
        );
    }

    @Test
    void shouldRejectBlankSemanticVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionResult(
                        200L,
                        100L,
                        15L,
                        " ",
                        "Title",
                        "Summary",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin",
                        LocalDateTime.now(),
                        0L
                )
        );
    }

    @Test
    void shouldRejectMissingConfidence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionResult(
                        200L,
                        100L,
                        15L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "MARKDOWN",
                        null,
                        null,
                        "admin",
                        LocalDateTime.now(),
                        0L
                )
        );
    }

    @Test
    void shouldRejectInvalidLockVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CreateKnowledgeVersionResult(
                        200L,
                        100L,
                        15L,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        null,
                        "admin",
                        LocalDateTime.now(),
                        -1L
                )
        );
    }

    private static CreateKnowledgeVersionResult validResult(
            Long id
    ) {
        return new CreateKnowledgeVersionResult(
                id,
                100L,
                15L,
                "1.0.0",
                "Title",
                "Summary",
                "MARKDOWN",
                BigDecimal.ONE,
                null,
                "admin",
                LocalDateTime.now(),
                0L
        );
    }
}