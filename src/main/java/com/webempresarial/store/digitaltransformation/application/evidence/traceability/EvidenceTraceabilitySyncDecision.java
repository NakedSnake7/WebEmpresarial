package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

public record EvidenceTraceabilitySyncDecision(
        EvidenceTraceabilitySyncAction action,
        boolean changeRequired,
        String reason
) {

    public EvidenceTraceabilitySyncDecision {
        if (action == null) {
            throw new IllegalArgumentException(
                    "La acción es obligatoria"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "La razón es obligatoria"
            );
        }
    }

    public static EvidenceTraceabilitySyncDecision noChange(
            String reason
    ) {
        return new EvidenceTraceabilitySyncDecision(
                EvidenceTraceabilitySyncAction.NO_CHANGE,
                false,
                reason
        );
    }

    public static EvidenceTraceabilitySyncDecision change(
            EvidenceTraceabilitySyncAction action,
            String reason
    ) {
        return new EvidenceTraceabilitySyncDecision(
                action,
                true,
                reason
        );
    }
}