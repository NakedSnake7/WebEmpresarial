package com.webempresarial.store.digitaltransformation.domain.traceability;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraceabilityLinkRepository
        extends JpaRepository<TraceabilityLink, Long> {

    boolean existsByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
            Long projectId,
            Long sourceNodeId,
            Long targetNodeId,
            TraceabilityRelationType relationType
    );

    @EntityGraph(attributePaths = {
            "project",
            "project.store",
            "sourceNode",
            "targetNode"
    })
    Optional<TraceabilityLink> findByIdAndProjectStoreId(
            Long id,
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "sourceNode",
            "targetNode"
    })
    List<TraceabilityLink>
    findAllByProjectIdOrderByCreatedAtAsc(
            Long projectId
    );

    List<TraceabilityLink>
    findAllBySourceNodeIdOrderByCreatedAtAsc(
            Long sourceNodeId
    );

    List<TraceabilityLink>
    findAllByTargetNodeIdOrderByCreatedAtAsc(
            Long targetNodeId
    );

    List<TraceabilityLink>
    findAllByProjectIdAndRelationTypeOrderByCreatedAtAsc(
            Long projectId,
            TraceabilityRelationType relationType
    );

    List<TraceabilityLink>
    findAllByProjectIdAndRequiresReviewTrueOrderByCreatedAtAsc(
            Long projectId
    );
    Optional<TraceabilityLink>
    findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
            Long projectId,
            Long sourceNodeId,
            Long targetNodeId,
            TraceabilityRelationType relationType
    );
    List<TraceabilityLink>
    findAllByProjectIdAndSourceNodeIdAndRelationType(
            Long projectId,
            Long sourceNodeId,
            TraceabilityRelationType relationType
    );
}