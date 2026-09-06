package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DefaultStrategicInterpretationGuardrailValidator
        implements StrategicInterpretationGuardrailValidator {

    @Override
    public StrategicInterpretationValidationResult validate(
            StrategicInterpretationRequest request,
            StrategicInterpretationResult result
    ) {
        Objects.requireNonNull(
                request,
                "StrategicInterpretationRequest es obligatorio"
        );

        Objects.requireNonNull(
                result,
                "StrategicInterpretationResult es obligatorio"
        );

        List<StrategicInterpretationViolation> violations =
                new ArrayList<>();

        if (result.getInterpretedThesis() == null
                || result.getInterpretedThesis().isBlank()) {
            violations.add(
                    StrategicInterpretationViolation.EMPTY_INTERPRETATION
            );
        }

        boolean unknownArtifact =
                result.getReferencedArtifactCodes()
                        .stream()
                        .anyMatch(code ->
                                !request.getSourceArtifactCodes()
                                        .contains(code)
                        );

        if (unknownArtifact) {
            violations.add(
                    StrategicInterpretationViolation.UNKNOWN_SOURCE_ARTIFACT
            );
        }

        if (request.getConstraints().contains(
                StrategicInterpretationConstraint.REQUIRE_SOURCE_ALIGNMENT
        )
                && result.getReferencedArtifactCodes().isEmpty()) {

            violations.add(
                    StrategicInterpretationViolation.SOURCE_ALIGNMENT_MISSING
            );
        }

        if (violations.isEmpty()) {
            return StrategicInterpretationValidationResult.valid();
        }

        /*
         * Unknown sources invalidan automáticamente.
         * Ausencia de referencias requiere revisión,
         * pero no necesariamente invalida la interpretación.
         */
        if (violations.contains(
                StrategicInterpretationViolation.UNKNOWN_SOURCE_ARTIFACT
        )) {
            return StrategicInterpretationValidationResult.of(
                    StrategicInterpretationValidationStatus.INVALID,
                    violations
            );
        }

        return StrategicInterpretationValidationResult.of(
                StrategicInterpretationValidationStatus.REQUIRES_REVIEW,
                violations
        );
    }
}