package com.webempresarial.store.digitaltransformation.domain.evidence;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;



class SourceEvidenceTest {

    @Test
    void shouldExtractExplicitEvidenceWithoutMandatoryReview() {
        TransformationSourceDocument source =
                TestSources.validSource();

        TransformationProject project =
                source.getProject();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        project,
                        source,
                        null,
                        "EVD-AUDIT-001",
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL,
                        "La marca de Robert es más fuerte " +
                        "que su plataforma digital.",
                        "La marca de Robert Slingerland es hoy " +
                        "más fuerte que su plataforma digital.",
                        "Existe una brecha entre la marca " +
                        "y su experiencia digital.",
                        EvidenceLocator.page(2)
                );

        assertThat(evidence.getStatus())
                .isEqualTo(EvidenceStatus.EXTRACTED);

        assertThat(evidence.isRequiresHumanReview())
                .isFalse();

        assertThat(evidence.canGenerateRequirements())
                .isFalse();
    }

    @Test
    void shouldRequireReviewForInferredEvidence() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-AUDIT-002",
                        EvidenceClassification.EXPERIENCE_REQUIREMENT,
                        EvidenceConfidence.INFERRED,
                        EvidenceExtractionOrigin.AI_ASSISTED,
                        "Debe existir una página dedicada " +
                        "a la filosofía.",
                        "La filosofía debe estar en el centro " +
                        "de la experiencia.",
                        "La página independiente es una solución " +
                        "arquitectónica inferida.",
                        EvidenceLocator.page(3)
                );

        assertThat(evidence.isRequiresHumanReview())
                .isTrue();
    }

    @Test
    void shouldAllowVerifiedEvidenceToGenerateRequirements() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-AUDIT-003",
                        EvidenceClassification.SEO_REQUIREMENT,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL,
                        "Se debe implementar una base técnica " +
                        "y semántica de SEO.",
                        "Construir una base técnica y semántica " +
                        "de SEO completa.",
                        null,
                        EvidenceLocator.page(11)
                );

        evidence.verify("Jovani Amacende");

        assertThat(evidence.getStatus())
                .isEqualTo(EvidenceStatus.VERIFIED);

        assertThat(evidence.canGenerateRequirements())
                .isTrue();

        assertThat(evidence.isRequiresHumanReview())
                .isFalse();
    }

    @Test
    void shouldRejectVerificationWithoutReviewer() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-001",
                        EvidenceClassification.OTHER,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL,
                        "Afirmación",
                        "Fragmento",
                        null,
                        EvidenceLocator.page(1)
                );

        assertThatThrownBy(() ->
                evidence.verify(" ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("responsable");
    }

    @Test
    void shouldRejectInvalidLocator() {
        assertThatThrownBy(() ->
                EvidenceLocator.pages(4, 2)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("página final");
    }
}