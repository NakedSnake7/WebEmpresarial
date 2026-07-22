package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.CreateKnowledgeVersionCommand;
import com.webempresarial.store.knowledge.application.result.CreateKnowledgeVersionResult;
import com.webempresarial.store.knowledge.application.usecase.CreateKnowledgeVersionUseCase;
import com.webempresarial.store.knowledge.domain.event.KnowledgeVersionCreatedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso responsable de crear una nueva versión
 * dentro de un KnowledgeObject existente.
 */
@Service
public class CreateKnowledgeVersionService
        implements CreateKnowledgeVersionUseCase {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final KnowledgeObjectVersionRepository versionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateKnowledgeVersionService(
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
    public CreateKnowledgeVersionResult execute(
            CreateKnowledgeVersionCommand command
    ) {
        Objects.requireNonNull(
                command,
                "CreateKnowledgeVersionCommand es obligatorio"
        );

        KnowledgeObject knowledgeObject =
                resolveKnowledgeObject(
                        command.knowledgeObjectId(),
                        command.storeId()
                );

        ensureEditable(knowledgeObject);

        SemanticVersion semanticVersion =
                SemanticVersion.parse(
                        command.semanticVersion()
                );

        KnowledgeConfidence confidence =
                KnowledgeConfidence.of(
                        command.confidence()
                );

        validateVersionDoesNotExist(
                command,
                semanticVersion
        );

        validateVersionIsNewer(
                command,
                semanticVersion
        );

        KnowledgeObjectVersion version =
                knowledgeObject.createVersion(
                        semanticVersion,
                        command.title(),
                        command.summary(),
                        command.content(),
                        command.contentFormat(),
                        confidence,
                        command.sourceReference(),
                        command.actor()
                );

        KnowledgeObjectVersion saved =
                versionRepository.saveAndFlush(version);

        eventPublisher.publishEvent(
                createEvent(saved)
        );

        return createResult(saved);
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

    private static void ensureEditable(
            KnowledgeObject knowledgeObject
    ) {
        if (!knowledgeObject.isEditable()) {
            throw new IllegalStateException(
                    "No se pueden crear versiones cuando "
                            + "KnowledgeObject se encuentra en estado "
                            + knowledgeObject.getStatus()
            );
        }
    }

    private void validateVersionDoesNotExist(
            CreateKnowledgeVersionCommand command,
            SemanticVersion semanticVersion
    ) {
        boolean alreadyExists =
                versionRepository
                        .existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
                                command.knowledgeObjectId(),
                                command.storeId(),
                                semanticVersion.getMajor(),
                                semanticVersion.getMinor(),
                                semanticVersion.getPatch()
                        );

        if (alreadyExists) {
            throw new IllegalStateException(
                    "La versión "
                            + semanticVersion
                            + " ya existe para KnowledgeObject "
                            + command.knowledgeObjectId()
                            + " dentro de la Store "
                            + command.storeId()
            );
        }
    }

    private void validateVersionIsNewer(
            CreateKnowledgeVersionCommand command,
            SemanticVersion candidateVersion
    ) {
        Optional<KnowledgeObjectVersion> latestVersion =
                versionRepository
                        .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                command.knowledgeObjectId(),
                                command.storeId()
                        );

        if (latestVersion.isEmpty()) {
            validateInitialVersion(candidateVersion);
            return;
        }

        SemanticVersion currentLatest =
                latestVersion
                        .get()
                        .getSemanticVersion();

        if (!candidateVersion.isNewerThan(currentLatest)) {
            throw new IllegalArgumentException(
                    "La nueva versión "
                            + candidateVersion
                            + " debe ser posterior a la versión más reciente "
                            + currentLatest
            );
        }
    }

    /**
     * La primera versión formal del objeto debe comenzar en 1.0.0.
     */
    private static void validateInitialVersion(
            SemanticVersion candidateVersion
    ) {
        SemanticVersion initial =
                SemanticVersion.initial();

        if (!candidateVersion.equals(initial)) {
            throw new IllegalArgumentException(
                    "La primera versión de un KnowledgeObject debe ser "
                            + initial
                            + ". Se recibió "
                            + candidateVersion
            );
        }
    }

    private KnowledgeVersionCreatedEvent createEvent(
            KnowledgeObjectVersion saved
    ) {
        return new KnowledgeVersionCreatedEvent(
                saved.getId(),
                saved.getKnowledgeObject().getId(),
                saved.getKnowledgeObject().getStore().getId(),
                saved.getSemanticVersion().toString(),
                saved.getTitle(),
                saved.getContentFormat(),
                saved.getConfidence().getValue(),
                saved.getCreatedBy(),
                saved.getCreatedAt()
        );
    }

    private CreateKnowledgeVersionResult createResult(
            KnowledgeObjectVersion saved
    ) {
        return new CreateKnowledgeVersionResult(
                saved.getId(),
                saved.getKnowledgeObject().getId(),
                saved.getKnowledgeObject().getStore().getId(),
                saved.getSemanticVersion().toString(),
                saved.getTitle(),
                saved.getSummary(),
                saved.getContentFormat(),
                saved.getConfidence().getValue(),
                saved.getSourceReference(),
                saved.getCreatedBy(),
                saved.getCreatedAt(),
                saved.getLockVersion()
        );
    }
}