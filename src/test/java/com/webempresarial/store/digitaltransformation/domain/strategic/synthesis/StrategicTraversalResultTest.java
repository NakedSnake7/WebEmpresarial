package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicTraversalResultTest {

    @Test
    void completeTraversalShouldProduceChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        StrategicTraversalResult result =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.COMPLETE,
                        finding,
                        problem,
                        objective,
                        opportunity,
                        List.of(),
                        List.of()
                );

        assertThat(result.isComplete()).isTrue();
        assertThat(result.canBuildChain()).isTrue();
        assertThat(result.toChain()).isPresent();

        assertThat(
                result.toChain()
                        .orElseThrow()
                        .getCompleteness()
        ).isEqualTo(
                StrategicChainCompleteness.COMPLETE
        );
    }

    @Test
    void ambiguousTraversalShouldNotProduceChain() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicTraversalAmbiguity ambiguity =
                new StrategicTraversalAmbiguity(
                        StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS,
                        "FND-001",
                        List.of(
                                "PRB-001",
                                "PRB-002"
                        ),
                        "Múltiples problemas"
                );

        StrategicTraversalResult result =
                StrategicTraversalResult.of(
                        StrategicTraversalStatus.AMBIGUOUS,
                        finding,
                        null,
                        null,
                        null,
                        List.of(),
                        List.of(ambiguity)
                );

        assertThat(result.isAmbiguous()).isTrue();
        assertThat(result.canBuildChain()).isFalse();
        assertThat(result.toChain()).isEmpty();
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            String code,
            StrategicArtifactType type
    ) {
        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                code,
                type,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                "Statement " + code,
                null,
                null
        );
    }
}