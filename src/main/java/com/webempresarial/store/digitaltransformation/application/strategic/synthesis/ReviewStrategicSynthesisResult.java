package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import java.util.Objects;

public record ReviewStrategicSynthesisResult(
        StrategicSynthesis synthesis,
        StrategicSynthesisReview review,
        StrategicSynthesisReviewPolicyResult policyResult,
        StrategicSynthesisStatus previousStatus,
        StrategicSynthesisStatus resultingStatus
) {

    public ReviewStrategicSynthesisResult {
        Objects.requireNonNull(
                synthesis,
                "La síntesis resultante es obligatoria"
        );

        Objects.requireNonNull(
                review,
                "La revisión es obligatoria"
        );

        Objects.requireNonNull(
                policyResult,
                "El resultado de policy es obligatorio"
        );

        Objects.requireNonNull(
                previousStatus,
                "El estado previo es obligatorio"
        );

        Objects.requireNonNull(
                resultingStatus,
                "El estado resultante es obligatorio"
        );
    }
}