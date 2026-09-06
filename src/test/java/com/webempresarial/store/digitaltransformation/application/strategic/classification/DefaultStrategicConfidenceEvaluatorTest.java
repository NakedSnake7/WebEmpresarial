package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicConfidenceEvaluatorTest {

    private final DefaultStrategicConfidenceEvaluator evaluator =
            new DefaultStrategicConfidenceEvaluator();

    @Test
    void shouldReturnExplicitForDecisiveExplicitSource() {
        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "Hallazgo",
                        null,
                        null,
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL
                );

        StrategicRuleMatch match =
                new StrategicRuleMatch(
                        "SCR-001",
                        StrategicClassificationRuleType.SOURCE_CLASSIFICATION,
                        StrategicArtifactType.FINDING,
                        StrategicRuleStrength.DECISIVE,
                        true,
                        "Clasificación explícita"
                );

        assertThat(
                evaluator.evaluate(
                        candidate,
                        5,
                        0,
                        List.of(match)
                )
        ).isEqualTo(
                StrategicConfidence.EXPLICIT
        );
    }

    @Test
    void shouldReturnStronglySupportedForLargeMargin() {
        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "Objetivo",
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertThat(
                evaluator.evaluate(
                        candidate,
                        8,
                        3,
                        List.of()
                )
        ).isEqualTo(
                StrategicConfidence.STRONGLY_SUPPORTED
        );
    }

    @Test
    void shouldReturnUncertainForWeakScore() {
        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "Neutral",
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertThat(
                evaluator.evaluate(
                        candidate,
                        1,
                        0,
                        List.of()
                )
        ).isEqualTo(
                StrategicConfidence.UNCERTAIN
        );
    }
}