package com.webempresarial.store.knowledge.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeRiskLevelTest {

    @Test
    void shouldReturnMediumAsDefaultRisk() {
        assertEquals(
                KnowledgeRiskLevel.MEDIUM,
                KnowledgeRiskLevel.defaultValue()
        );
    }

    @Test
    void shouldIdentifyEnhancedReviewLevels() {
        assertTrue(
                KnowledgeRiskLevel.HIGH
                        .requiresEnhancedReview()
        );

        assertTrue(
                KnowledgeRiskLevel.CRITICAL
                        .requiresEnhancedReview()
        );

        assertFalse(
                KnowledgeRiskLevel.MEDIUM
                        .requiresEnhancedReview()
        );

        assertFalse(
                KnowledgeRiskLevel.LOW
                        .requiresEnhancedReview()
        );
    }

    @Test
    void shouldIdentifyCriticalRisk() {
        assertTrue(KnowledgeRiskLevel.CRITICAL.isCritical());
        assertFalse(KnowledgeRiskLevel.HIGH.isCritical());
    }

    @Test
    void shouldCompareRiskLevels() {
        assertTrue(
                KnowledgeRiskLevel.CRITICAL
                        .isHigherThan(
                                KnowledgeRiskLevel.HIGH
                        )
        );

        assertTrue(
                KnowledgeRiskLevel.HIGH
                        .isHigherThan(
                                KnowledgeRiskLevel.MEDIUM
                        )
        );

        assertFalse(
                KnowledgeRiskLevel.LOW
                        .isHigherThan(
                                KnowledgeRiskLevel.MEDIUM
                        )
        );
    }

    @Test
    void shouldRejectNullComparison() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeRiskLevel.MEDIUM
                        .isHigherThan(null)
        );
    }
}