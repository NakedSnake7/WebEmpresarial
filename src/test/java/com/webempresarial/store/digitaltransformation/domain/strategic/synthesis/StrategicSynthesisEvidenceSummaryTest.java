package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisEvidenceSummaryTest {

    @Test
    void shouldCreateEvidenceSummary() {
        StrategicSynthesisEvidenceSummary summary =
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-AUDIT-001",
                                "EVD-PROPOSAL-001"
                        ),
                        4
                );

        assertThat(summary.getCoverageStatus())
                .isEqualTo(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                );

        assertThat(summary.getCoveragePercentage())
                .isEqualTo(100);

        assertThat(summary.getEvidenceCodes())
                .containsExactly(
                        "EVD-AUDIT-001",
                        "EVD-PROPOSAL-001"
                );

        assertThat(summary.getMaximumTraceDepth())
                .isEqualTo(4);

        assertThat(summary.hasEvidence())
                .isTrue();
    }

    @Test
    void shouldRejectInvalidCoveragePercentage() {
        assertThatThrownBy(() ->
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        101,
                        List.of("EVD-001"),
                        1
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "entre 0 y 100"
                );
    }

    @Test
    void evidenceCodesShouldBeImmutable() {
        StrategicSynthesisEvidenceSummary summary =
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of("EVD-001"),
                        1
                );

        assertThatThrownBy(() ->
                summary.getEvidenceCodes()
                        .add("EVD-002")
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }
    @Test
    void shouldBeEqualWhenEvidenceSummaryHasSameValue() {

        StrategicSynthesisEvidenceSummary first =
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001",
                                "EVD-002"
                        ),
                        4
                );

        StrategicSynthesisEvidenceSummary second =
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001",
                                "EVD-002"
                        ),
                        4
                );

        assertThat(first)
                .isEqualTo(second);

        assertThat(first.hashCode())
                .isEqualTo(
                        second.hashCode()
                );
    }
    @Test
    void shouldNotBeEqualWhenEvidenceSummaryDiffers() {

        StrategicSynthesisEvidenceSummary first =
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of("EVD-001"),
                        4
                );

        StrategicSynthesisEvidenceSummary second =
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED,
                        75,
                        List.of("EVD-001"),
                        3
                );

        assertThat(first)
                .isNotEqualTo(second);
    }
}