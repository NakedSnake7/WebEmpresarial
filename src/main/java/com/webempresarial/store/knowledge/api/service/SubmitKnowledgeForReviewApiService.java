package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgeLifecycleResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class SubmitKnowledgeForReviewApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;

    public SubmitKnowledgeForReviewApiService(
            KnowledgeObjectRepository knowledgeObjectRepository
    ) {
        this.knowledgeObjectRepository =
                Objects.requireNonNull(
                        knowledgeObjectRepository,
                        "KnowledgeObjectRepository es obligatorio"
                );
    }

    public KnowledgeLifecycleResponse submit(
            Long storeId,
            Long knowledgeObjectId,
            String actor
    ) {
        validateIdentifiers(
                storeId,
                knowledgeObjectId
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

        knowledgeObject.submitForReview(
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
}