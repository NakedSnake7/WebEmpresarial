package com.webempresarial.store.knowledge.infrastructure.repository;

import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeObjectVersionRepository
        extends JpaRepository<KnowledgeObjectVersion, Long> {

    Optional<KnowledgeObjectVersion>
    findByIdAndKnowledgeObjectStoreId(
            Long id,
            Long storeId
    );
    
    Optional<KnowledgeObjectVersion>
    findByIdAndKnowledgeObjectIdAndKnowledgeObjectStoreId(
            Long versionId,
            Long knowledgeObjectId,
            Long storeId
    );
    
    List<KnowledgeObjectVersion>
    findByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
            Long knowledgeObjectId,
            Long storeId
    );

    Optional<KnowledgeObjectVersion>
    findByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
            Long knowledgeObjectId,
            Long storeId,
            int major,
            int minor,
            int patch
    );

    boolean
    existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
            Long knowledgeObjectId,
            Long storeId,
            int major,
            int minor,
            int patch
    );

    Page<KnowledgeObjectVersion>
    findByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
            Long knowledgeObjectId,
            Long storeId,
            Pageable pageable
    );

    Optional<KnowledgeObjectVersion>
    findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
            Long knowledgeObjectId,
            Long storeId
    );

    long countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
            Long knowledgeObjectId,
            Long storeId
    );

    @EntityGraph(
            attributePaths = {
                    "knowledgeObject",
                    "knowledgeObject.store"
            }
    )
    Optional<KnowledgeObjectVersion>
    findDetailedByIdAndKnowledgeObjectStoreId(
            Long id,
            Long storeId
    );

    boolean existsByIdAndKnowledgeObjectStoreId(
            Long id,
            Long storeId
    );
}