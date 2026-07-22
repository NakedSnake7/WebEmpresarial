package com.webempresarial.store.knowledge.domain.value;

import org.junit.jupiter.api.Test; 



import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeConfidenceTest {

    @Test
    void shouldCreateConfidenceFromBigDecimal() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.of(
                        new BigDecimal("0.8500")
                );

        assertEquals(
                new BigDecimal("0.8500"),
                confidence.getValue()
        );

        assertEquals(
                "0.8500",
                confidence.toString()
        );
    }

    @Test
    void shouldCreateConfidenceFromDouble() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.of(0.75);

        assertEquals(
                new BigDecimal("0.7500"),
                confidence.getValue()
        );
    }

    @Test
    void shouldNormalizeScale() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.of(
                        new BigDecimal("0.7")
                );

        assertEquals(
                new BigDecimal("0.7000"),
                confidence.getValue()
        );
    }

    @Test
    void shouldRoundUsingHalfUp() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.of(
                        new BigDecimal("0.75678")
                );

        assertEquals(
                new BigDecimal("0.7568"),
                confidence.getValue()
        );
    }

    @Test
    void shouldCreateZeroConfidence() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.zero();

        assertEquals(
                new BigDecimal("0.0000"),
                confidence.getValue()
        );

        assertTrue(confidence.isZero());
        assertFalse(confidence.isFull());
    }

    @Test
    void shouldCreateFullConfidence() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.full();

        assertEquals(
                new BigDecimal("1.0000"),
                confidence.getValue()
        );

        assertTrue(confidence.isFull());
        assertFalse(confidence.isZero());
    }

    @Test
    void shouldConvertToPercentage() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.of(
                        new BigDecimal("0.8575")
                );

        assertEquals(
                new BigDecimal("85.75"),
                confidence.asPercentage()
        );
    }

    @Test
    void shouldCompareConfidenceLevels() {
        KnowledgeConfidence high =
                KnowledgeConfidence.of(
                        new BigDecimal("0.9000")
                );

        KnowledgeConfidence medium =
                KnowledgeConfidence.of(
                        new BigDecimal("0.7000")
                );

        assertTrue(high.isHigherThan(medium));
        assertTrue(medium.isLowerThan(high));
        assertTrue(high.isAtLeast(medium));
        assertFalse(medium.isAtLeast(high));
    }

    @Test
    void shouldAcceptMinimumAndMaximumValues() {
        KnowledgeConfidence minimum =
                KnowledgeConfidence.of(
                        BigDecimal.ZERO
                );

        KnowledgeConfidence maximum =
                KnowledgeConfidence.of(
                        BigDecimal.ONE
                );

        assertEquals(
                new BigDecimal("0.0000"),
                minimum.getValue()
        );

        assertEquals(
                new BigDecimal("1.0000"),
                maximum.getValue()
        );
    }

    @Test
    void shouldRejectNegativeValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeConfidence.of(
                        new BigDecimal("-0.0001")
                )
        );
    }

    @Test
    void shouldRejectValueGreaterThanOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> KnowledgeConfidence.of(
                        new BigDecimal("1.0001")
                )
        );
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                NullPointerException.class,
                () -> KnowledgeConfidence.of(
                        (BigDecimal) null
                )
        );
    }

    @Test
    void shouldCompareEquivalentValuesByValue() {
        KnowledgeConfidence first =
                KnowledgeConfidence.of(
                        new BigDecimal("0.7")
                );

        KnowledgeConfidence second =
                KnowledgeConfidence.of(
                        new BigDecimal("0.7000")
                );

        assertEquals(first, second);
        assertEquals(
                first.hashCode(),
                second.hashCode()
        );
    }

    @Test
    void shouldRejectNullComparison() {
        KnowledgeConfidence confidence =
                KnowledgeConfidence.full();

        assertThrows(
                NullPointerException.class,
                () -> confidence.compareTo(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> confidence.isHigherThan(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> confidence.isAtLeast(null)
        );
    }
}