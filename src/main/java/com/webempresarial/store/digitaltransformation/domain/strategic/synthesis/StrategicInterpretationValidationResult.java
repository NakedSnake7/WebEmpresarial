package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicInterpretationValidationResult {

    private final StrategicInterpretationValidationStatus status;

    private final List<StrategicInterpretationViolation> violations;

    private StrategicInterpretationValidationResult(
            StrategicInterpretationValidationStatus status,
            List<StrategicInterpretationViolation> violations
    ) {
        this.status =
                Objects.requireNonNull(
                        status,
                        "El estado de validación es obligatorio"
                );

        this.violations =
                violations == null
                        ? List.of()
                        : List.copyOf(violations);
    }

    public static StrategicInterpretationValidationResult valid() {
        return new StrategicInterpretationValidationResult(
                StrategicInterpretationValidationStatus.VALID,
                List.of()
        );
    }

    public static StrategicInterpretationValidationResult of(
            StrategicInterpretationValidationStatus status,
            List<StrategicInterpretationViolation> violations
    ) {
        return new StrategicInterpretationValidationResult(
                status,
                violations
        );
    }

    public boolean isValid() {
        return status
                == StrategicInterpretationValidationStatus.VALID;
    }

    public StrategicInterpretationValidationStatus getStatus() {
        return status;
    }

    public List<StrategicInterpretationViolation> getViolations() {
        return violations;
    }
}