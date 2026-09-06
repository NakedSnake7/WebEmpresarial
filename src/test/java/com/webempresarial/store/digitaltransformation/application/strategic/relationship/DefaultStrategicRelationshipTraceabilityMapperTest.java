package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityRelationType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicRelationshipTraceabilityMapperTest {

    private final DefaultStrategicRelationshipTraceabilityMapper mapper =
            new DefaultStrategicRelationshipTraceabilityMapper();

    @Test
    void shouldNormalizeAllStrategicRelationshipsAsDerivedFrom() {
        for (StrategicRelationshipType type
                : StrategicRelationshipType.values()) {

            StrategicRelationshipTraceabilityMapping mapping =
                    mapper.map(type);

            assertThat(mapping.reverseDirection())
                    .isTrue();

            assertThat(mapping.relationType())
                    .isEqualTo(
                            TraceabilityRelationType.DERIVED_FROM
                    );

            assertThat(mapping.strength())
                    .isEqualTo(
                            TraceabilityStrength.STRONG
                    );
        }
    }
}