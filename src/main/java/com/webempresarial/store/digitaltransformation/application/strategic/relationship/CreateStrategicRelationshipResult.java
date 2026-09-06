package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;

public record CreateStrategicRelationshipResult(
        Long relationshipId,
        Long sourceArtifactId,
        String sourceArtifactCode,
        Long targetArtifactId,
        String targetArtifactCode,
        StrategicRelationshipType relationshipType,
        StrategicRelationshipStatus status,
        boolean created
) {
}