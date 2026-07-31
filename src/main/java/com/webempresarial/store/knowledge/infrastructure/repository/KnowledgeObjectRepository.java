package com.webempresarial.store.knowledge.infrastructure.repository;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KnowledgeObjectRepository
        extends JpaRepository<KnowledgeObject, Long>,
        JpaSpecificationExecutor<KnowledgeObject> {
	
	
	@Override
	@EntityGraph(attributePaths = {
	        "store",
	        "currentVersion"
	})
	Page<KnowledgeObject> findAll(
	        Specification<KnowledgeObject> specification,
	        Pageable pageable
	);

    Optional<KnowledgeObject> findByIdAndStoreId(
            Long id,
            Long storeId
    );

    @EntityGraph(attributePaths = {
            "store",
            "currentVersion"
    })
    Optional<KnowledgeObject> findByStoreIdAndCodeValue(
            Long storeId,
            String code
    );

    boolean existsByStoreIdAndCodeValue(
            Long storeId,
            String code
    );



    Page<KnowledgeObject> findByStoreIdOrderByCreatedAtDesc(
            Long storeId,
            Pageable pageable
    );

    Page<KnowledgeObject> findByStoreIdAndStatusOrderByCreatedAtDesc(
            Long storeId,
            KnowledgeStatus status,
            Pageable pageable
    );

    Page<KnowledgeObject> findByStoreIdAndDomainOrderByCreatedAtDesc(
            Long storeId,
            KnowledgeDomain domain,
            Pageable pageable
    );

    Page<KnowledgeObject>
    findByStoreIdAndContextRootTypeAndContextRootReferenceOrderByCreatedAtDesc(
            Long storeId,
            KnowledgeContextType contextType,
            String contextReference,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "currentVersion")
    Optional<KnowledgeObject> findWithCurrentVersionByIdAndStoreId(
            Long id,
            Long storeId
    );

    long countByStoreIdAndStatus(
            Long storeId,
            KnowledgeStatus status
    );

    boolean existsByIdAndStoreId(
            Long id,
            Long storeId
    );

    @Query("""
        SELECT ko
        FROM KnowledgeObject ko
        JOIN FETCH ko.store s
        LEFT JOIN FETCH ko.currentVersion cv
        WHERE ko.id = :knowledgeObjectId
        AND s.id = :storeId
    """)
    Optional<KnowledgeObject> findGovernedAggregate(
            Long knowledgeObjectId,
            Long storeId
    );

    @Query("""
        SELECT ko
        FROM KnowledgeObject ko
        JOIN FETCH ko.store s
        JOIN FETCH ko.currentVersion cv
        WHERE s.id = :storeId
        AND ko.code.value = :code
        AND ko.status = :status
    """)
    Optional<KnowledgeObject> findPublishedByStoreIdAndCode(
            Long storeId,
            String code,
            KnowledgeStatus status
    );
}