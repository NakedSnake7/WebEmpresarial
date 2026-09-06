package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategicSynthesisReviewRecordRepository
        extends JpaRepository<StrategicSynthesisReviewRecord, Long> {

    @EntityGraph(attributePaths = {
            "reviewedSynthesis",
            "reviewedSynthesis.project",
            "reviewedSynthesis.project.store",
            "reviewedSynthesis.evidenceCodes",
            "reviewedSynthesis.sourceArtifactCodes",
            "resultingSynthesis"
    })
    Optional<StrategicSynthesisReviewRecord>
    findFirstByReviewedSynthesisIdAndReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
            Long synthesisId,
            Long projectId,
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "reviewedSynthesis",
            "reviewedSynthesis.project",
            "reviewedSynthesis.project.store",
            "reviewedSynthesis.evidenceCodes",
            "reviewedSynthesis.sourceArtifactCodes",
            "resultingSynthesis"
    })
    List<StrategicSynthesisReviewRecord>
    findAllByReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
            Long projectId,
            Long storeId
    );
}