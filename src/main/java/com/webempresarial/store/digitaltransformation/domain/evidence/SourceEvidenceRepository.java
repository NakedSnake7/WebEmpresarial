package com.webempresarial.store.digitaltransformation.domain.evidence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceEvidenceRepository
        extends JpaRepository<SourceEvidence, Long> {

    boolean existsByProjectIdAndEvidenceCodeIgnoreCase(
            Long projectId,
            String evidenceCode
    );

    @EntityGraph(attributePaths = {
            "project",
            "project.store",
            "sourceDocument",
            "sourceSection"
    })
    Optional<SourceEvidence> findByIdAndProjectStoreId(
            Long id,
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "sourceDocument",
            "sourceSection"
    })
    List<SourceEvidence>
    findAllByProjectIdOrderByExtractedAtAsc(
            Long projectId
    );

    List<SourceEvidence>
    findAllByProjectIdAndClassificationOrderByExtractedAtAsc(
            Long projectId,
            EvidenceClassification classification
    );

    List<SourceEvidence>
    findAllByProjectIdAndStatusOrderByExtractedAtAsc(
            Long projectId,
            EvidenceStatus status
    );

    List<SourceEvidence>
    findAllByProjectIdAndRequiresHumanReviewTrueOrderByExtractedAtAsc(
            Long projectId
    );

    long countByProjectIdAndStatus(
            Long projectId,
            EvidenceStatus status
    );
}