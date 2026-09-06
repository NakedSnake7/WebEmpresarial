package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicTraversalAmbiguityTest {

    @Test
    void shouldCreateAmbiguityWithMultipleCandidates() {
        StrategicTraversalAmbiguity ambiguity =
                new StrategicTraversalAmbiguity(
                        StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS,
                        "FND-001",
                        List.of(
                                "PRB-001",
                                "PRB-002"
                        ),
                        "Existen dos problemas candidatos"
                );

        assertThat(ambiguity.candidateArtifactCodes())
                .containsExactly(
                        "PRB-001",
                        "PRB-002"
                );
    }

    @Test
    void shouldRejectSingleCandidateAmbiguity() {
        assertThatThrownBy(() ->
                new StrategicTraversalAmbiguity(
                        StrategicTraversalAmbiguityType.MULTIPLE_BUSINESS_PROBLEMS,
                        "FND-001",
                        List.of(
                                "PRB-001"
                        ),
                        "Solo uno"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "al menos dos"
                );
    }
}