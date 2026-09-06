package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityRelationType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityStrength;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicRelationshipTraceabilityMapper
        implements StrategicRelationshipTraceabilityMapper {

    @Override
    public StrategicRelationshipTraceabilityMapping map(
            StrategicRelationshipType relationshipType
    ) {
        Objects.requireNonNull(
                relationshipType,
                "El tipo de relación estratégica es obligatorio"
        );

        return switch (relationshipType) {

            case REVEALS ->
                    derivedFrom();

            case ADDRESSED_BY ->
                    derivedFrom();

            case ENABLES ->
                    derivedFrom();
        };
    }

    private static StrategicRelationshipTraceabilityMapping
    derivedFrom() {
        return new StrategicRelationshipTraceabilityMapping(
                true,
                TraceabilityRelationType.DERIVED_FROM,
                TraceabilityStrength.STRONG
        );
    }
}