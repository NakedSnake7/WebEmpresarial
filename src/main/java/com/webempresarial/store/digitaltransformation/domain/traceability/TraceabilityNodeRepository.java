package com.webempresarial.store.digitaltransformation.domain.traceability;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraceabilityNodeRepository
        extends JpaRepository<TraceabilityNode, Long> {

    boolean existsByProjectIdAndNodeCodeIgnoreCase(
            Long projectId,
            String nodeCode
    );

    boolean existsByProjectIdAndNodeTypeAndExternalReference(
            Long projectId,
            TraceabilityNodeType nodeType,
            String externalReference
    );

    @EntityGraph(attributePaths = {
            "project",
            "project.store"
    })
    Optional<TraceabilityNode> findByIdAndProjectStoreId(
            Long id,
            Long storeId
    );

    Optional<TraceabilityNode>
    findByProjectIdAndNodeCodeIgnoreCase(
            Long projectId,
            String nodeCode
    );

    List<TraceabilityNode>
    findAllByProjectIdOrderByCreatedAtAsc(
            Long projectId
    );

    List<TraceabilityNode>
    findAllByProjectIdAndNodeTypeOrderByCreatedAtAsc(
            Long projectId,
            TraceabilityNodeType nodeType
    );

    List<TraceabilityNode>
    findAllByProjectIdAndStatusOrderByCreatedAtAsc(
            Long projectId,
            TraceabilityNodeStatus status
    );

    List<TraceabilityNode>
    findAllByProjectIdAndRequiresReviewTrueOrderByCreatedAtAsc(
            Long projectId
    );
}