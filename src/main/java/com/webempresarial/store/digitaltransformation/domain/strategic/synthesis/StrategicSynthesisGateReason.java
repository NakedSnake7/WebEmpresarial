package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.Objects;

public record StrategicSynthesisGateReason(
        StrategicSynthesisGateReasonCode code,
        StrategicSynthesisGateSeverity severity,
        String message
) {

    public StrategicSynthesisGateReason {
        Objects.requireNonNull(
                code,
                "El código de razón es obligatorio"
        );

        Objects.requireNonNull(
                severity,
                "La severidad es obligatoria"
        );

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje de la razón es obligatorio"
            );
        }

        message = message.trim();
    }

    public boolean isBlocking() {
        return severity
                == StrategicSynthesisGateSeverity.BLOCKING;
    }

    public boolean isWarning() {
        return severity
                == StrategicSynthesisGateSeverity.WARNING;
    }
}