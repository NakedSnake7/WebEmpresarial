package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicTraceabilityTypeMapperTest {

    private final StrategicTraceabilityTypeMapper mapper =
            new StrategicTraceabilityTypeMapper();

    @Test
    void shouldMapAllStrategicTypes() {
        assertThat(
                mapper.map(
                        StrategicArtifactType.FINDING
                )
        ).isEqualTo(
                TraceabilityNodeType.STRATEGIC_FINDING
        );

        assertThat(
                mapper.map(
                        StrategicArtifactType.BUSINESS_PROBLEM
                )
        ).isEqualTo(
                TraceabilityNodeType.BUSINESS_PROBLEM
        );

        assertThat(
                mapper.map(
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                )
        ).isEqualTo(
                TraceabilityNodeType.BUSINESS_OBJECTIVE
        );

        assertThat(
                mapper.map(
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                )
        ).isEqualTo(
                TraceabilityNodeType.STRATEGIC_OPPORTUNITY
        );

        assertThat(
                mapper.map(
                        StrategicArtifactType.EXISTING_STRENGTH
                )
        ).isEqualTo(
                TraceabilityNodeType.EXISTING_STRENGTH
        );

        assertThat(
                mapper.map(
                        StrategicArtifactType.TRANSFORMATION_PRINCIPLE
                )
        ).isEqualTo(
                TraceabilityNodeType.TRANSFORMATION_PRINCIPLE
        );
    }
}