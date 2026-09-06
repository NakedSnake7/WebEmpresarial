package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisTest {

    @Test
    void shouldCreateDeterministicReadySynthesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis synthesis =
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "La experiencia digital actual no refleja plenamente el valor de la marca.",
                        "Existe una brecha entre el valor real de la marca y su percepción digital.",
                        "Elevar la experiencia digital para representar adecuadamente el posicionamiento.",
                        "Construir una experiencia digital premium y orientada a autoridad.",
                        "La transformación debe cerrar la brecha entre valor real y percepción digital.",
                        evidenceSummary(),
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

        assertThat(synthesis.isReady())
                .isTrue();

        assertThat(synthesis.isAiAssisted())
                .isFalse();

        assertThat(synthesis.getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThat(synthesis.getConfidence())
                .isEqualTo(
                        StrategicSynthesisConfidence.HIGH
                );

        assertThat(synthesis.getSourceArtifactCodes())
                .containsExactly(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );
    }

    @Test
    void shouldNormalizeAndDeduplicateSourceArtifactCodes() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis synthesis =
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Problem",
                        "Objective",
                        "Opportunity",
                        "Strategic thesis",
                        evidenceSummary(),
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        List.of(
                                " FND-001 ",
                                "PRB-001",
                                "FND-001",
                                "OBJ-001",
                                "OPP-001"
                        )
                );

        assertThat(synthesis.getSourceArtifactCodes())
                .containsExactly(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );
    }

    @Test
    void shouldRejectBlankStrategicThesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        assertThatThrownBy(() ->
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Problem",
                        "Objective",
                        "Opportunity",
                        " ",
                        evidenceSummary(),
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        List.of(
                                "FND-001",
                                "PRB-001",
                                "OBJ-001",
                                "OPP-001"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "tesis estratégica"
                );
    }

    @Test
    void shouldRejectEmptySourceArtifactCodes() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        assertThatThrownBy(() ->
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Problem",
                        "Objective",
                        "Opportunity",
                        "Strategic thesis",
                        evidenceSummary(),
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        List.of()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "artefactos fuente"
                );
    }

    @Test
    void shouldRecognizeAiAssistedSynthesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis synthesis =
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Problem",
                        "Objective",
                        "Opportunity",
                        "AI assisted thesis",
                        evidenceSummary(),
                        StrategicSynthesisConfidence.MEDIUM,
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        List.of(
                                "FND-001",
                                "PRB-001",
                                "OBJ-001",
                                "OPP-001"
                        )
                );

        assertThat(synthesis.isAiAssisted())
                .isTrue();

        assertThat(synthesis.requiresReview())
                .isTrue();

        assertThat(synthesis.isReady())
                .isFalse();
    }

    private static StrategicSynthesisEvidenceSummary evidenceSummary() {
        return StrategicSynthesisEvidenceSummary.of(
                StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                100,
                List.of("EVD-AUDIT-001"),
                4
        );
    }
    @Test
    void shouldRejectSupportedSummaryWithoutEvidence() {
        assertThatThrownBy(() ->
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(),
                        4
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "debe contener evidencia"
                );
    }
    @Test
    void withStatusShouldCreateNewSynthesisWithoutMutatingOriginal() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis original =
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Problem",
                        "Objective",
                        "Opportunity",
                        "Thesis",
                        evidenceSummary(),
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

        StrategicSynthesis updated =
                original.withStatus(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(updated)
                .isNotSameAs(original);

        assertThat(original.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );

        assertThat(updated.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(updated.getStrategicThesis())
                .isEqualTo(
                        original.getStrategicThesis()
                );

        assertThat(updated.getSourceArtifactCodes())
                .containsExactlyElementsOf(
                        original.getSourceArtifactCodes()
                );
    }
}