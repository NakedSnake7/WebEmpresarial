package com.webempresarial.store.digitaltransformation.domain.strategic;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategicArtifactRepository
        extends JpaRepository<StrategicArtifact, Long> {

    boolean existsByProjectIdAndArtifactCodeIgnoreCase(
            Long projectId,
            String artifactCode
    );

    @EntityGraph(attributePaths = {
            "project",
            "project.store"
    })
    Optional<StrategicArtifact>
    findByIdAndProjectStoreId(
            Long id,
            Long storeId
    );

    Optional<StrategicArtifact>
    findByProjectIdAndArtifactCodeIgnoreCase(
            Long projectId,
            String artifactCode
    );

    List<StrategicArtifact>
    findAllByProjectIdOrderByCreatedAtAsc(
            Long projectId
    );

    List<StrategicArtifact>
    findAllByProjectIdAndArtifactTypeOrderByCreatedAtAsc(
            Long projectId,
            StrategicArtifactType artifactType
    );

    List<StrategicArtifact>
    findAllByProjectIdAndStatusOrderByCreatedAtAsc(
            Long projectId,
            StrategicArtifactStatus status
    );

    List<StrategicArtifact>
    findAllByProjectIdAndPriorityOrderByCreatedAtAsc(
            Long projectId,
            StrategicPriority priority
    );

    List<StrategicArtifact>
    findAllByProjectIdAndRequiresReviewTrueOrderByCreatedAtAsc(
            Long projectId
    );
    long countByProjectIdAndArtifactType(
            Long projectId,
            StrategicArtifactType artifactType
    );
    
    Optional<StrategicArtifact>
    findBySourceEvidenceIdAndArtifactType(
            Long sourceEvidenceId,
            StrategicArtifactType artifactType
    );
    boolean existsBySourceEvidenceIdAndArtifactType(
            Long sourceEvidenceId,
            StrategicArtifactType artifactType
    );
    Optional<StrategicArtifact>
    findByIdAndProjectIdAndProjectStoreId(
            Long artifactId,
            Long projectId,
            Long storeId
    );
    List<StrategicArtifact>
    findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
            Long projectId,
            Long storeId,
            StrategicArtifactType artifactType
    );
    
}