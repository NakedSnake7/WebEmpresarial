package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.Objects;

public record StrategicChainGap(
        StrategicChainGapType type,
        String description
) {

    public StrategicChainGap {
        Objects.requireNonNull(
                type,
                "El tipo de gap es obligatorio"
        );

        if (description == null
                || description.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción del gap es obligatoria"
            );
        }

        description = description.trim();
    }
}