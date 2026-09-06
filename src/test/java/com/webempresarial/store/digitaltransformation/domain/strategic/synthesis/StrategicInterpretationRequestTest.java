package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicInterpretationRequestTest {

    @Test
    void shouldBuildRequestFromDeterministicSynthesis() {
        StrategicSynthesis synthesis =
                deterministicSynthesis();

        StrategicInterpretationRequest request =
                StrategicInterpretationRequest.from(
                        synthesis,
                        StrategicInterpretationMode.REFINE_THESIS
                );

        assertThat(request.getMode())
                .isEqualTo(
                        StrategicInterpretationMode.REFINE_THESIS
                );

        assertThat(request.getFinding())
                .isEqualTo(
                        synthesis.getFindingStatement()
                );

        assertThat(request.getBusinessProblem())
                .isEqualTo(
                        synthesis.getBusinessProblemStatement()
                );

        assertThat(request.getBusinessObjective())
                .isEqualTo(
                        synthesis.getBusinessObjectiveStatement()
                );

        assertThat(request.getStrategicOpportunity())
                .isEqualTo(
                        synthesis.getStrategicOpportunityStatement()
                );

        assertThat(request.getSourceArtifactCodes())
                .containsExactly(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );

        assertThat(request.getConstraints())
                .contains(
                        StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_FACTS,
                        StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_OBJECTIVES,
                        StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_OPPORTUNITIES
                );
    }

    @Test
    void shouldRejectAiAssistedSynthesisAsBaseContext() {
        StrategicSynthesis synthesis =
                synthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED
                );

        assertThatThrownBy(() ->
                StrategicInterpretationRequest.from(
                        synthesis,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "DETERMINISTIC"
                );
    }

    private static StrategicSynthesis deterministicSynthesis() {
        return synthesis(
                StrategicSynthesisOrigin.DETERMINISTIC
        );
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisOrigin origin
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                "Deterministic strategic thesis",
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of("EVD-001"),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                origin,
                StrategicSynthesisStatus.READY,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}