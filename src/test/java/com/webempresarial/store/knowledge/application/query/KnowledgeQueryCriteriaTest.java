package com.webempresarial.store.knowledge.application.query;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeQueryCriteriaTest {

    @Test
    void shouldCreateDefaultCriteriaForStore() {
        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.forStore(15L);

        assertEquals(15L, criteria.storeId());

        assertEquals(
                KnowledgeQueryCriteria.DEFAULT_PAGE,
                criteria.page()
        );

        assertEquals(
                KnowledgeQueryCriteria.DEFAULT_SIZE,
                criteria.size()
        );

        assertNull(criteria.code());
        assertNull(criteria.status());
        assertFalse(criteria.requiresVersionJoin());
    }

    @Test
    void shouldBuildPublishedQueryWithFilters() {
        LocalDateTime effectiveAt =
                LocalDateTime.of(
                        2026,
                        7,
                        22,
                        10,
                        0
                );

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria
                        .builder(15L)
                        .code(" KS-100 ")
                        .publishedOnly()
                        .context(
                                KnowledgeContextType.PROJECT,
                                " ROBERT-SLINGERLAND "
                        )
                        .minimumConfidence(
                                new BigDecimal("0.9000")
                        )
                        .effectiveAt(effectiveAt)
                        .text(" architecture ")
                        .page(2)
                        .size(50)
                        .build();

        assertEquals("KS-100", criteria.code());

        assertEquals(
                KnowledgeStatus.PUBLISHED,
                criteria.status()
        );

        assertEquals(
                KnowledgeContextType.PROJECT,
                criteria.contextType()
        );

        assertEquals(
                "ROBERT-SLINGERLAND",
                criteria.contextReference()
        );

        assertEquals(
                new BigDecimal("0.9000"),
                criteria.minimumConfidence()
        );

        assertEquals(effectiveAt, criteria.effectiveAt());
        assertEquals("architecture", criteria.text());
        assertEquals(2, criteria.page());
        assertEquals(50, criteria.size());

        assertTrue(criteria.hasCode());
        assertTrue(criteria.hasContext());
        assertTrue(criteria.hasMinimumConfidence());
        assertTrue(criteria.hasEffectiveMoment());
        assertTrue(criteria.hasText());
        assertTrue(criteria.requiresVersionJoin());
    }

    @Test
    void shouldNormalizeBlankOptionalValuesToNull() {
        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria
                        .builder(15L)
                        .code(" ")
                        .text(" ")
                        .build();

        assertNull(criteria.code());
        assertNull(criteria.text());
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .forStore(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .forStore(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .forStore(-1L)
        );
    }

    @Test
    void shouldRejectContextReferenceWithoutType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new KnowledgeQueryCriteria(
                        15L,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "PROJECT-1",
                        null,
                        null,
                        null,
                        0,
                        20
                )
        );
    }

    @Test
    void shouldRejectContextTypeWithoutReference() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .context(
                                KnowledgeContextType.PROJECT,
                                " "
                        )
                        .build()
        );
    }

    @Test
    void shouldRejectConfidenceOutsideRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .minimumConfidence(
                                new BigDecimal("-0.0001")
                        )
                        .build()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .minimumConfidence(
                                new BigDecimal("1.0001")
                        )
                        .build()
        );
    }

    @Test
    void shouldAcceptMinimumAndMaximumConfidence() {
        KnowledgeQueryCriteria minimum =
                KnowledgeQueryCriteria
                        .builder(15L)
                        .minimumConfidence(BigDecimal.ZERO)
                        .build();

        KnowledgeQueryCriteria maximum =
                KnowledgeQueryCriteria
                        .builder(15L)
                        .minimumConfidence(BigDecimal.ONE)
                        .build();

        assertEquals(
                BigDecimal.ZERO,
                minimum.minimumConfidence()
        );

        assertEquals(
                BigDecimal.ONE,
                maximum.minimumConfidence()
        );
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .page(-1)
                        .build()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .size(0)
                        .build()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .size(
                                KnowledgeQueryCriteria.MAX_SIZE + 1
                        )
                        .build()
        );
    }

    @Test
    void shouldRejectTextLongerThanMaximum() {
        String text =
                "A".repeat(
                        KnowledgeQueryCriteria
                                .MAX_TEXT_LENGTH
                                + 1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeQueryCriteria
                        .builder(15L)
                        .text(text)
                        .build()
        );
    }
}