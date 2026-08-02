package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeVersionRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeVersionCreatedResponse;
import com.webempresarial.store.knowledge.api.exception.DuplicateKnowledgeVersionException;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateKnowledgeVersionApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;

    public CreateKnowledgeVersionApiService(
            KnowledgeObjectRepository knowledgeObjectRepository,
            KnowledgeObjectVersionRepository versionRepository
    ) {
        this.knowledgeObjectRepository =
                Objects.requireNonNull(
                        knowledgeObjectRepository,
                        "KnowledgeObjectRepository es obligatorio"
                );

        this.versionRepository =
                Objects.requireNonNull(
                        versionRepository,
                        "KnowledgeObjectVersionRepository es obligatorio"
                );
    }

    public KnowledgeVersionCreatedResponse create(
            Long storeId,
            Long knowledgeObjectId,
            CreateKnowledgeVersionRequest request,
            String actor
    ) {
        validateIdentifiers(
                storeId,
                knowledgeObjectId
        );

        Objects.requireNonNull(
                request,
                "CreateKnowledgeVersionRequest es obligatorio"
        );

        validateActor(actor);

        KnowledgeObject knowledgeObject =
                knowledgeObjectRepository
                        .findGovernedAggregate(
                                knowledgeObjectId,
                                storeId
                        )
                        .orElseThrow(
                                () -> new KnowledgeObjectNotFoundException(
                                        knowledgeObjectId
                                )
                        );

        SemanticVersion semanticVersion =
                SemanticVersion.of(
                        request.major(),
                        request.minor(),
                        request.patch()
                );

        ensureVersionDoesNotExist(
                knowledgeObjectId,
                storeId,
                semanticVersion,
                request
        );

        KnowledgeObjectVersion version =
                knowledgeObject.createVersion(
                        semanticVersion,
                        request.normalizedTitle(),
                        request.normalizedSummary(),
                        request.normalizedContent(),
                        request.normalizedContentFormat(),
                        KnowledgeConfidence.of(
                                request.confidence()
                        ),
                        request.normalizedSourceReference(),
                        actor.trim()
                );

        KnowledgeObjectVersion savedVersion =
                versionRepository.saveAndFlush(
                        version
                );

        return KnowledgeVersionCreatedResponse.from(
                savedVersion
        );
    }

    private void ensureVersionDoesNotExist(
            Long knowledgeObjectId,
            Long storeId,
            SemanticVersion semanticVersion,
            CreateKnowledgeVersionRequest request
    ) {
        boolean exists =
                versionRepository
                        .existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
                                knowledgeObjectId,
                                storeId,
                                request.major(),
                                request.minor(),
                                request.patch()
                        );

        if (exists) {
            throw new DuplicateKnowledgeVersionException(
                    semanticVersion.toString()
            );
        }
    }

    private void validateIdentifiers(
            Long storeId,
            Long knowledgeObjectId
    ) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (knowledgeObjectId == null
                || knowledgeObjectId <= 0) {

            throw new IllegalArgumentException(
                    "El knowledgeObjectId debe ser válido"
            );
        }
    }

    private void validateActor(
            String actor
    ) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor es obligatorio"
            );
        }
    }
}