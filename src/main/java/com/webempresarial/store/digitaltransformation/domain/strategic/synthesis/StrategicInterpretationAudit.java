package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicInterpretationAudit {

    private final StrategicInterpretationMode mode;

    private final List<StrategicInterpretationConstraint> constraints;

    private final List<String> sourceArtifactCodes;

    private final List<String> referencedArtifactCodes;

    private final StrategicInterpretationValidationStatus
            validationStatus;

    private final List<StrategicInterpretationViolation>
            violations;

    private StrategicInterpretationAudit(
            StrategicInterpretationMode mode,
            List<StrategicInterpretationConstraint> constraints,
            List<String> sourceArtifactCodes,
            List<String> referencedArtifactCodes,
            StrategicInterpretationValidationStatus validationStatus,
            List<StrategicInterpretationViolation> violations
    ) {
        this.mode =
                Objects.requireNonNull(
                        mode,
                        "El modo de interpretación es obligatorio"
                );

        this.constraints =
                immutableNonNull(
                        constraints
                );

        this.sourceArtifactCodes =
                immutableStrings(
                        sourceArtifactCodes
                );

        this.referencedArtifactCodes =
                immutableStrings(
                        referencedArtifactCodes
                );

        this.validationStatus =
                Objects.requireNonNull(
                        validationStatus,
                        "El estado de validación es obligatorio"
                );

        this.violations =
                immutableNonNull(
                        violations
                );
    }

    public static StrategicInterpretationAudit from(
            StrategicInterpretationRequest request,
            StrategicInterpretationResult result,
            StrategicInterpretationValidationResult validation
    ) {
        Objects.requireNonNull(
                request,
                "StrategicInterpretationRequest es obligatorio"
        );

        Objects.requireNonNull(
                result,
                "StrategicInterpretationResult es obligatorio"
        );

        Objects.requireNonNull(
                validation,
                "StrategicInterpretationValidationResult es obligatorio"
        );

        return new StrategicInterpretationAudit(
                request.getMode(),
                request.getConstraints(),
                request.getSourceArtifactCodes(),
                result.getReferencedArtifactCodes(),
                validation.getStatus(),
                validation.getViolations()
        );
    }

    private static <T> List<T> immutableNonNull(
            List<T> values
    ) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private static List<String> immutableStrings(
            List<String> values
    ) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    public StrategicInterpretationMode getMode() {
        return mode;
    }

    public List<StrategicInterpretationConstraint>
    getConstraints() {
        return constraints;
    }

    public List<String> getSourceArtifactCodes() {
        return sourceArtifactCodes;
    }

    public List<String> getReferencedArtifactCodes() {
        return referencedArtifactCodes;
    }

    public StrategicInterpretationValidationStatus
    getValidationStatus() {
        return validationStatus;
    }

    public List<StrategicInterpretationViolation>
    getViolations() {
        return violations;
    }
}