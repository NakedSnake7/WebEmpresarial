package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationOutcome;
import java.util.Objects;

public record RequestStrategicInterpretationResult(

        StoredStrategicSynthesis deterministicSynthesis,

        StoredStrategicSynthesis aiSynthesis,

        StrategicInterpretationOutcome outcome

) {

    public RequestStrategicInterpretationResult {

        deterministicSynthesis =
                Objects.requireNonNull(
                        deterministicSynthesis,
                        "La síntesis determinista persistida es obligatoria"
                );

        aiSynthesis =
                Objects.requireNonNull(
                        aiSynthesis,
                        "La síntesis AI persistida es obligatoria"
                );

        outcome =
                Objects.requireNonNull(
                        outcome,
                        "El resultado de interpretación es obligatorio"
                );
    }
}