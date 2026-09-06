package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicClassificationEngineTest {

    private final StrategicConfidenceEvaluator confidenceEvaluator =
            new DefaultStrategicConfidenceEvaluator();

    @Test
    void shouldClassifyExplicitFindingAutomatically() {
        StrategicClassificationEngine engine =
                new DefaultStrategicClassificationEngine(
                        List.of(
                                new EvidenceClassificationStrategicRule(),
                                new StatementSemanticStrategicRule(),
                                new FutureStateStrategicRule(),
                                new TransformationPrincipleStrategicRule()
                        ),
                        confidenceEvaluator
                );

        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "Actualmente existe una brecha entre la marca y su plataforma digital.",
                        null,
                        null,
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL
                );

        StrategicClassificationResult result =
                engine.classify(candidate);

        assertThat(result.proposedType())
                .isEqualTo(
                        StrategicArtifactType.FINDING
                );

        assertThat(result.confidence())
                .isEqualTo(
                        StrategicConfidence.EXPLICIT
                );

        assertThat(result.decision())
                .isEqualTo(
                        StrategicClassificationDecision.AUTO_ACCEPT
                );

        assertThat(result.eligibleForAutomaticDerivation())
                .isTrue();
    }

    @Test
    void shouldRequireReviewWhenSignalsCompete() {
        StrategicClassificationEngine engine =
                new DefaultStrategicClassificationEngine(
                        List.of(
                                new StatementSemanticStrategicRule(),
                                new FutureStateStrategicRule()
                        ),
                        confidenceEvaluator
                );

        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "Existe una oportunidad para mejorar y reducir una brecha.",
                        null,
                        null,
                        null,
                        EvidenceConfidence.INFERRED,
                        EvidenceExtractionOrigin.AI_ASSISTED
                );

        StrategicClassificationResult result =
                engine.classify(candidate);

        assertThat(result.requiresHumanReview())
                .isTrue();

        assertThat(result.eligibleForAutomaticDerivation())
                .isFalse();
    }

    @Test
    void shouldRejectCandidateWithoutSignals() {
        StrategicClassificationEngine engine =
                new DefaultStrategicClassificationEngine(
                        List.of(
                                new StatementSemanticStrategicRule()
                        ),
                        confidenceEvaluator
                );

        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "Texto completamente neutral.",
                        null,
                        null,
                        null,
                        null,
                        null
                );

        StrategicClassificationResult result =
                engine.classify(candidate);

        assertThat(result.decision())
                .isEqualTo(
                        StrategicClassificationDecision.REJECTED
                );

        assertThat(result.proposedType())
                .isNull();
    }
}