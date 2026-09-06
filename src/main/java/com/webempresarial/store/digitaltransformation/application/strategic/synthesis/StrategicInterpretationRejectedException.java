package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationValidationResult;

import java.util.Objects;

public class StrategicInterpretationRejectedException
        extends RuntimeException {

    private final StrategicInterpretationValidationResult validation;

    public StrategicInterpretationRejectedException(
            StrategicInterpretationValidationResult validation
    ) {
        super(buildMessage(validation));

        this.validation =
                Objects.requireNonNull(
                        validation,
                        "El resultado de validación es obligatorio"
                );
    }

    private static String buildMessage(
            StrategicInterpretationValidationResult validation
    ) {
        Objects.requireNonNull(
                validation,
                "El resultado de validación es obligatorio"
        );

        return "La interpretación estratégica fue rechazada por guardrails: "
                + validation.getStatus()
                + " "
                + validation.getViolations();
    }

    public StrategicInterpretationValidationResult
    getValidation() {
        return validation;
    }
}