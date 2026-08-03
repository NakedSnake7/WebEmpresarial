package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgeVersionSummaryResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class KnowledgeVersionQueryApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;

    public KnowledgeVersionQueryApiService(
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

    public List<KnowledgeVersionSummaryResponse> findAll(
            Long storeId,
            Long knowledgeObjectId
    ) {
        validateIdentifier(
                storeId,
                "storeId"
        );

        validateIdentifier(
                knowledgeObjectId,
                "knowledgeObjectId"
        );

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

        List<KnowledgeObjectVersion> versions =
                versionRepository
                        .findByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                knowledgeObjectId,
                                storeId
                        );

        Long currentVersionId =
                knowledgeObject.getCurrentVersion() != null
                        ? knowledgeObject.getCurrentVersion().getId()
                        : null;

        Long latestVersionId =
                versions.isEmpty()
                        ? null
                        : versions.getFirst().getId();

        return versions.stream()
                .map(
                        version ->
                                KnowledgeVersionSummaryResponse.from(
                                        version,
                                        currentVersionId,
                                        latestVersionId
                                )
                )
                .toList();
    }

    private void validateIdentifier(
            Long value,
            String field
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    "El " + field + " debe ser válido"
            );
        }
    }
}