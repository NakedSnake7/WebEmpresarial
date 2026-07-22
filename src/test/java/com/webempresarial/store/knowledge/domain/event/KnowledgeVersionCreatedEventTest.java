package com.webempresarial.store.knowledge.domain.event;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KnowledgeVersionCreatedEventTest {

    @Test
    void shouldCreateEvent() {
        LocalDateTime occurredAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        22,
                        45
                );

        KnowledgeVersionCreatedEvent event =
                new KnowledgeVersionCreatedEvent(
                        200L,
                        100L,
                        15L,
                        " 1.0.0 ",
                        " Knowledge Objects Specification ",
                        " MARKDOWN ",
                        new BigDecimal("0.9500"),
                        " admin@webempresarial.com ",
                        occurredAt
                );

        assertEquals(
                200L,
                event.knowledgeVersionId()
        );

        assertEquals(
                100L,
                event.knowledgeObjectId()
        );

        assertEquals(
                15L,
                event.storeId()
        );

        assertEquals(
                "1.0.0",
                event.semanticVersion()
        );

        assertEquals(
                "Knowledge Objects Specification",
                event.title()
        );

        assertEquals(
                "MARKDOWN",
                event.contentFormat()
        );

        assertEquals(
                new BigDecimal("0.9500"),
                event.confidence()
        );

        assertEquals(
                "admin@webempresarial.com",
                event.actor()
        );

        assertEquals(
                occurredAt,
                event.occurredAt()
        );
    }

    @Test
    void shouldRejectInvalidIdentifiers() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validEvent(null, 100L, 15L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validEvent(200L, null, 15L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validEvent(200L, 100L, null)
        );
    }

    @Test
    void shouldRejectMissingConfidence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeVersionCreatedEvent(
                        200L,
                        100L,
                        15L,
                        "1.0.0",
                        "Title",
                        "MARKDOWN",
                        null,
                        "admin",
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectMissingOccurredAt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeVersionCreatedEvent(
                        200L,
                        100L,
                        15L,
                        "1.0.0",
                        "Title",
                        "MARKDOWN",
                        BigDecimal.ONE,
                        "admin",
                        null
                )
        );
    }

    private static KnowledgeVersionCreatedEvent validEvent(
            Long knowledgeVersionId,
            Long knowledgeObjectId,
            Long storeId
    ) {
        return new KnowledgeVersionCreatedEvent(
                knowledgeVersionId,
                knowledgeObjectId,
                storeId,
                "1.0.0",
                "Title",
                "MARKDOWN",
                BigDecimal.ONE,
                "admin",
                LocalDateTime.now()
        );
    }
}