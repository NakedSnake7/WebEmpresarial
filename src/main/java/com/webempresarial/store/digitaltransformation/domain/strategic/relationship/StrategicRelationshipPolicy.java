package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;

public interface StrategicRelationshipPolicy {

    void validate(
            StrategicArtifact source,
            StrategicArtifact target,
            StrategicRelationshipType relationshipType
    );
}