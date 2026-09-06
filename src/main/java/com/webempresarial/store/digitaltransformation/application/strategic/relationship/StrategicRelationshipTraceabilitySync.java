package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLink;

import java.util.Objects;

public record StrategicRelationshipTraceabilitySync(
        TraceabilityLink link,
        boolean created
) {

    public StrategicRelationshipTraceabilitySync {
        Objects.requireNonNull(
                link,
                "La relación de trazabilidad es obligatoria"
        );
    }
}