package com.webempresarial.store.knowledge.domain.event;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KnowledgeObjectCreatedEventTest {

    @Test
    void shouldCreateEvent() {
        LocalDateTime occurredAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        21,
                        0
                );

        KnowledgeObjectCreatedEvent event =
                new KnowledgeObjectCreatedEvent(
                        100L,
                        15L,
                        " KS-100 ",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        " ROBERT-SLINGERLAND ",
                        " admin@webempresarial.com ",
                        occurredAt
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
                "KS-100",
                event.code()
        );

        assertEquals(
                "ROBERT-SLINGERLAND",
                event.contextReference()
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
                () -> createEvent(null, 1L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> createEvent(1L, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> createEvent(0L, 1L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> createEvent(1L, 0L)
        );
    }

    @Test
    void shouldRejectMissingOccurredAt() {
        assertThrows(
                NullPointerException.class,
                () -> new KnowledgeObjectCreatedEvent(
                        1L,
                        1L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        "admin",
                        null
                )
        );
    }

    private static KnowledgeObjectCreatedEvent createEvent(
            Long knowledgeObjectId,
            Long storeId
    ) {
        return new KnowledgeObjectCreatedEvent(
                knowledgeObjectId,
                storeId,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeStatus.DRAFT,
                KnowledgeContextType.PROJECT,
                "PROJECT-1",
                "admin",
                LocalDateTime.now()
        );
    }

    private static KnowledgeTypeCode firstTypeCode() {
        return KnowledgeTypeCode.values()[0];
    }

    private static KnowledgeDomain firstDomain() {
        return KnowledgeDomain.values()[0];
    }

    private static KnowledgeClassification firstClassification() {
        return KnowledgeClassification.values()[0];
    }

    private static KnowledgeRiskLevel firstRiskLevel() {
        return KnowledgeRiskLevel.values()[0];
    }
}