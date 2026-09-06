package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategicSynthesisRecordRepository
        extends JpaRepository<StrategicSynthesisRecord, Long> {

    @EntityGraph(attributePaths = {
            "project",
            "project.store",
            "evidenceCodes",
            "sourceArtifactCodes"
    })
    Optional<StrategicSynthesisRecord>
    findFirstByProjectIdAndProjectStoreIdAndOriginOrderByCreatedAtDesc(
            Long projectId,
            Long storeId,
            StrategicSynthesisOrigin origin
    );

    @EntityGraph(attributePaths = {
            "project",
            "project.store",
            "evidenceCodes",
            "sourceArtifactCodes"
    })
    List<StrategicSynthesisRecord>
    findAllByProjectIdAndProjectStoreIdOrderByCreatedAtDesc(
            Long projectId,
            Long storeId
    );
    @EntityGraph(attributePaths = {
            "project",
            "project.store",
            "evidenceCodes",
            "sourceArtifactCodes"
    })
    Optional<StrategicSynthesisRecord>
    findByIdAndProjectIdAndProjectStoreId(
            Long id,
            Long projectId,
            Long storeId
    );
}