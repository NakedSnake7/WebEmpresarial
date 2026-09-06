package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicRelationshipCompatibilityTest {

    @Test
    void findingShouldRevealBusinessProblem() {
        assertThat(
                StrategicRelationshipCompatibility.supports(
                        StrategicArtifactType.FINDING,
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        StrategicRelationshipType.REVEALS
                )
        ).isTrue();
    }

    @Test
    void businessProblemShouldBeAddressedByObjective() {
        assertThat(
                StrategicRelationshipCompatibility.supports(
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicRelationshipType.ADDRESSED_BY
                )
        ).isTrue();
    }

    @Test
    void businessObjectiveShouldEnableOpportunity() {
        assertThat(
                StrategicRelationshipCompatibility.supports(
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        StrategicRelationshipType.ENABLES
                )
        ).isTrue();
    }

    @Test
    void shouldRejectSemanticallyInvalidRelationship() {
        assertThat(
                StrategicRelationshipCompatibility.supports(
                        StrategicArtifactType.FINDING,
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        StrategicRelationshipType.ENABLES
                )
        ).isFalse();
    }

    @Test
    void ensureSupportedShouldFailForInvalidDirection() {
        assertThatThrownBy(() ->
                StrategicRelationshipCompatibility
                        .ensureSupported(
                                StrategicArtifactType.BUSINESS_OBJECTIVE,
                                StrategicArtifactType.BUSINESS_PROBLEM,
                                StrategicRelationshipType.ADDRESSED_BY
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no es válida"
                );
    }
}