package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.PublishKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.PublishKnowledgeResult;
import com.webempresarial.store.knowledge.application.usecase.PublishKnowledgeUseCase;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.event.KnowledgePublishedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de uso que gobierna la transición:
 *
 * <pre>
 * APPROVED -> PUBLISHED
 * </pre>
 */
@Service
public class PublishKnowledgeService
        implements PublishKnowledgeUseCase {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PublishKnowledgeService(
            KnowledgeObjectRepository knowledgeObjectRepository,
            KnowledgeObjectVersionRepository versionRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.knowledgeObjectRepository =
                knowledgeObjectRepository;

        this.versionRepository =
                versionRepository;

        this.eventPublisher =
                eventPublisher;
    }

    @Override
    @Transactional
    public PublishKnowledgeResult execute(
            PublishKnowledgeCommand command
    ) {
        Objects.requireNonNull(
                command,
                "PublishKnowledgeCommand es obligatorio"
        );

        KnowledgeObject knowledgeObject =
                resolveKnowledgeObject(
                        command.knowledgeObjectId(),
                        command.storeId()
                );

        validateApprovedStatus(knowledgeObject);

        KnowledgeObjectVersion version =
                resolveVersion(
                        command.knowledgeVersionId(),
                        command.storeId()
                );

        validateVersionBelongsToObject(
                version,
                knowledgeObject
        );

        KnowledgeStatus previousStatus =
                knowledgeObject.getStatus();

        knowledgeObject.publish(
                version,
                command.validFrom(),
                command.validUntil(),
                command.actor()
        );

        KnowledgeObject saved =
                knowledgeObjectRepository.saveAndFlush(
                        knowledgeObject
                );

        eventPublisher.publishEvent(
                createEvent(
                        saved,
                        previousStatus
                )
        );

        return createResult(
                saved,
                previousStatus
        );
    }

    private KnowledgeObject resolveKnowledgeObject(
            Long knowledgeObjectId,
            Long storeId
    ) {
        return knowledgeObjectRepository
                .findByIdAndStoreId(
                        knowledgeObjectId,
                        storeId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "KnowledgeObject no encontrado dentro de la Store. "
                                        + "knowledgeObjectId="
                                        + knowledgeObjectId
                                        + ", storeId="
                                        + storeId
                        )
                );
    }

    private KnowledgeObjectVersion resolveVersion(
            Long knowledgeVersionId,
            Long storeId
    ) {
        return versionRepository
                .findDetailedByIdAndKnowledgeObjectStoreId(
                        knowledgeVersionId,
                        storeId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "KnowledgeObjectVersion no encontrada dentro "
                                        + "de la Store. knowledgeVersionId="
                                        + knowledgeVersionId
                                        + ", storeId="
                                        + storeId
                        )
                );
    }

    private static void validateApprovedStatus(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject.getStatus()
                != KnowledgeStatus.APPROVED) {

            throw new IllegalStateException(
                    "Solo un KnowledgeObject en estado APPROVED "
                            + "puede publicarse. Estado actual: "
                            + knowledgeObject.getStatus()
            );
        }
    }

    private static void validateVersionBelongsToObject(
            KnowledgeObjectVersion version,
            KnowledgeObject knowledgeObject
    ) {
        if (!version.belongsTo(knowledgeObject)) {
            throw new IllegalArgumentException(
                    "La versión seleccionada no pertenece "
                            + "al KnowledgeObject"
            );
        }
    }

    private KnowledgePublishedEvent createEvent(
            KnowledgeObject saved,
            KnowledgeStatus previousStatus
    ) {
        KnowledgeObjectVersion currentVersion =
                requireCurrentVersion(saved);

        return new KnowledgePublishedEvent(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                currentVersion.getId(),
                currentVersion.getSemanticVersion().toString(),
                previousStatus,
                saved.getStatus(),
                saved.getValidFrom(),
                saved.getValidUntil(),
                saved.getUpdatedBy(),
                saved.getUpdatedAt()
        );
    }

    private PublishKnowledgeResult createResult(
            KnowledgeObject saved,
            KnowledgeStatus previousStatus
    ) {
        KnowledgeObjectVersion currentVersion =
                requireCurrentVersion(saved);

        return new PublishKnowledgeResult(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                currentVersion.getId(),
                currentVersion.getSemanticVersion().toString(),
                previousStatus,
                saved.getStatus(),
                saved.getValidFrom(),
                saved.getValidUntil(),
                saved.getUpdatedBy(),
                saved.getUpdatedAt(),
                saved.getLockVersion()
        );
    }

    private static KnowledgeObjectVersion requireCurrentVersion(
            KnowledgeObject knowledgeObject
    ) {
        KnowledgeObjectVersion currentVersion =
                knowledgeObject.getCurrentVersion();

        if (currentVersion == null) {
            throw new IllegalStateException(
                    "KnowledgeObject publicado no tiene versión vigente"
            );
        }

        return currentVersion;
    }
}