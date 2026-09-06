package com.webempresarial.store.digitaltransformation.infrastructure.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.infrastructure.traceability.synthesis.persistence.DefaultTraceabilityCodeGenerator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultTraceabilityCodeGeneratorTest {

    private final DefaultTraceabilityCodeGenerator generator =
            new DefaultTraceabilityCodeGenerator();

    @Test
    void shouldGenerateEvidenceNodeCodeWithoutDuplicatingPrefix() {
        String result =
                generator.generateForExternalReference(
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        "EVD-AUDIT-001"
                );

        assertThat(result)
                .isEqualTo("NODE-EVD-AUDIT-001");
    }

    @Test
    void shouldNormalizeExternalReference() {
        String result =
                generator.generateForExternalReference(
                        TraceabilityNodeType.BUSINESS_OBJECTIVE,
                        "Objetivo crecimiento internacional"
                );

        assertThat(result)
                .isEqualTo(
                        "NODE-OBJ-OBJETIVO-CRECIMIENTO-INTERNACIONAL"
                );
    }

    @Test
    void shouldRejectBlankReference() {
        assertThatThrownBy(() ->
                generator.generateForExternalReference(
                        TraceabilityNodeType.OTHER,
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("referencia");
    }
}