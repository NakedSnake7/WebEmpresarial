package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategicRelationshipRepository
        extends JpaRepository<StrategicRelationship, Long> {

    Optional<StrategicRelationship>
    findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
            Long projectId,
            Long sourceArtifactId,
            Long targetArtifactId,
            StrategicRelationshipType relationshipType
    );

    boolean
    existsByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
            Long projectId,
            Long sourceArtifactId,
            Long targetArtifactId,
            StrategicRelationshipType relationshipType
    );

    List<StrategicRelationship>
    findAllByProjectIdAndSourceArtifactId(
            Long projectId,
            Long sourceArtifactId
    );

    List<StrategicRelationship>
    findAllByProjectIdAndTargetArtifactId(
            Long projectId,
            Long targetArtifactId
    );
    List<StrategicRelationship>
    findAllByProjectIdAndSourceArtifactIdAndStatus(
            Long projectId,
            Long sourceArtifactId,
            StrategicRelationshipStatus status
    );
}