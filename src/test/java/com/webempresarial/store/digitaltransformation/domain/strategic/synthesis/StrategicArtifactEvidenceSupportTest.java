package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicArtifactEvidenceSupportTest {

    @Test
    void shouldCreateDirectEvidenceSupport() {
        StrategicArtifact artifact =
                finding();

        StrategicArtifactEvidenceSupport support =
                StrategicArtifactEvidenceSupport.direct(
                        artifact,
                        List.of("EVD-AUDIT-001"),
                        TraceabilityStrength.DIRECT,
                        "El hallazgo deriva directamente de evidencia documental."
                );

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.DIRECT
                );

        assertThat(support.isDirect())
                .isTrue();

        assertThat(support.hasEvidence())
                .isTrue();

        assertThat(support.getTraceDepth())
                .isEqualTo(1);
    }

    @Test
    void shouldCreateInheritedSupport() {
        StrategicArtifact artifact =
                finding();

        StrategicArtifactEvidenceSupport support =
                StrategicArtifactEvidenceSupport.inherited(
                        artifact,
                        List.of("EVD-AUDIT-001"),
                        TraceabilityStrength.STRONG,
                        3,
                        "El artefacto hereda soporte mediante la cadena estratégica."
                );

        assertThat(support.getCoverageLevel())
                .isEqualTo(
                        EvidenceCoverageLevel.INHERITED
                );

        assertThat(support.isInherited())
                .isTrue();

        assertThat(support.getTraceDepth())
                .isEqualTo(3);
    }

    @Test
    void shouldRejectInheritedSupportWithDepthOne() {
        StrategicArtifact artifact =
                finding();

        assertThatThrownBy(() ->
                StrategicArtifactEvidenceSupport.inherited(
                        artifact,
                        List.of("EVD-001"),
                        TraceabilityStrength.STRONG,
                        1,
                        "Invalid inherited support"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "profundidad"
                );
    }

    @Test
    void unsupportedArtifactShouldNotContainEvidence() {
        StrategicArtifact artifact =
                finding();

        StrategicArtifactEvidenceSupport support =
                StrategicArtifactEvidenceSupport.none(
                        artifact,
                        "No existe una ruta hasta evidencia fuente."
                );

        assertThat(support.hasEvidence())
                .isFalse();

        assertThat(support.getEvidenceCodes())
                .isEmpty();

        assertThat(support.getTraceDepth())
                .isZero();

        assertThat(support.getWeakestTraceStrength())
                .isNull();
    }

    @Test
    void evidenceCodesShouldBeImmutable() {
        StrategicArtifactEvidenceSupport support =
                StrategicArtifactEvidenceSupport.direct(
                        finding(),
                        List.of("EVD-001"),
                        TraceabilityStrength.DIRECT,
                        "Direct support"
                );

        assertThatThrownBy(() ->
                support.getEvidenceCodes()
                        .add("EVD-002")
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    private static StrategicArtifact finding() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicArtifact.deriveFromEvidence(
                evidence.getProject(),
                evidence,
                "FND-001",
                StrategicArtifactType.FINDING,
                StrategicConfidence.EXPLICIT,
                StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                "Finding",
                null,
                null
        );
    }
}