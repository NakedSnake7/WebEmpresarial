package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.exception.DuplicateKnowledgeCodeException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.model.Store;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CreateKnowledgeApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;

    public CreateKnowledgeApiService(
            KnowledgeObjectRepository knowledgeObjectRepository
    ) {
        this.knowledgeObjectRepository =
                Objects.requireNonNull(
                        knowledgeObjectRepository,
                        "KnowledgeObjectRepository es obligatorio"
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

        String normalizedCode =
                request.normalizedCode();

        validateActor(actor);
        ensureCodeIsAvailable(
                store.getId(),
                normalizedCode
        );

        KnowledgeObject knowledgeObject =
                createAggregate(
                        store,
                        request,
                        actor.trim()
                );

        KnowledgeObject savedKnowledgeObject =
                knowledgeObjectRepository.save(
                        knowledgeObject
                );

        return KnowledgeCreatedResponse.from(
                savedKnowledgeObject
        );
    }

    private void ensureCodeIsAvailable(
            Long storeId,
            String code
    ) {
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

    private KnowledgeObject createAggregate(
            Store store,
            CreateKnowledgeRequest request,
            String actor
    ) {
        /*
         * Aquí conectaremos exclusivamente las fábricas reales:
         *
         * KnowledgeCode
         * KnowledgeContextRoot
         * KnowledgeConfidence
         * SemanticVersion
         * KnowledgeObject
         * KnowledgeObjectVersion
         *
         * No debemos construir estas reglas por duplicado desde la API.
         */

        throw new UnsupportedOperationException(
                "Pendiente conectar la fábrica real de KnowledgeObject"
        );
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