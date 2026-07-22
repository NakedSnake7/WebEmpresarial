package com.webempresarial.store.knowledge.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeClassificationTest {

    @Test
    void shouldReturnInternalAsDefaultClassification() {
        assertEquals(
                KnowledgeClassification.INTERNAL,
                KnowledgeClassification.defaultValue()
        );
    }

    @Test
    void shouldIdentifyPublicClassification() {
        assertTrue(KnowledgeClassification.PUBLIC.isPublic());
        assertFalse(KnowledgeClassification.INTERNAL.isPublic());
    }

    @Test
    void shouldIdentifyRestrictedAccess() {
        assertTrue(
                KnowledgeClassification.CONFIDENTIAL
                        .requiresRestrictedAccess()
        );

        assertTrue(
                KnowledgeClassification.RESTRICTED
                        .requiresRestrictedAccess()
        );

        assertFalse(
                KnowledgeClassification.INTERNAL
                        .requiresRestrictedAccess()
        );
    }

    @Test
    void shouldCompareRestrictionLevels() {
        assertTrue(
                KnowledgeClassification.RESTRICTED
                        .isMoreRestrictiveThan(
                                KnowledgeClassification.CONFIDENTIAL
                        )
        );

        assertTrue(
                KnowledgeClassification.CONFIDENTIAL
                        .isMoreRestrictiveThan(
                                KnowledgeClassification.INTERNAL
                        )
        );

        assertFalse(
                KnowledgeClassification.PUBLIC
                        .isMoreRestrictiveThan(
                                KnowledgeClassification.INTERNAL
                        )
        );
    }

    @Test
    void shouldRejectNullComparison() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeClassification.INTERNAL
                        .isMoreRestrictiveThan(null)
        );
    }
}