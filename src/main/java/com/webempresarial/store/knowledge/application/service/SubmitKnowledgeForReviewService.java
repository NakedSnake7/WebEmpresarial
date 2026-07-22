package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.SubmitKnowledgeForReviewCommand;
import com.webempresarial.store.knowledge.application.result.SubmitKnowledgeForReviewResult;
import com.webempresarial.store.knowledge.application.usecase.SubmitKnowledgeForReviewUseCase;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.event.KnowledgeSubmittedForReviewEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
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
 * DRAFT -> IN_REVIEW
 * </pre>
 */
@Service
public class SubmitKnowledgeForReviewService
        implements SubmitKnowledgeForReviewUseCase {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubmitKnowledgeForReviewService(
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
    public SubmitKnowledgeForReviewResult execute(
            SubmitKnowledgeForReviewCommand command
    ) {
        Objects.requireNonNull(
                command,
                "SubmitKnowledgeForReviewCommand es obligatorio"
        );

        KnowledgeObject knowledgeObject =
                resolveKnowledgeObject(
                        command.knowledgeObjectId(),
                        command.storeId()
                );

        validateDraftStatus(knowledgeObject);

        long versionCount =
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                command.knowledgeObjectId(),
                                command.storeId()
                        );

        validateHasVersions(versionCount);

        KnowledgeStatus previousStatus =
                knowledgeObject.getStatus();

        knowledgeObject.submitForReview(
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
                        versionCount
                )
        );

        return createResult(
                saved,
                previousStatus,
                versionCount
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

    private static void validateDraftStatus(
            KnowledgeObject knowledgeObject
    ) {
        if (knowledgeObject.getStatus()
                != KnowledgeStatus.DRAFT) {

            throw new IllegalStateException(
                    "Solo un KnowledgeObject en estado DRAFT "
                            + "puede enviarse a revisión. Estado actual: "
                            + knowledgeObject.getStatus()
            );
        }
    }

    private static void validateHasVersions(
            long versionCount
    ) {
        if (versionCount <= 0) {
            throw new IllegalStateException(
                    "KnowledgeObject no puede enviarse a revisión "
                            + "porque todavía no tiene versiones"
            );
        }
    }

    private KnowledgeSubmittedForReviewEvent createEvent(
            KnowledgeObject saved,
            KnowledgeStatus previousStatus,
            long versionCount
    ) {
        return new KnowledgeSubmittedForReviewEvent(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                previousStatus,
                saved.getStatus(),
                versionCount,
                saved.getUpdatedBy(),
                saved.getUpdatedAt()
        );
    }

    private SubmitKnowledgeForReviewResult createResult(
            KnowledgeObject saved,
            KnowledgeStatus previousStatus,
            long versionCount
    ) {
        return new SubmitKnowledgeForReviewResult(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                previousStatus,
                saved.getStatus(),
                versionCount,
                saved.getUpdatedBy(),
                saved.getUpdatedAt(),
                saved.getLockVersion()
        );
    }
}