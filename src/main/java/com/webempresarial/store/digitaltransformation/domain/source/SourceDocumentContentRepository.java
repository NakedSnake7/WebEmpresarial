package com.webempresarial.store.digitaltransformation.domain.source;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceDocumentContentRepository
        extends JpaRepository<SourceDocumentContent, Long> {

    boolean existsBySourceDocumentIdAndContentVersion(
            Long sourceDocumentId,
            int contentVersion
    );

    @EntityGraph(attributePaths = {
            "sourceDocument",
            "sourceDocument.project",
            "sourceDocument.project.store"
    })
    Optional<SourceDocumentContent>
    findByIdAndSourceDocumentProjectStoreId(
            Long id,
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "sourceDocument",
            "sourceDocument.project"
    })
    Optional<SourceDocumentContent>
    findBySourceDocumentIdAndCurrentTrue(
            Long sourceDocumentId
    );

    List<SourceDocumentContent>
    findAllBySourceDocumentIdOrderByContentVersionDesc(
            Long sourceDocumentId
    );
}