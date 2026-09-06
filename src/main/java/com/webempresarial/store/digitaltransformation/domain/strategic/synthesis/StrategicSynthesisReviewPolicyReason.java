package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.Objects;

public record StrategicSynthesisReviewPolicyReason(
        StrategicSynthesisReviewPolicyReasonCode code,
        String message
) {

    public StrategicSynthesisReviewPolicyReason {
        Objects.requireNonNull(
                code,
                "El código de razón es obligatorio"
        );

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje de la razón es obligatorio"
            );
        }

        message = message.trim();
    }
}