package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

public record EvidenceTraceabilityDrift(
        boolean detected,
        boolean titleChanged,
        boolean classificationChanged,
        String reason
) {

    public static EvidenceTraceabilityDrift none() {
        return new EvidenceTraceabilityDrift(
                false,
                false,
                false,
                "No se detectaron diferencias"
        );
    }
}