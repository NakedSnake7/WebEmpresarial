package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgeDetailResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class KnowledgeDetailApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;

    public KnowledgeDetailApiService(
            KnowledgeObjectRepository knowledgeObjectRepository
    ) {
        this.knowledgeObjectRepository =
                Objects.requireNonNull(
                        knowledgeObjectRepository,
                        "KnowledgeObjectRepository es obligatorio"
                );
    }

    public KnowledgeDetailResponse findById(
            Long storeId,
            Long knowledgeObjectId
    ) {
        if (storeId == null) {
            throw new IllegalArgumentException(
                    "El storeId es obligatorio"
            );
        }

        if (knowledgeObjectId == null) {
            throw new IllegalArgumentException(
                    "El knowledgeObjectId es obligatorio"
            );
        }

        if (knowledgeObjectId <= 0) {
            throw new IllegalArgumentException(
                    "El knowledgeObjectId debe ser mayor que cero"
            );
        }

        KnowledgeObject knowledgeObject =
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
                                knowledgeObjectId,
                                storeId
                        )
                        .orElseThrow(
                                () -> new KnowledgeObjectNotFoundException(
                                        knowledgeObjectId
                                )
                        );

        return KnowledgeDetailResponse.from(
                knowledgeObject
        );
    }
    
    
    public KnowledgeDetailResponse findByCode(
            Long storeId,
            String code
    ) {
        if (storeId == null) {
            throw new IllegalArgumentException(
                    "El storeId es obligatorio"
            );
        }

        String normalizedCode =
                normalizeCode(code);

        KnowledgeObject knowledgeObject =
                knowledgeObjectRepository
                        .findByStoreIdAndCodeValue(
                                storeId,
                                normalizedCode
                        )
                        .orElseThrow(
                                () -> new KnowledgeObjectNotFoundException(
                                        "No se encontró el KnowledgeObject con código "
                                                + normalizedCode
                                )
                        );

        return KnowledgeDetailResponse.from(
                knowledgeObject
        );
    }

    private String normalizeCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del KnowledgeObject es obligatorio"
            );
        }

        return code
                .trim()
                .toUpperCase();
    }
}