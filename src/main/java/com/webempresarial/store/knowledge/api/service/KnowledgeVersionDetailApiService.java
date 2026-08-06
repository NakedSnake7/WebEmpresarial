package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgeVersionDetailResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.api.exception.KnowledgeVersionNotFoundException;
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
public class KnowledgeVersionDetailApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;
    private final KnowledgeContentRenderer contentRenderer;

    public KnowledgeVersionDetailApiService(
            KnowledgeObjectRepository knowledgeObjectRepository,
            KnowledgeObjectVersionRepository versionRepository,
            KnowledgeContentRenderer contentRenderer
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

        this.contentRenderer =
                Objects.requireNonNull(
                        contentRenderer,
                        "KnowledgeContentRenderer es obligatorio"
                );
    }

    public KnowledgeVersionDetailResponse findById(
            Long storeId,
            Long knowledgeObjectId,
            Long versionId
    ) {
        validateIdentifier(
                storeId,
                "storeId"
        );

        validateIdentifier(
                knowledgeObjectId,
                "knowledgeObjectId"
        );

        validateIdentifier(
                versionId,
                "versionId"
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

        KnowledgeObjectVersion version =
                versionRepository
                        .findByIdAndKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                versionId,
                                knowledgeObjectId,
                                storeId
                        )
                        .orElseThrow(
                                () -> new KnowledgeVersionNotFoundException(
                                        versionId
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

        KnowledgeContentRenderer.RenderedKnowledgeContent rendered =
                contentRenderer.render(
                        version.getContent(),
                        version.getContentFormat()
                );

        return KnowledgeVersionDetailResponse.from(
                version,
                rendered.html(),
                rendered.format(),
                currentVersionId,
                latestVersionId
        );
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