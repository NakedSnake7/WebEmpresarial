package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.application.result.KnowledgeQueryItem;
import com.webempresarial.store.knowledge.application.result.KnowledgeQueryPage;
import com.webempresarial.store.knowledge.application.usecase.KnowledgeQueryUseCase;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.specification.KnowledgeObjectSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Implementación del Knowledge Query Engine.
 */
@Service
public class KnowledgeQueryService
        implements KnowledgeQueryUseCase {

    private final KnowledgeObjectRepository repository;

    public KnowledgeQueryService(
            KnowledgeObjectRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "KnowledgeObjectRepository es obligatorio"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeQueryPage search(
            KnowledgeQueryCriteria criteria
    ) {
        Objects.requireNonNull(
                criteria,
                "KnowledgeQueryCriteria es obligatorio"
        );

        Pageable pageable =
                PageRequest.of(
                        criteria.page(),
                        criteria.size(),
                        Sort.by(
                                Sort.Order.desc("updatedAt"),
                                Sort.Order.desc("id")
                        )
                );

        Page<KnowledgeObject> result =
                repository.findAll(
                        KnowledgeObjectSpecification.from(
                                criteria
                        ),
                        pageable
                );

        List<KnowledgeQueryItem> items =
                result.getContent()
                        .stream()
                        .map(KnowledgeQueryService::toItem)
                        .toList();

        return new KnowledgeQueryPage(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private static KnowledgeQueryItem toItem(
            KnowledgeObject knowledgeObject
    ) {
        validateRequiredAggregateFields(
                knowledgeObject
        );

        KnowledgeObjectVersion currentVersion =
                knowledgeObject.getCurrentVersion();

        if (currentVersion == null) {
            return withoutCurrentVersion(
                    knowledgeObject
            );
        }

        validateCurrentVersion(
                knowledgeObject,
                currentVersion
        );

        return withCurrentVersion(
                knowledgeObject,
                currentVersion
        );
    }

    private static KnowledgeQueryItem withoutCurrentVersion(
            KnowledgeObject knowledgeObject
    ) {
        return new KnowledgeQueryItem(
                knowledgeObject.getId(),
                knowledgeObject.getStore().getId(),
                knowledgeObject.getCode().getValue(),
                knowledgeObject.getTypeCode(),
                knowledgeObject.getDomain(),
                knowledgeObject.getClassification(),
                knowledgeObject.getRiskLevel(),
                knowledgeObject.getStatus(),
                knowledgeObject.getContextRoot().getType(),
                knowledgeObject.getContextRoot().getReference(),
                null,
                null,
                null,
                null,
                null,
                null,
                knowledgeObject.getValidFrom(),
                knowledgeObject.getValidUntil(),
                knowledgeObject.getCreatedAt(),
                knowledgeObject.getUpdatedAt()
        );
    }

    private static KnowledgeQueryItem withCurrentVersion(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion currentVersion
    ) {
        return new KnowledgeQueryItem(
                knowledgeObject.getId(),
                knowledgeObject.getStore().getId(),
                knowledgeObject.getCode().getValue(),
                knowledgeObject.getTypeCode(),
                knowledgeObject.getDomain(),
                knowledgeObject.getClassification(),
                knowledgeObject.getRiskLevel(),
                knowledgeObject.getStatus(),
                knowledgeObject.getContextRoot().getType(),
                knowledgeObject.getContextRoot().getReference(),
                currentVersion.getId(),
                currentVersion
                        .getSemanticVersion()
                        .toString(),
                currentVersion.getTitle(),
                currentVersion.getSummary(),
                currentVersion.getContentFormat(),
                currentVersion
                        .getConfidence()
                        .getValue(),
                knowledgeObject.getValidFrom(),
                knowledgeObject.getValidUntil(),
                knowledgeObject.getCreatedAt(),
                knowledgeObject.getUpdatedAt()
        );
    }

    private static void validateRequiredAggregateFields(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject == null) {
            throw new IllegalStateException(
                    "KnowledgeObject no puede ser null"
            );
        }

        if (knowledgeObject.getId() == null) {
            throw new IllegalStateException(
                    "KnowledgeObject no tiene identificador"
            );
        }

        if (knowledgeObject.getStore() == null
                || knowledgeObject.getStore().getId() == null) {

            throw new IllegalStateException(
                    "KnowledgeObject no tiene Store válida"
            );
        }

        if (knowledgeObject.getCode() == null) {
            throw new IllegalStateException(
                    "KnowledgeObject no tiene código"
            );
        }

        if (knowledgeObject.getContextRoot() == null) {
            throw new IllegalStateException(
                    "KnowledgeObject no tiene contexto"
            );
        }

        if (knowledgeObject.getCreatedAt() == null
                || knowledgeObject.getUpdatedAt() == null) {

            throw new IllegalStateException(
                    "KnowledgeObject no tiene auditoría temporal"
            );
        }
    }

    private static void validateCurrentVersion(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion currentVersion
    ) {
        if (!currentVersion.belongsTo(knowledgeObject)) {
            throw new IllegalStateException(
                    "La versión vigente no pertenece al KnowledgeObject"
            );
        }

        if (currentVersion.getId() == null) {
            throw new IllegalStateException(
                    "La versión vigente no tiene identificador"
            );
        }

        if (currentVersion.getSemanticVersion() == null) {
            throw new IllegalStateException(
                    "La versión vigente no tiene SemanticVersion"
            );
        }

        if (currentVersion.getConfidence() == null) {
            throw new IllegalStateException(
                    "La versión vigente no tiene KnowledgeConfidence"
            );
        }
    }
}