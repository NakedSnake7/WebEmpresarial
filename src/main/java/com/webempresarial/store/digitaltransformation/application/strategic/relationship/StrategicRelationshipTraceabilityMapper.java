package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;

public interface StrategicRelationshipTraceabilityMapper {

    StrategicRelationshipTraceabilityMapping map(
            StrategicRelationshipType relationshipType
    );
}