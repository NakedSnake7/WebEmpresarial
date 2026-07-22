package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.CreateKnowledgeObjectCommand;
import com.webempresarial.store.knowledge.application.result.CreateKnowledgeObjectResult;
import com.webempresarial.store.knowledge.application.usecase.CreateKnowledgeObjectUseCase;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.event.KnowledgeObjectCreatedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de uso responsable de crear un KnowledgeObject
 * dentro de una Store.
 */
@Service
public class CreateKnowledgeObjectService
        implements CreateKnowledgeObjectUseCase {

    private final StoreRepository storeRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateKnowledgeObjectService(
            StoreRepository storeRepository,
            KnowledgeObjectRepository knowledgeObjectRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.storeRepository = storeRepository;
        this.knowledgeObjectRepository =
                knowledgeObjectRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Ejecuta la creación completa dentro de una transacción:
     *
     * <ol>
     *     <li>Resuelve y valida la Store.</li>
     *     <li>Construye el KnowledgeCode.</li>
     *     <li>Comprueba unicidad tenant-safe.</li>
     *     <li>Construye el contexto funcional.</li>
     *     <li>Crea y persiste el agregado.</li>
     *     <li>Publica el evento correspondiente.</li>
     *     <li>Devuelve un resultado desacoplado de JPA.</li>
     * </ol>
     */
    @Override
    @Transactional
    public CreateKnowledgeObjectResult execute(
            CreateKnowledgeObjectCommand command
    ) {
        Objects.requireNonNull(
                command,
                "CreateKnowledgeObjectCommand es obligatorio"
        );

        Store store = resolveStore(command.storeId());

        KnowledgeCode code =
                KnowledgeCode.of(command.code());

        validateUniqueCode(
                store.getId(),
                code
        );

        KnowledgeContextRoot contextRoot =
                resolveContextRoot(command);

        KnowledgeObject knowledgeObject =
                KnowledgeObject.create(
                        store,
                        code,
                        command.typeCode(),
                        command.domain(),
                        command.classification(),
                        command.riskLevel(),
                        contextRoot,
                        command.actor()
                );

        /*
         * saveAndFlush garantiza que:
         *
         * - se asigne el ID;
         * - se ejecute @PrePersist;
         * - createdAt quede disponible;
         * - lockVersion sea inicializada;
         * - las restricciones SQL se validen antes de publicar el evento.
         */
        KnowledgeObject saved =
                knowledgeObjectRepository.saveAndFlush(
                        knowledgeObject
                );

        KnowledgeObjectCreatedEvent event =
                createEvent(saved);

        eventPublisher.publishEvent(event);

        return createResult(saved);
    }

    private Store resolveStore(Long storeId) {
        Store store = storeRepository
                .findById(storeId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Store no encontrada: "
                                        + storeId
                        )
                );

        if (!store.isActiva()) {
            throw new IllegalStateException(
                    "No se puede crear conocimiento en una Store inactiva: "
                            + storeId
            );
        }

        return store;
    }

    private void validateUniqueCode(
            Long storeId,
            KnowledgeCode code
    ) {
        boolean alreadyExists =
                knowledgeObjectRepository
                        .existsByStoreIdAndCodeValue(
                                storeId,
                                code.getValue()
                        );

        if (alreadyExists) {
            throw new IllegalStateException(
                    "Ya existe un KnowledgeObject con código "
                            + code.getValue()
                            + " dentro de la Store "
                            + storeId
            );
        }
    }

    private KnowledgeContextRoot resolveContextRoot(
            CreateKnowledgeObjectCommand command
    ) {
        if (command.contextType()
                == KnowledgeContextType.STORE) {

            /*
             * Para un contexto STORE, la referencia siempre se deriva
             * del storeId autenticado y no del valor recibido.
             */
            return KnowledgeContextRoot.store(
                    command.storeId()
            );
        }

        if (command.contextType()
                == KnowledgeContextType.PLATFORM) {

            /*
             * El contexto de plataforma utiliza una referencia
             * canónica y no confía en el texto del consumidor.
             */
            return KnowledgeContextRoot.platform();
        }

        return KnowledgeContextRoot.of(
                command.contextType(),
                command.contextReference()
        );
    }

    private KnowledgeObjectCreatedEvent createEvent(
            KnowledgeObject saved
    ) {
        return new KnowledgeObjectCreatedEvent(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                saved.getTypeCode(),
                saved.getDomain(),
                saved.getClassification(),
                saved.getRiskLevel(),
                saved.getStatus(),
                saved.getContextRoot().getType(),
                saved.getContextRoot().getReference(),
                saved.getCreatedBy(),
                saved.getCreatedAt()
        );
    }

    private CreateKnowledgeObjectResult createResult(
            KnowledgeObject saved
    ) {
        return new CreateKnowledgeObjectResult(
                saved.getId(),
                saved.getStore().getId(),
                saved.getCode().getValue(),
                saved.getTypeCode(),
                saved.getDomain(),
                saved.getClassification(),
                saved.getRiskLevel(),
                saved.getStatus(),
                saved.getContextRoot().getType(),
                saved.getContextRoot().getReference(),
                saved.getCreatedBy(),
                saved.getCreatedAt(),
                saved.getLockVersion()
        );
    }
}