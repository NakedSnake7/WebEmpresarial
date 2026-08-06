package com.webempresarial.store.digitaltransformation.domain.source;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransformationSourceDocumentRepository
        extends JpaRepository<TransformationSourceDocument, Long> {

    boolean existsByProjectIdAndChecksumSha256(
            Long projectId,
            String checksumSha256
    );

    boolean existsByProjectIdAndSourceTypeAndDocumentVersion(
            Long projectId,
            TransformationSourceType sourceType,
            int documentVersion
    );
    
    @EntityGraph(attributePaths = {
            "project",
            "project.store"
    })
    List<TransformationSourceDocument>
    findAllByProjectIdAndAuthoritativeTrueOrderByRegisteredAtAsc(
            Long projectId
    );

    @EntityGraph(attributePaths = {
            "project",
            "project.store"
    })
    Optional<TransformationSourceDocument> findByIdAndProjectStoreId(
            Long id,
            Long storeId
    );

    List<TransformationSourceDocument>
    findAllByProjectIdOrderByRegisteredAtAsc(
            Long projectId
    );

    List<TransformationSourceDocument>
    findAllByProjectIdAndSourceRoleOrderByRegisteredAtAsc(
            Long projectId,
            TransformationSourceRole sourceRole
    );


    long countByProjectIdAndStatus(
            Long projectId,
            TransformationSourceStatus status
    );
}