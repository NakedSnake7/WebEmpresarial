package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import java.util.Objects;

public record StrategicDerivationDecision(
        StrategicDerivationAction action,
        String reason
) {

    public StrategicDerivationDecision {
        Objects.requireNonNull(
                action,
                "La acción de derivación es obligatoria"
        );

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "La razón de derivación es obligatoria"
            );
        }

        reason = reason.trim();
    }

    public static StrategicDerivationDecision derive(
            String reason
    ) {
        return new StrategicDerivationDecision(
                StrategicDerivationAction.DERIVE,
                reason
        );
    }

    public static StrategicDerivationDecision review(
            String reason
    ) {
        return new StrategicDerivationDecision(
                StrategicDerivationAction.REVIEW_REQUIRED,
                reason
        );
    }

    public static StrategicDerivationDecision reject(
            String reason
    ) {
        return new StrategicDerivationDecision(
                StrategicDerivationAction.REJECT,
                reason
        );
    }

    public boolean canDerive() {
        return action == StrategicDerivationAction.DERIVE;
    }
}