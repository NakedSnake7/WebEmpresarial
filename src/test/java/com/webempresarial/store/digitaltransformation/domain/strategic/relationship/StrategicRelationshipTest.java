package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicRelationshipTest {

    @Test
    void shouldCreateFindingToProblemRelationship() {
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

        StrategicRelationship relationship =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        "El hallazgo evidencia el problema de negocio."
                );

        assertThat(relationship.isActive())
                .isTrue();

        assertThat(relationship.getSourceArtifact())
                .isSameAs(finding);

        assertThat(relationship.getTargetArtifact())
                .isSameAs(problem);

        assertThat(relationship.getRelationshipType())
                .isEqualTo(
                        StrategicRelationshipType.REVEALS
                );

        assertThat(relationship.getOrigin())
                .isEqualTo(
                        StrategicRelationshipOrigin.RULE_ENGINE
                );
    }

    @Test
    void shouldRejectInvalidStrategicRelationship() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact opportunity =
                artifact(
                        evidence,
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        assertThatThrownBy(() ->
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        opportunity,
                        StrategicRelationshipType.ENABLES,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        "Relación inválida"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void shouldRejectSelfRelationship() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        assertThatThrownBy(() ->
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        finding,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.MANUAL,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "consigo mismo"
                );
    }

    @Test
    void shouldRejectRelationship() {
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

        StrategicRelationship relationship =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.MANUAL,
                        null
                );

        relationship.reject();

        assertThat(relationship.getStatus())
                .isEqualTo(
                        StrategicRelationshipStatus.REJECTED
                );

        assertThat(relationship.isActive())
                .isFalse();
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
                "Strategic statement " + code,
                "Strategic rationale " + code,
                null
        );
    }
}