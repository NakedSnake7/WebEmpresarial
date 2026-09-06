package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DeterministicStrategicThesisGeneratorTest {

    private final DeterministicStrategicThesisGenerator generator =
            new DeterministicStrategicThesisGenerator();

    @Test
    void shouldGenerateThesisUsingOnlyApprovedStatements() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        "La representación digital no refleja la marca."
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "La experiencia reduce la percepción de autoridad."
                );

        StrategicArtifact objective =
                artifact(
                        evidence,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Elevar la percepción digital de la marca."
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Construir una experiencia editorial premium."
                );

        StrategicChain chain =
                StrategicChain.of(
                        evidence.getProject(),
                        finding,
                        problem,
                        objective,
                        opportunity
                );

        String thesis =
                generator.generate(chain);

        assertThat(thesis)
                .contains(
                        problem.getStatement()
                )
                .contains(
                        objective.getStatement()
                )
                .contains(
                        opportunity.getStatement()
                );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            String code,
            StrategicArtifactType type,
            String statement
    ) {
        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                code,
                type,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                statement,
                null,
                null
        );
    }
}