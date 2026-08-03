package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.KnowledgeDetailResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class KnowledgeDetailApiService {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;
    private final KnowledgeContentRenderer contentRenderer;

    public KnowledgeDetailApiService(
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

    public KnowledgeDetailResponse findById(
            Long storeId,
            Long knowledgeObjectId
    ) {
        validateStoreId(storeId);
        validateKnowledgeObjectId(knowledgeObjectId);

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

        KnowledgeObjectVersion latestVersion =
                resolveLatestVersion(
                        knowledgeObject.getId(),
                        storeId
                );

        return buildResponse(
                knowledgeObject,
                latestVersion
        );
    }

    public KnowledgeDetailResponse findByCode(
            Long storeId,
            String code
    ) {
        validateStoreId(storeId);

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

        KnowledgeObjectVersion latestVersion =
                resolveLatestVersion(
                        knowledgeObject.getId(),
                        storeId
                );

        return buildResponse(
                knowledgeObject,
                latestVersion
        );
    }

    private KnowledgeDetailResponse buildResponse(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion latestVersion
    ) {
        return KnowledgeDetailResponse.from(
                knowledgeObject,
                mapVersion(
                        knowledgeObject.getCurrentVersion()
                ),
                mapVersion(
                        latestVersion
                )
        );
    }

    private KnowledgeDetailResponse.VersionDetail mapVersion(
            KnowledgeObjectVersion version
    ) {
        if (version == null) {
            return null;
        }

        KnowledgeContentRenderer.RenderedKnowledgeContent renderedContent =
                contentRenderer.render(
                        version.getContent(),
                        version.getContentFormat()
                );

        return KnowledgeDetailResponse.VersionDetail.from(
                version,
                renderedContent.html(),
                renderedContent.format()
        );
    }

    private KnowledgeObjectVersion resolveLatestVersion(
            Long knowledgeObjectId,
            Long storeId
    ) {
        return versionRepository
                .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                        knowledgeObjectId,
                        storeId
                )
                .orElse(null);
    }

    private void validateStoreId(
            Long storeId
    ) {
        if (storeId == null) {
            throw new IllegalArgumentException(
                    "El storeId es obligatorio"
            );
        }

        if (storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser mayor que cero"
            );
        }
    }

    private void validateKnowledgeObjectId(
            Long knowledgeObjectId
    ) {
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
                .toUpperCase(Locale.ROOT);
    }
}