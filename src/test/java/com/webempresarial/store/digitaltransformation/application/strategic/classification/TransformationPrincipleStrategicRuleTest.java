package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TransformationPrincipleStrategicRuleTest {

    private final TransformationPrincipleStrategicRule rule =
            new TransformationPrincipleStrategicRule();

    @Test
    void shouldRecognizeTransversalPrinciple() {
        StrategicClassificationCandidate candidate =
                new StrategicClassificationCandidate(
                        "La filosofía debe estar en el centro de la experiencia.",
                        null,
                        null,
                        null,
                        null,
                        null
                );

        List<StrategicRuleMatch> matches =
                rule.evaluate(candidate);

        assertThat(matches).hasSize(1);

        assertThat(matches.getFirst().suggestedType())
                .isEqualTo(
                        StrategicArtifactType.TRANSFORMATION_PRINCIPLE
                );

        assertThat(matches.getFirst().strength())
                .isEqualTo(
                        StrategicRuleStrength.DECISIVE
                );
    }
}