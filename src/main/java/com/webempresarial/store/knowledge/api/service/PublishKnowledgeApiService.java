package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgeLifecycleResponse;
import com.webempresarial.store.knowledge.api.dto.PublishKnowledgeRequest;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.api.exception.KnowledgeVersionNotFoundException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class PublishKnowledgeApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;

    public PublishKnowledgeApiService(
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

    public KnowledgeLifecycleResponse publish(
            Long storeId,
            Long knowledgeObjectId,
            PublishKnowledgeRequest request,
            String actor
    ) {
        validateIdentifiers(
                storeId,
                knowledgeObjectId
        );

        Objects.requireNonNull(
                request,
                "PublishKnowledgeRequest es obligatorio"
        );

        validateActor(actor);
        validateValidityPeriod(request);

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

        KnowledgeObjectVersion version =
                versionRepository
                        .findDetailedByIdAndKnowledgeObjectStoreId(
                                request.versionId(),
                                storeId
                        )
                        .orElseThrow(
                                () -> new KnowledgeVersionNotFoundException(
                                        request.versionId()
                                )
                        );

        /*
         * Defensa adicional contra IDOR dentro del mismo Store:
         * la versión debe pertenecer al objeto indicado en la URL.
         */
        if (!version.belongsTo(knowledgeObject)) {
            throw new IllegalArgumentException(
                    "La versión seleccionada no pertenece al KnowledgeObject"
            );
        }

        knowledgeObject.publish(
                version,
                request.validFrom(),
                request.validUntil(),
                actor.trim()
        );

        KnowledgeObject savedKnowledgeObject =
                knowledgeObjectRepository.saveAndFlush(
                        knowledgeObject
                );

        return KnowledgeLifecycleResponse.from(
                savedKnowledgeObject
        );
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

    private void validateValidityPeriod(
            PublishKnowledgeRequest request
    ) {
        if (request.validFrom() == null) {
            throw new IllegalArgumentException(
                    "La fecha inicial de vigencia es obligatoria"
            );
        }

        if (request.validUntil() != null
                && !request.validUntil()
                        .isAfter(request.validFrom())) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia debe ser posterior "
                            + "a la fecha inicial"
            );
        }
    }
}