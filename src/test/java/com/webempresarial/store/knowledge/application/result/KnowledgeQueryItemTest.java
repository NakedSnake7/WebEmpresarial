package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeQueryItemTest {

    private static final LocalDateTime VALID_FROM =
            LocalDateTime.of(
                    2026,
                    7,
                    22,
                    8,
                    0
            );

    private static final LocalDateTime VALID_UNTIL =
            LocalDateTime.of(
                    2027,
                    7,
                    22,
                    8,
                    0
            );

    @Test
    void shouldIdentifyCurrentVersion() {
        KnowledgeQueryItem item =
                publishedItem();

        assertTrue(item.hasCurrentVersion());
    }

    @Test
    void draftObjectMayNotHaveCurrentVersion() {
        KnowledgeQueryItem item =
                draftItem();

        assertFalse(item.hasCurrentVersion());
    }

    @Test
    void shouldBeEffectiveInsideValidityPeriod() {
        KnowledgeQueryItem item =
                publishedItem();

        assertTrue(
                item.isEffectiveAt(
                        VALID_FROM.plusDays(1)
                )
        );
    }

    @Test
    void shouldNotBeEffectiveBeforeValidity() {
        KnowledgeQueryItem item =
                publishedItem();

        assertFalse(
                item.isEffectiveAt(
                        VALID_FROM.minusSeconds(1)
                )
        );
    }

    @Test
    void shouldNotBeEffectiveAtExpirationMoment() {
        KnowledgeQueryItem item =
                publishedItem();

        assertFalse(
                item.isEffectiveAt(VALID_UNTIL)
        );
    }

    @Test
    void archivedObjectShouldNotBeEffective() {
        KnowledgeQueryItem item =
                createVersionedItem(
                        KnowledgeStatus.ARCHIVED
                );

        assertFalse(
                item.isEffectiveAt(
                        VALID_FROM.plusDays(1)
                )
        );
    }

    @Test
    void shouldEvaluateMinimumConfidence() {
        KnowledgeQueryItem item =
                publishedItem();

        assertTrue(
                item.hasMinimumConfidence(
                        new BigDecimal("0.9000")
                )
        );

        assertFalse(
                item.hasMinimumConfidence(
                        new BigDecimal("0.9600")
                )
        );
    }

    @Test
    void shouldRejectPartialVersionMetadata() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeQueryItem(
                        100L,
                        15L,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeStatus.DRAFT,
                        KnowledgeContextType.PROJECT,
                        "PROJECT-1",
                        null,
                        "1.0.0",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );
    }

    private static KnowledgeQueryItem publishedItem() {
        return createVersionedItem(
                KnowledgeStatus.PUBLISHED
        );
    }

    private static KnowledgeQueryItem createVersionedItem(
            KnowledgeStatus status
    ) {
        return new KnowledgeQueryItem(
                100L,
                15L,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                status,
                KnowledgeContextType.PROJECT,
                "ROBERT-SLINGERLAND",
                200L,
                "1.0.0",
                "Knowledge Objects Specification",
                "Specification summary",
                "MARKDOWN",
                new BigDecimal("0.9500"),
                VALID_FROM,
                VALID_UNTIL,
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        0
                ),
                LocalDateTime.of(
                        2026,
                        7,
                        22,
                        8,
                        0
                )
        );
    }

    private static KnowledgeQueryItem draftItem() {
        return new KnowledgeQueryItem(
                100L,
                15L,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeStatus.DRAFT,
                KnowledgeContextType.PROJECT,
                "ROBERT-SLINGERLAND",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        0
                ),
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        0
                )
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