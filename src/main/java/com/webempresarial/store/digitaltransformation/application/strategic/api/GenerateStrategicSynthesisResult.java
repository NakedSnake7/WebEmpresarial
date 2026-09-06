package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisGateResult;

import java.util.Objects;

public record GenerateStrategicSynthesisResult(

        StoredStrategicSynthesis synthesis,

        StrategicSynthesisGateResult gateResult

) {

    public GenerateStrategicSynthesisResult {

        synthesis =
                Objects.requireNonNull(
                        synthesis,
                        "La síntesis persistida es obligatoria"
                );

        gateResult =
                Objects.requireNonNull(
                        gateResult,
                        "El resultado del synthesis gate es obligatorio"
                );
    }
}