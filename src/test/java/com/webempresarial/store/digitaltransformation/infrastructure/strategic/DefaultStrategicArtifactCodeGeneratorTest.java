package com.webempresarial.store.digitaltransformation.infrastructure.strategic;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactTypeDescriptor;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicArtifactCodeGeneratorTest {

    private final DefaultStrategicArtifactCodeGenerator generator =
            new DefaultStrategicArtifactCodeGenerator();

    @Test
    void shouldGenerateFindingCode() {
        assertThat(
                generator.generate(
                        StrategicArtifactTypeDescriptor.of(
                                StrategicArtifactType.FINDING
                        ),
                        1
                )
        ).isEqualTo(
                "FND-001"
        );
    }

    @Test
    void shouldGenerateObjectiveCode() {
        assertThat(
                generator.generate(
                        StrategicArtifactTypeDescriptor.of(
                                StrategicArtifactType.BUSINESS_OBJECTIVE
                        ),
                        12
                )
        ).isEqualTo(
                "OBJ-012"
        );
    }

    @Test
    void shouldRejectInvalidSequence() {
        assertThatThrownBy(() ->
                generator.generate(
                        StrategicArtifactTypeDescriptor.of(
                                StrategicArtifactType.FINDING
                        ),
                        0
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "secuencia"
                );
    }
}