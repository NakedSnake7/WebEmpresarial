package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

import java.util.Objects;

public record StrategicArtifactTypeDescriptor(
        StrategicArtifactType type,
        String prefix
) {

    public StrategicArtifactTypeDescriptor {
        Objects.requireNonNull(
                type,
                "El tipo es obligatorio"
        );

        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException(
                    "El prefijo es obligatorio"
            );
        }
    }

    public static StrategicArtifactTypeDescriptor of(
            StrategicArtifactType type
    ) {
        return new StrategicArtifactTypeDescriptor(
                type,
                switch (type) {
                    case FINDING -> "FND";
                    case BUSINESS_PROBLEM -> "PRB";
                    case BUSINESS_OBJECTIVE -> "OBJ";
                    case STRATEGIC_OPPORTUNITY -> "OPP";
                    case EXISTING_STRENGTH -> "STR";
                    case TRANSFORMATION_PRINCIPLE -> "PRN";
                }
        );
    }
}