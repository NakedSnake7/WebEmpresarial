package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgePageResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeSearchRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeSummaryResponse;
import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.specification.KnowledgeObjectSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class KnowledgeSearchApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;

    public KnowledgeSearchApiService(
            KnowledgeObjectRepository knowledgeObjectRepository
    ) {
        this.knowledgeObjectRepository =
                Objects.requireNonNull(
                        knowledgeObjectRepository,
                        "KnowledgeObjectRepository es obligatorio"
                );
    }

    public KnowledgePageResponse<KnowledgeSummaryResponse> search(
            Long storeId,
            KnowledgeSearchRequest request
    ) {
        if (storeId == null) {
            throw new IllegalArgumentException(
                    "El storeId es obligatorio"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "KnowledgeSearchRequest es obligatorio"
            );
        }

        KnowledgeQueryCriteria criteria =
                request.toCriteria(storeId);

        PageRequest pageable =
                PageRequest.of(
                        criteria.page(),
                        criteria.size()
                );

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        pageable
                );

        return KnowledgePageResponse.from(
                result,
                KnowledgeSummaryResponse::from
        );
    }
}