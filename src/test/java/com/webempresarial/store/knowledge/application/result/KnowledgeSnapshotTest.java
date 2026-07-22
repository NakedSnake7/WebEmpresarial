package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeSnapshotTest {

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
    void shouldBeValidInsideValidityPeriod() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertTrue(
                snapshot.isValidAt(
                        VALID_FROM.plusDays(1)
                )
        );
    }

    @Test
    void shouldBeValidAtExactStart() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertTrue(
                snapshot.isValidAt(VALID_FROM)
        );
    }

    @Test
    void shouldNotBeValidBeforeStart() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertFalse(
                snapshot.isValidAt(
                        VALID_FROM.minusNanos(1)
                )
        );
    }

    @Test
    void shouldNotBeValidAtExpirationInstant() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertFalse(
                snapshot.isValidAt(VALID_UNTIL)
        );
    }

    @Test
    void shouldSupportOpenEndedValidity() {
        KnowledgeSnapshot snapshot =
                snapshotWithValidity(
                        VALID_FROM,
                        null
                );

        assertTrue(
                snapshot.isValidAt(
                        VALID_FROM.plusYears(50)
                )
        );
    }

    @Test
    void shouldEvaluateMinimumConfidence() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertTrue(
                snapshot.hasMinimumConfidence(
                        new BigDecimal("0.9000")
                )
        );

        assertTrue(
                snapshot.hasMinimumConfidence(
                        new BigDecimal("0.9500")
                )
        );

        assertFalse(
                snapshot.hasMinimumConfidence(
                        new BigDecimal("0.9600")
                )
        );
    }

    @Test
    void shouldIdentifyContext() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertTrue(
                snapshot.belongsToContext(
                        KnowledgeContextType.PROJECT,
                        " ROBERT-SLINGERLAND "
                )
        );

        assertFalse(
                snapshot.belongsToContext(
                        KnowledgeContextType.SYSTEM,
                        "ROBERT-SLINGERLAND"
                )
        );
    }

    @Test
    void shouldRejectInvalidValidityPeriod() {
        assertThrows(
                IllegalArgumentException.class,
                () -> snapshotWithValidity(
                        VALID_FROM,
                        VALID_FROM
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> snapshotWithValidity(
                        VALID_FROM,
                        VALID_FROM.minusDays(1)
                )
        );
    }

    @Test
    void shouldRejectConfidenceOutsideRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> snapshotWithConfidence(
                        new BigDecimal("1.0001")
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> snapshotWithConfidence(
                        new BigDecimal("-0.0001")
                )
        );
    }

    @Test
    void shouldRejectNullMoment() {
        KnowledgeSnapshot snapshot =
                validSnapshot();

        assertThrows(
                NullPointerException.class,
                () -> snapshot.isValidAt(null)
        );
    }

    private static KnowledgeSnapshot validSnapshot() {
        return snapshotWithValidity(
                VALID_FROM,
                VALID_UNTIL
        );
    }

    private static KnowledgeSnapshot snapshotWithValidity(
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) {
        return createSnapshot(
                new BigDecimal("0.9500"),
                validFrom,
                validUntil
        );
    }

    private static KnowledgeSnapshot snapshotWithConfidence(
            BigDecimal confidence
    ) {
        return createSnapshot(
                confidence,
                VALID_FROM,
                VALID_UNTIL
        );
    }

    private static KnowledgeSnapshot createSnapshot(
            BigDecimal confidence,
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) {
        return new KnowledgeSnapshot(
                100L,
                200L,
                15L,
                "KS-100",
                "1.0.0",
                "Knowledge Objects Specification",
                "Specification summary",
                "# Knowledge Objects",
                "MARKDOWN",
                confidence,
                null,
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeContextType.PROJECT,
                "ROBERT-SLINGERLAND",
                validFrom,
                validUntil,
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