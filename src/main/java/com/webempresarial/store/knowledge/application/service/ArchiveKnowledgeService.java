package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.ArchiveKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.ArchiveKnowledgeResult;
import com.webempresarial.store.knowledge.application.usecase.ArchiveKnowledgeUseCase;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.event.KnowledgeArchivedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de uso que gobierna la transición:
 *
 * <pre>
 * PUBLISHED -> ARCHIVED
 * </pre>
 */
@Service
public class ArchiveKnowledgeService
        implements ArchiveKnowledgeUseCase {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ArchiveKnowledgeService(
            KnowledgeObjectRepository knowledgeObjectRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.knowledgeObjectRepository =
                knowledgeObjectRepository;

        this.eventPublisher =
                eventPublisher;
    }

    @Override
    @Transactional
    public ArchiveKnowledgeResult execute(
            ArchiveKnowledgeCommand command
    ) {
        Objects.requireNonNull(
                command,
                "ArchiveKnowledgeCommand es obligatorio"
        );

        KnowledgeObject knowledgeObject =
                resolveKnowledgeObject(
                        command.knowledgeObjectId(),
                        command.storeId()
                );

        validatePublishedState(knowledgeObject);

        KnowledgeObjectVersion currentVersion =
                requireCurrentVersion(knowledgeObject);

        KnowledgeStatus previousStatus =
                knowledgeObject.getStatus();

        knowledgeObject.archive(
                command.actor()
        );

        KnowledgeObject saved =
                knowledgeObjectRepository.saveAndFlush(
                        knowledgeObject
                );

        KnowledgeObjectVersion savedCurrentVersion =
                requireCurrentVersion(saved);

        eventPublisher.publishEvent(
                createEvent(
                        saved,
                        savedCurrentVersion,
                        previousStatus,
                        command.reason()
                )
        );

        return createResult(
                saved,
                savedCurrentVersion,
                previousStatus,
                command.reason()
        );
    }

    private KnowledgeObject resolveKnowledgeObject(
            Long knowledgeObjectId,
            Long storeId
    ) {
        return knowledgeObjectRepository
                .findWithCurrentVersionByIdAndStoreId(
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

    private static void validatePublishedState(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject.getStatus()
                != KnowledgeStatus.PUBLISHED) {

            throw new IllegalStateException(
                    "Solo un KnowledgeObject en estado PUBLISHED "
                            + "puede archivarse. Estado actual: "
                            + knowledgeObject.getStatus()
            );
        }
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

        if (!currentVersion.belongsTo(knowledgeObject)) {
            throw new IllegalStateException(
                    "La versión vigente no pertenece al KnowledgeObject"
            );
        }

        return currentVersion;
    }

    private static KnowledgeArchivedEvent createEvent(
            KnowledgeObject saved,
            KnowledgeObjectVersion currentVersion,
            KnowledgeStatus previousStatus,
            String reason
    ) {
        return new KnowledgeArchivedEvent(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                currentVersion.getId(),
                currentVersion.getSemanticVersion().toString(),
                previousStatus,
                saved.getStatus(),
                reason,
                saved.getUpdatedBy(),
                saved.getUpdatedAt()
        );
    }

    private static ArchiveKnowledgeResult createResult(
            KnowledgeObject saved,
            KnowledgeObjectVersion currentVersion,
            KnowledgeStatus previousStatus,
            String reason
    ) {
        return new ArchiveKnowledgeResult(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                currentVersion.getId(),
                currentVersion.getSemanticVersion().toString(),
                previousStatus,
                saved.getStatus(),
                reason,
                saved.getUpdatedBy(),
                saved.getUpdatedAt(),
                saved.getLockVersion()
        );
    }
}