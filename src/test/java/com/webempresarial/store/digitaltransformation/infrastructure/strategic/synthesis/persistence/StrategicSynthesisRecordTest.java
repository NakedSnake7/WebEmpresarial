package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisRecordTest {

    @Test
    void shouldPreserveSynthesisThroughRecordRoundTrip() {
        StrategicSynthesis original =
                synthesis();

        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        original
                );

        StrategicSynthesis reconstructed =
                record.toDomain();

        assertThat(reconstructed.getProject())
                .isSameAs(
                        original.getProject()
                );

        assertThat(reconstructed.getFindingStatement())
                .isEqualTo(
                        original.getFindingStatement()
                );

        assertThat(reconstructed.getBusinessProblemStatement())
                .isEqualTo(
                        original.getBusinessProblemStatement()
                );

        assertThat(reconstructed.getBusinessObjectiveStatement())
                .isEqualTo(
                        original.getBusinessObjectiveStatement()
                );

        assertThat(reconstructed.getStrategicOpportunityStatement())
                .isEqualTo(
                        original.getStrategicOpportunityStatement()
                );

        assertThat(reconstructed.getStrategicThesis())
                .isEqualTo(
                        original.getStrategicThesis()
                );

        assertThat(reconstructed.getConfidence())
                .isEqualTo(
                        original.getConfidence()
                );

        assertThat(reconstructed.getOrigin())
                .isEqualTo(
                        original.getOrigin()
                );

        assertThat(reconstructed.getStatus())
                .isEqualTo(
                        original.getStatus()
                );
    }

    @Test
    void shouldPreserveEvidenceSummary() {
        StrategicSynthesis original =
                synthesis();

        StrategicSynthesis reconstructed =
                StrategicSynthesisRecord
                        .from(original)
                        .toDomain();

        StrategicSynthesisEvidenceSummary expected =
                original.getEvidenceSummary();

        StrategicSynthesisEvidenceSummary actual =
                reconstructed.getEvidenceSummary();

        assertThat(actual.getCoverageStatus())
                .isEqualTo(
                        expected.getCoverageStatus()
                );

        assertThat(actual.getCoveragePercentage())
                .isEqualTo(
                        expected.getCoveragePercentage()
                );

        assertThat(actual.getMaximumTraceDepth())
                .isEqualTo(
                        expected.getMaximumTraceDepth()
                );

        assertThat(actual.getEvidenceCodes())
                .containsExactlyElementsOf(
                        expected.getEvidenceCodes()
                );
    }

    @Test
    void shouldPreserveSourceArtifactOrder() {
        StrategicSynthesis original =
                synthesis();

        StrategicSynthesis reconstructed =
                StrategicSynthesisRecord
                        .from(original)
                        .toDomain();

        assertThat(
                reconstructed.getSourceArtifactCodes()
        ).containsExactly(
                "FND-001",
                "PRB-001",
                "OBJ-001",
                "OPP-001"
        );
    }

    @Test
    void shouldRejectNullSynthesis() {
        assertThatThrownBy(() ->
                StrategicSynthesisRecord.from(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "síntesis"
                );
    }

    private static StrategicSynthesis synthesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                "Strategic thesis",
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001",
                                "EVD-002"
                        ),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                StrategicSynthesisOrigin.DETERMINISTIC,
                StrategicSynthesisStatus.READY,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
    @Test
    void shouldRejectConversionToStoredSynthesisBeforePersistence() {
        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        synthesis()
                );

        assertThatThrownBy(
                record::toStoredSynthesis
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "persistido"
                );
    }
}