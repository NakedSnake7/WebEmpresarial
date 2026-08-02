package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest; 
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.exception.DuplicateKnowledgeCodeException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import com.webempresarial.store.model.Store;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateKnowledgeApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;

    public CreateKnowledgeApiService(
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

    public KnowledgeCreatedResponse create(
            Store store,
            CreateKnowledgeRequest request,
            String actor
    ) {
        validateStore(store);

        Objects.requireNonNull(
                request,
                "CreateKnowledgeRequest es obligatorio"
        );

        validateActor(actor);

        String normalizedActor =
                actor.trim();

        String normalizedCode =
                request.normalizedCode();

        ensureCodeIsAvailable(
                store.getId(),
                normalizedCode
        );

        KnowledgeObject knowledgeObject =
                createAggregate(
                        store,
                        request,
                        normalizedActor
                );

        KnowledgeObject savedKnowledgeObject =
                knowledgeObjectRepository.saveAndFlush(
                        knowledgeObject
                );

        KnowledgeObjectVersion initialVersion =
                createInitialVersion(
                        savedKnowledgeObject,
                        request,
                        normalizedActor
                );

        KnowledgeObjectVersion savedVersion =
                versionRepository.saveAndFlush(
                        initialVersion
                );

        return KnowledgeCreatedResponse.from(
                savedKnowledgeObject,
                savedVersion
        );
    }

    private KnowledgeObject createAggregate(
            Store store,
            CreateKnowledgeRequest request,
            String actor
    ) {
        KnowledgeContextRoot contextRoot =
                createContextRoot(
                        request,
                        store
                );

        return KnowledgeObject.create(
                store,
                KnowledgeCode.of(
                        request.normalizedCode()
                ),
                request.typeCode(),
                request.domain(),
                request.classification(),
                request.riskLevel(),
                contextRoot,
                actor
        );
    }

    private KnowledgeObjectVersion createInitialVersion(
            KnowledgeObject knowledgeObject,
            CreateKnowledgeRequest request,
            String actor
    ) {
        return knowledgeObject.createVersion(
                SemanticVersion.of(1, 0, 0),
                request.normalizedTitle(),
                request.normalizedSummary(),
                request.content().trim(),
                request.normalizedContentFormat(),
                KnowledgeConfidence.of(
                        request.confidence()
                ),
                request.normalizedSourceReference(),
                actor
        );
    }

    private KnowledgeContextRoot createContextRoot(
            CreateKnowledgeRequest request,
            Store store
    ) {
        if (request.contextType() == null) {
            return KnowledgeContextRoot.store(
                    store.getId()
            );
        }

        return KnowledgeContextRoot.of(
                request.contextType(),
                request.normalizedContextReference()
        );
    }

    private void ensureCodeIsAvailable(
            Long storeId,
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del conocimiento es obligatorio"
            );
        }

        boolean alreadyExists =
                knowledgeObjectRepository
                        .existsByStoreIdAndCodeValue(
                                storeId,
                                code
                        );

        if (alreadyExists) {
            throw new DuplicateKnowledgeCodeException(
                    code
            );
        }
    }

    private void validateStore(
            Store store
    ) {
        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        if (!store.isActiva()) {
            throw new IllegalStateException(
                    "No se puede crear conocimiento en una tienda inactiva"
            );
        }
    }

    private void validateActor(
            String actor
    ) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor del comando es obligatorio"
            );
        }
    }
}