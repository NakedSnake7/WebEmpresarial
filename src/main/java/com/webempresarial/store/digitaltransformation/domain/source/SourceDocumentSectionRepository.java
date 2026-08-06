package com.webempresarial.store.digitaltransformation.domain.source;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceDocumentSectionRepository
        extends JpaRepository<SourceDocumentSection, Long> {

    boolean existsBySourceContentIdAndSectionCodeIgnoreCase(
            Long sourceContentId,
            String sectionCode
    );

    @EntityGraph(attributePaths = {
            "sourceContent",
            "sourceContent.sourceDocument",
            "sourceContent.sourceDocument.project",
            "sourceContent.sourceDocument.project.store"
    })
    Optional<SourceDocumentSection>
    findByIdAndSourceContentSourceDocumentProjectStoreId(
            Long id,
            Long storeId
    );

    List<SourceDocumentSection>
    findAllBySourceContentIdOrderByDisplayOrderAsc(
            Long sourceContentId
    );

    List<SourceDocumentSection>
    findAllBySourceContentIdAndSectionTypeOrderByDisplayOrderAsc(
            Long sourceContentId,
            SourceSectionType sectionType
    );
}