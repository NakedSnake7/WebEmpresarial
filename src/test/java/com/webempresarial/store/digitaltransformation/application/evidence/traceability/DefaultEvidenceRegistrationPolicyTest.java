package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultEvidenceRegistrationPolicyTest {

    private final DefaultEvidenceRegistrationPolicy policy =
            new DefaultEvidenceRegistrationPolicy();

    @Test
    void shouldApproveVerifiedStrategicEvidence() {
        SourceEvidence evidence =
                verifiedEvidence(
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT
                );

        EvidenceRegistrationDecision decision =
                policy.evaluate(evidence);

        assertThat(decision.register()).isTrue();
    }

    @Test
    void shouldRejectUnverifiedEvidence() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-001",
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL,
                        "Hallazgo",
                        "Fragmento",
                        null,
                        EvidenceLocator.page(1)
                );

        EvidenceRegistrationDecision decision =
                policy.evaluate(evidence);

        assertThat(decision.register()).isFalse();
        assertThat(decision.reason())
                .contains("no está verificada");
    }

    @Test
    void shouldRejectNonStrategicClassification() {
        SourceEvidence evidence =
                verifiedEvidence(
                        EvidenceClassification.QUOTE,
                        EvidenceConfidence.EXPLICIT
                );

        EvidenceRegistrationDecision decision =
                policy.evaluate(evidence);

        assertThat(decision.register()).isFalse();
        assertThat(decision.reason())
                .contains("clasificación");
    }

    private static SourceEvidence verifiedEvidence(
            EvidenceClassification classification,
            EvidenceConfidence confidence
    ) {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-001",
                        classification,
                        confidence,
                        EvidenceExtractionOrigin.MANUAL,
                        "Afirmación estratégica",
                        "Fragmento respaldatorio",
                        null,
                        EvidenceLocator.page(1)
                );

        evidence.verify("Jovani Amacende");

        return evidence;
    }
}