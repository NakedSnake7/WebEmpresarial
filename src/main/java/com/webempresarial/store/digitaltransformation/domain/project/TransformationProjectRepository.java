package com.webempresarial.store.digitaltransformation.domain.project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransformationProjectRepository
        extends JpaRepository<TransformationProject, Long> {

    boolean existsByStoreIdAndCodeIgnoreCase(
            Long storeId,
            String code
    );

    Optional<TransformationProject> findByStoreIdAndCodeIgnoreCase(
            Long storeId,
            String code
    );

    @EntityGraph(attributePaths = "store")
    Optional<TransformationProject> findByIdAndStoreId(
            Long id,
            Long storeId
    );

    List<TransformationProject> findAllByStoreIdOrderByCreatedAtDesc(
            Long storeId
    );

    List<TransformationProject>
    findAllByStoreIdAndStatusOrderByCreatedAtDesc(
            Long storeId,
            TransformationProjectStatus status
    );
}