package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityRelationType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;

import java.util.Objects;

public record StrategicRelationshipTraceabilityMapping(
        boolean reverseDirection,
        TraceabilityRelationType relationType,
        TraceabilityStrength strength
) {

    public StrategicRelationshipTraceabilityMapping {
        Objects.requireNonNull(
                relationType,
                "El tipo de relación de trazabilidad es obligatorio"
        );

        Objects.requireNonNull(
                strength,
                "La fuerza de relación es obligatoria"
        );
    }
}