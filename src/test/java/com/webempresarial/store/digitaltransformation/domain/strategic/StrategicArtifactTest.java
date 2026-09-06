package com.webempresarial.store.digitaltransformation.domain.strategic;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicArtifactTest {

    @Test
    void shouldCreateFindingInDraftStatus() {
        TransformationProject project =
                TestSources.validProject();

        StrategicArtifact artifact =
                StrategicArtifact.create(
                        project,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Existe una brecha entre la marca y su plataforma digital.",
                        "La auditoría identifica directamente esta diferencia.",
                        "La percepción digital puede reducir la autoridad percibida."
                );

        assertThat(artifact.getStatus())
                .isEqualTo(
                        StrategicArtifactStatus.DRAFT
                );

        assertThat(artifact.getPriority())
                .isEqualTo(
                        StrategicPriority.UNASSESSED
                );

        assertThat(artifact.isRequiresReview())
                .isFalse();

        assertThat(artifact.canDriveImplementation())
                .isFalse();
    }

    @Test
    void shouldRequireReviewForInferredArtifact() {
        StrategicArtifact artifact =
                StrategicArtifact.create(
                        TestSources.validProject(),
                        "OPP-001",
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        StrategicConfidence.INFERRED,
                        StrategicArtifactOrigin.AI_ASSISTED,
                        "Crear una experiencia editorial premium.",
                        null,
                        null
                );

        assertThat(artifact.isRequiresReview())
                .isTrue();
    }

    @Test
    void shouldAllowVerifiedArtifactToDriveImplementation() {
        StrategicArtifact artifact =
                StrategicArtifact.create(
                        TestSources.validProject(),
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicConfidence.STRONGLY_SUPPORTED,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Elevar la percepción digital de la marca.",
                        null,
                        null
                );

        artifact.verify(
                "Jovani Amacende"
        );

        assertThat(artifact.canDriveImplementation())
                .isTrue();

        assertThat(artifact.getStatus())
                .isEqualTo(
                        StrategicArtifactStatus.VERIFIED
                );
    }

    @Test
    void shouldRejectVerificationWithoutReviewer() {
        StrategicArtifact artifact =
                StrategicArtifact.create(
                        TestSources.validProject(),
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.MANUAL,
                        "Hallazgo",
                        null,
                        null
                );

        assertThatThrownBy(() ->
                artifact.verify(" ")
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "responsable"
                );
    }

    @Test
    void shouldAssignPriority() {
        StrategicArtifact artifact =
                StrategicArtifact.create(
                        TestSources.validProject(),
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.MANUAL,
                        "Problema empresarial",
                        null,
                        null
                );

        artifact.assignPriority(
                StrategicPriority.HIGH
        );

        assertThat(artifact.getPriority())
                .isEqualTo(
                        StrategicPriority.HIGH
                );
    }
    @Test
    void shouldDeriveArtifactFromEvidence() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Existe una brecha digital.",
                        "Derivado de evidencia documental.",
                        null
                );

        assertThat(artifact.getSourceEvidence())
                .isSameAs(evidence);

        assertThat(artifact.getArtifactType())
                .isEqualTo(
                        StrategicArtifactType.FINDING
                );
    }
}