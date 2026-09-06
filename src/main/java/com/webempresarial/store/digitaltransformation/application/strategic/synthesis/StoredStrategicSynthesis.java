package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;

import java.time.Instant;
import java.util.Objects;

public record StoredStrategicSynthesis(
        Long id,
        StrategicSynthesis synthesis,
        Instant createdAt
) {

    public StoredStrategicSynthesis {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El id persistente de la síntesis debe ser válido"
            );
        }

        synthesis =
                Objects.requireNonNull(
                        synthesis,
                        "La síntesis es obligatoria"
                );

        createdAt =
                Objects.requireNonNull(
                        createdAt,
                        "La fecha de creación es obligatoria"
                );
    }
}