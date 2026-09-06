package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisStatus;

import java.util.Objects;

public record SubmitStrategicSynthesisResult(
        StrategicSynthesis synthesis,
        StrategicSynthesisStatus previousStatus,
        StrategicSynthesisStatus resultingStatus
) {

    public SubmitStrategicSynthesisResult {
        Objects.requireNonNull(
                synthesis,
                "La síntesis es obligatoria"
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