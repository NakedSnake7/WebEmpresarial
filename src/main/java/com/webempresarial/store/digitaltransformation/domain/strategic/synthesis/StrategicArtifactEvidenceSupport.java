package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;

import java.util.List;
import java.util.Objects;

public final class StrategicArtifactEvidenceSupport {

    private final StrategicArtifact artifact;

    private final EvidenceCoverageLevel coverageLevel;

    private final StrategicEvidenceSupportType supportType;

    private final List<String> evidenceCodes;

    private final TraceabilityStrength weakestTraceStrength;

    private final int traceDepth;

    private final String rationale;

    private StrategicArtifactEvidenceSupport(
            StrategicArtifact artifact,
            EvidenceCoverageLevel coverageLevel,
            StrategicEvidenceSupportType supportType,
            List<String> evidenceCodes,
            TraceabilityStrength weakestTraceStrength,
            int traceDepth,
            String rationale
    ) {
        this.artifact =
                Objects.requireNonNull(
                        artifact,
                        "El artefacto estratégico es obligatorio"
                );

        this.coverageLevel =
                Objects.requireNonNull(
                        coverageLevel,
                        "El nivel de cobertura es obligatorio"
                );

        this.supportType =
                Objects.requireNonNull(
                        supportType,
                        "El tipo de soporte es obligatorio"
                );

        this.evidenceCodes =
                evidenceCodes == null
                        ? List.of()
                        : List.copyOf(evidenceCodes);

        if (traceDepth < 0) {
            throw new IllegalArgumentException(
                    "La profundidad de trazabilidad no puede ser negativa"
            );
        }

        this.traceDepth =
                traceDepth;

        this.weakestTraceStrength =
                weakestTraceStrength;

        if (rationale == null
                || rationale.isBlank()) {
            throw new IllegalArgumentException(
                    "La justificación del soporte es obligatoria"
            );
        }

        this.rationale =
                rationale.trim();

        validateState();
    }

    public static StrategicArtifactEvidenceSupport direct(
            StrategicArtifact artifact,
            List<String> evidenceCodes,
            TraceabilityStrength strength,
            String rationale
    ) {
        return new StrategicArtifactEvidenceSupport(
                artifact,
                EvidenceCoverageLevel.DIRECT,
                StrategicEvidenceSupportType.SOURCE_EVIDENCE,
                evidenceCodes,
                Objects.requireNonNull(
                        strength,
                        "La fuerza de trazabilidad es obligatoria"
                ),
                1,
                rationale
        );
    }

    public static StrategicArtifactEvidenceSupport inherited(
            StrategicArtifact artifact,
            List<String> evidenceCodes,
            TraceabilityStrength weakestTraceStrength,
            int traceDepth,
            String rationale
    ) {
        if (traceDepth < 2) {
            throw new IllegalArgumentException(
                    "El soporte heredado debe tener profundidad mínima 2"
            );
        }

        return new StrategicArtifactEvidenceSupport(
                artifact,
                EvidenceCoverageLevel.INHERITED,
                StrategicEvidenceSupportType.TRACEABILITY_CHAIN,
                evidenceCodes,
                Objects.requireNonNull(
                        weakestTraceStrength,
                        "La fuerza mínima de trazabilidad es obligatoria"
                ),
                traceDepth,
                rationale
        );
    }

    public static StrategicArtifactEvidenceSupport weak(
            StrategicArtifact artifact,
            List<String> evidenceCodes,
            TraceabilityStrength weakestTraceStrength,
            int traceDepth,
            String rationale
    ) {
        return new StrategicArtifactEvidenceSupport(
                artifact,
                EvidenceCoverageLevel.WEAK,
                StrategicEvidenceSupportType.TRACEABILITY_CHAIN,
                evidenceCodes,
                weakestTraceStrength,
                traceDepth,
                rationale
        );
    }

    public static StrategicArtifactEvidenceSupport none(
            StrategicArtifact artifact,
            String rationale
    ) {
        return new StrategicArtifactEvidenceSupport(
                artifact,
                EvidenceCoverageLevel.NONE,
                StrategicEvidenceSupportType.NONE,
                List.of(),
                null,
                0,
                rationale
        );
    }

    private void validateState() {
        if (coverageLevel == EvidenceCoverageLevel.NONE) {
            if (!evidenceCodes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Un artefacto sin cobertura no puede contener evidencias"
                );
            }

            if (weakestTraceStrength != null) {
                throw new IllegalArgumentException(
                        "Un artefacto sin cobertura no puede tener fuerza de trazabilidad"
                );
            }

            if (traceDepth != 0) {
                throw new IllegalArgumentException(
                        "Un artefacto sin cobertura debe tener profundidad 0"
                );
            }

            return;
        }

        if (evidenceCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El soporte documental debe contener al menos una evidencia"
            );
        }

        if (weakestTraceStrength == null) {
            throw new IllegalArgumentException(
                    "El soporte documental debe indicar la fuerza mínima de trazabilidad"
            );
        }

        if (traceDepth <= 0) {
            throw new IllegalArgumentException(
                    "El soporte documental debe tener profundidad positiva"
            );
        }
    }

    public boolean hasEvidence() {
        return coverageLevel != EvidenceCoverageLevel.NONE;
    }

    public boolean isDirect() {
        return coverageLevel == EvidenceCoverageLevel.DIRECT;
    }

    public boolean isInherited() {
        return coverageLevel == EvidenceCoverageLevel.INHERITED;
    }

    public boolean isWeak() {
        return coverageLevel == EvidenceCoverageLevel.WEAK;
    }

    public StrategicArtifact getArtifact() {
        return artifact;
    }

    public EvidenceCoverageLevel getCoverageLevel() {
        return coverageLevel;
    }

    public StrategicEvidenceSupportType getSupportType() {
        return supportType;
    }

    public List<String> getEvidenceCodes() {
        return evidenceCodes;
    }

    public TraceabilityStrength getWeakestTraceStrength() {
        return weakestTraceStrength;
    }

    public int getTraceDepth() {
        return traceDepth;
    }

    public String getRationale() {
        return rationale;
    }
}