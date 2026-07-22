package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.ApproveKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.ApproveKnowledgeResult;
import com.webempresarial.store.knowledge.application.usecase.ApproveKnowledgeUseCase;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.event.KnowledgeApprovedEvent;
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
 * IN_REVIEW -> APPROVED
 * </pre>
 */
@Service
public class ApproveKnowledgeService
        implements ApproveKnowledgeUseCase {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ApproveKnowledgeService(
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
    public ApproveKnowledgeResult execute(
            ApproveKnowledgeCommand command
    ) {
        Objects.requireNonNull(
                command,
                "ApproveKnowledgeCommand es obligatorio"
        );

        KnowledgeObject knowledgeObject =
                resolveKnowledgeObject(
                        command.knowledgeObjectId(),
                        command.storeId()
                );

        validateInReviewStatus(knowledgeObject);

        long versionCount =
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                command.knowledgeObjectId(),
                                command.storeId()
                        );

        validateHasVersions(versionCount);

        KnowledgeObjectVersion latestVersion =
                resolveLatestVersion(
                        command.knowledgeObjectId(),
                        command.storeId()
                );

        KnowledgeStatus previousStatus =
                knowledgeObject.getStatus();

        knowledgeObject.approve(
                command.actor()
        );

        KnowledgeObject saved =
                knowledgeObjectRepository.saveAndFlush(
                        knowledgeObject
                );

        eventPublisher.publishEvent(
                createEvent(
                        saved,
                        previousStatus,
                        versionCount,
                        latestVersion
                )
        );

        return createResult(
                saved,
                previousStatus,
                versionCount,
                latestVersion
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

    private static void validateInReviewStatus(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject.getStatus()
                != KnowledgeStatus.IN_REVIEW) {

            throw new IllegalStateException(
                    "Solo un KnowledgeObject en estado IN_REVIEW "
                            + "puede aprobarse. Estado actual: "
                            + knowledgeObject.getStatus()
            );
        }
    }

    private static void validateHasVersions(
            long versionCount
    ) {
        if (versionCount <= 0) {
            throw new IllegalStateException(
                    "KnowledgeObject no puede aprobarse "
                            + "porque no tiene versiones persistidas"
            );
        }
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
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No fue posible recuperar la versión más reciente "
                                        + "de KnowledgeObject "
                                        + knowledgeObjectId
                        )
                );
    }

    private KnowledgeApprovedEvent createEvent(
            KnowledgeObject saved,
            KnowledgeStatus previousStatus,
            long versionCount,
            KnowledgeObjectVersion latestVersion
    ) {
        return new KnowledgeApprovedEvent(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                previousStatus,
                saved.getStatus(),
                versionCount,
                latestVersion.getSemanticVersion().toString(),
                saved.getUpdatedBy(),
                saved.getUpdatedAt()
        );
    }

    private ApproveKnowledgeResult createResult(
            KnowledgeObject saved,
            KnowledgeStatus previousStatus,
            long versionCount,
            KnowledgeObjectVersion latestVersion
    ) {
        return new ApproveKnowledgeResult(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                previousStatus,
                saved.getStatus(),
                versionCount,
                latestVersion.getSemanticVersion().toString(),
                saved.getUpdatedBy(),
                saved.getUpdatedAt(),
                saved.getLockVersion()
        );
    }
}