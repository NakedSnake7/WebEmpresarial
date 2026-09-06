package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicSynthesisEvidenceSummary {

    private final StrategicEvidenceCoverageStatus coverageStatus;

    private final int coveragePercentage;

    private final List<String> evidenceCodes;

    private final int maximumTraceDepth;

    private StrategicSynthesisEvidenceSummary(
            StrategicEvidenceCoverageStatus coverageStatus,
            int coveragePercentage,
            List<String> evidenceCodes,
            int maximumTraceDepth
    ) {
        this.coverageStatus =
                Objects.requireNonNull(
                        coverageStatus,
                        "El estado de cobertura es obligatorio"
                );

        if (coveragePercentage < 0
                || coveragePercentage > 100) {
            throw new IllegalArgumentException(
                    "El porcentaje de cobertura debe estar entre 0 y 100"
            );
        }

        this.coveragePercentage =
                coveragePercentage;

        this.evidenceCodes =
                evidenceCodes == null
                        ? List.of()
                        : List.copyOf(evidenceCodes);

        if (maximumTraceDepth < 0) {
            throw new IllegalArgumentException(
                    "La profundidad máxima no puede ser negativa"
            );
        }

        this.maximumTraceDepth =
                maximumTraceDepth;

        validateState();
    }

    public static StrategicSynthesisEvidenceSummary of(
            StrategicEvidenceCoverageStatus coverageStatus,
            int coveragePercentage,
            List<String> evidenceCodes,
            int maximumTraceDepth
    ) {
        return new StrategicSynthesisEvidenceSummary(
                coverageStatus,
                coveragePercentage,
                evidenceCodes,
                maximumTraceDepth
        );
    }
    
    private void validateState() {

        if (coverageStatus
                == StrategicEvidenceCoverageStatus.UNSUPPORTED) {

            if (!evidenceCodes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Una síntesis sin soporte no puede contener evidencias"
                );
            }

            if (coveragePercentage != 0) {
                throw new IllegalArgumentException(
                        "Una síntesis sin soporte debe tener cobertura 0"
                );
            }

            return;
        }

        if (evidenceCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Una síntesis respaldada debe contener evidencia"
            );
        }

        if (coveragePercentage <= 0) {
            throw new IllegalArgumentException(
                    "Una síntesis respaldada debe tener cobertura positiva"
            );
        }
    }

    public boolean hasEvidence() {
        return !evidenceCodes.isEmpty();
    }

    public StrategicEvidenceCoverageStatus getCoverageStatus() {
        return coverageStatus;
    }

    public int getCoveragePercentage() {
        return coveragePercentage;
    }

    public List<String> getEvidenceCodes() {
        return evidenceCodes;
    }

    public int getMaximumTraceDepth() {
        return maximumTraceDepth;
    }
    
    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof StrategicSynthesisEvidenceSummary other)) {
            return false;
        }

        return coveragePercentage
                == other.coveragePercentage
                && maximumTraceDepth
                == other.maximumTraceDepth
                && coverageStatus
                == other.coverageStatus
                && Objects.equals(
                        evidenceCodes,
                        other.evidenceCodes
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                coverageStatus,
                coveragePercentage,
                evidenceCodes,
                maximumTraceDepth
        );
    }	
}
