package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import java.util.Objects;

public record StrategicInterpretationOutcome(
        StrategicInterpretationRequest request,
        StrategicInterpretationResult interpretation,
        StrategicInterpretationValidationResult validation,
        StrategicInterpretationAudit audit,
        StrategicSynthesis synthesis
) {

    public StrategicInterpretationOutcome {
        Objects.requireNonNull(
                request,
                "StrategicInterpretationRequest es obligatorio"
        );

        Objects.requireNonNull(
                interpretation,
                "StrategicInterpretationResult es obligatorio"
        );

        Objects.requireNonNull(
                validation,
                "StrategicInterpretationValidationResult es obligatorio"
        );

        Objects.requireNonNull(
                audit,
                "StrategicInterpretationAudit es obligatorio"
        );

        Objects.requireNonNull(
                synthesis,
                "StrategicSynthesis es obligatoria"
        );

        if (!validation.isValid()) {
            throw new IllegalArgumentException(
                    "Solo una interpretación válida puede producir síntesis"
            );
        }

        if (audit.getValidationStatus()
                != validation.getStatus()) {
            throw new IllegalArgumentException(
                    "El audit no corresponde con la validación"
            );
        }

        if (synthesis.getOrigin()
                != StrategicSynthesisOrigin.AI_ASSISTED) {
            throw new IllegalArgumentException(
                    "La síntesis resultante debe ser AI_ASSISTED"
            );
        }

        if (synthesis.getStatus()
                != StrategicSynthesisStatus.REQUIRES_REVIEW) {
            throw new IllegalArgumentException(
                    "Una síntesis AI_ASSISTED debe requerir revisión"
            );
        }
    }
}