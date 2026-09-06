package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

public record EvidenceRegistrationDecision(
        boolean register,
        String reason
) {

    public EvidenceRegistrationDecision {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "La razón de la decisión es obligatoria"
            );
        }
    }

    public static EvidenceRegistrationDecision approved(
            String reason
    ) {
        return new EvidenceRegistrationDecision(
                true,
                reason
        );
    }

    public static EvidenceRegistrationDecision rejected(
            String reason
    ) {
        return new EvidenceRegistrationDecision(
                false,
                reason
        );
    }
}