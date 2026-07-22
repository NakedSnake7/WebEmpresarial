package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.CreateKnowledgeObjectCommand;
import com.webempresarial.store.knowledge.application.result.CreateKnowledgeObjectResult;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.event.KnowledgeObjectCreatedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateKnowledgeObjectServiceTest {

    private static final Long STORE_ID = 15L;
    private static final Long KNOWLEDGE_OBJECT_ID = 100L;
    private static final String ACTOR =
            "admin@webempresarial.com";

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private KnowledgeObject savedKnowledgeObject;

    private CreateKnowledgeObjectService service;

    @BeforeEach
    void setUp() {
        service = new CreateKnowledgeObjectService(
                storeRepository,
                knowledgeObjectRepository,
                eventPublisher
        );
    }

    @Test
    void shouldCreateKnowledgeObjectAndPublishEvent() {
        Store store = activeStore(STORE_ID);

        KnowledgeContextRoot contextRoot =
                KnowledgeContextRoot.of(
                        KnowledgeContextType.PROJECT,
                        "ROBERT-SLINGERLAND"
                );

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        21,
                        30
                );

        when(storeRepository.findById(STORE_ID))
                .thenReturn(Optional.of(store));

        when(
                knowledgeObjectRepository
                        .existsByStoreIdAndCodeValue(
                                STORE_ID,
                                "KS-100"
                        )
        ).thenReturn(false);

        configureSavedKnowledgeObject(
                store,
                contextRoot,
                createdAt
        );

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        CreateKnowledgeObjectResult result =
                service.execute(projectCommand());

        assertEquals(
                KNOWLEDGE_OBJECT_ID,
                result.id()
        );

        assertEquals(
                STORE_ID,
                result.storeId()
        );

        assertEquals(
                "KS-100",
                result.code()
        );

        assertEquals(
                KnowledgeStatus.DRAFT,
                result.status()
        );

        assertEquals(
                KnowledgeContextType.PROJECT,
                result.contextType()
        );

        assertEquals(
                "ROBERT-SLINGERLAND",
                result.contextReference()
        );

        assertEquals(
                ACTOR,
                result.createdBy()
        );

        assertEquals(
                createdAt,
                result.createdAt()
        );

        assertEquals(
                0L,
                result.lockVersion()
        );

        verify(
                knowledgeObjectRepository
        ).saveAndFlush(
                any(KnowledgeObject.class)
        );

        ArgumentCaptor<KnowledgeObjectCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObjectCreatedEvent.class
                );

        verify(eventPublisher).publishEvent(
                eventCaptor.capture()
        );

        KnowledgeObjectCreatedEvent event =
                eventCaptor.getValue();

        assertEquals(
                KNOWLEDGE_OBJECT_ID,
                event.knowledgeObjectId()
        );

        assertEquals(
                STORE_ID,
                event.storeId()
        );

        assertEquals(
                "KS-100",
                event.code()
        );

        assertEquals(
                KnowledgeStatus.DRAFT,
                event.status()
        );

        assertEquals(
                KnowledgeContextType.PROJECT,
                event.contextType()
        );

        assertEquals(
                "ROBERT-SLINGERLAND",
                event.contextReference()
        );

        assertEquals(
                ACTOR,
                event.actor()
        );

        assertEquals(
                createdAt,
                event.occurredAt()
        );
    }

    @Test
    void shouldDeriveStoreContextFromStoreId() {
        Store store = activeStore(STORE_ID);

        KnowledgeContextRoot persistedContext =
                KnowledgeContextRoot.store(STORE_ID);

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        21,
                        45
                );

        when(storeRepository.findById(STORE_ID))
                .thenReturn(Optional.of(store));

        when(
                knowledgeObjectRepository
                        .existsByStoreIdAndCodeValue(
                                STORE_ID,
                                "KS-100"
                        )
        ).thenReturn(false);

        configureSavedKnowledgeObject(
                store,
                persistedContext,
                createdAt
        );

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        /*
         * La referencia manipulada "9999" debe ignorarse.
         * El servicio debe construir STORE:15.
         */
        CreateKnowledgeObjectCommand command =
                new CreateKnowledgeObjectCommand(
                        STORE_ID,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.STORE,
                        "9999",
                        ACTOR
                );

        CreateKnowledgeObjectResult result =
                service.execute(command);

        ArgumentCaptor<KnowledgeObject> objectCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObject.class
                );

        verify(knowledgeObjectRepository)
                .saveAndFlush(objectCaptor.capture());

        KnowledgeObject objectToPersist =
                objectCaptor.getValue();

        assertNotNull(objectToPersist.getContextRoot());

        assertEquals(
                KnowledgeContextType.STORE,
                objectToPersist.getContextRoot().getType()
        );

        assertEquals(
                STORE_ID.toString(),
                objectToPersist
                        .getContextRoot()
                        .getReference()
        );

        assertTrue(
                objectToPersist
                        .getContextRoot()
                        .belongsToStore(STORE_ID)
        );

        assertEquals(
                STORE_ID.toString(),
                result.contextReference()
        );
    }

    @Test
    void shouldUseCanonicalPlatformContext() {
        Store store = activeStore(STORE_ID);

        KnowledgeContextRoot persistedContext =
                KnowledgeContextRoot.platform();

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        22,
                        0
                );

        when(storeRepository.findById(STORE_ID))
                .thenReturn(Optional.of(store));

        when(
                knowledgeObjectRepository
                        .existsByStoreIdAndCodeValue(
                                STORE_ID,
                                "KS-100"
                        )
        ).thenReturn(false);

        configureSavedKnowledgeObject(
                store,
                persistedContext,
                createdAt
        );

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        CreateKnowledgeObjectCommand command =
                new CreateKnowledgeObjectCommand(
                        STORE_ID,
                        "KS-100",
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextType.PLATFORM,
                        "REFERENCIA-MANIPULADA",
                        ACTOR
                );

        CreateKnowledgeObjectResult result =
                service.execute(command);

        ArgumentCaptor<KnowledgeObject> objectCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObject.class
                );

        verify(knowledgeObjectRepository)
                .saveAndFlush(objectCaptor.capture());

        KnowledgeContextRoot contextRoot =
                objectCaptor
                        .getValue()
                        .getContextRoot();

        assertEquals(
                KnowledgeContextType.PLATFORM,
                contextRoot.getType()
        );

        assertEquals(
                KnowledgeContextRoot.PLATFORM_REFERENCE,
                contextRoot.getReference()
        );

        assertEquals(
                KnowledgeContextRoot.PLATFORM_REFERENCE,
                result.contextReference()
        );
    }

    @Test
    void shouldRejectMissingStore() {
        when(storeRepository.findById(STORE_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                projectCommand()
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(STORE_ID.toString())
        );

        verifyNoInteractions(
                knowledgeObjectRepository,
                eventPublisher
        );
    }

    @Test
    void shouldRejectInactiveStore() {
        Store store = activeStore(STORE_ID);
        store.setActiva(false);

        when(storeRepository.findById(STORE_ID))
                .thenReturn(Optional.of(store));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.execute(
                                projectCommand()
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("inactiva")
        );

        verifyNoInteractions(
                knowledgeObjectRepository,
                eventPublisher
        );
    }

    @Test
    void shouldRejectDuplicatedCodeInsideStore() {
        Store store = activeStore(STORE_ID);

        when(storeRepository.findById(STORE_ID))
                .thenReturn(Optional.of(store));

        when(
                knowledgeObjectRepository
                        .existsByStoreIdAndCodeValue(
                                STORE_ID,
                                "KS-100"
                        )
        ).thenReturn(true);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.execute(
                                projectCommand()
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("KS-100")
        );

        assertTrue(
                exception.getMessage()
                        .contains(STORE_ID.toString())
        );

        verify(
                knowledgeObjectRepository,
                never()
        ).saveAndFlush(
                any(KnowledgeObject.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectNullCommand() {
        assertThrows(
                NullPointerException.class,
                () -> service.execute(null)
        );

        verifyNoInteractions(
                storeRepository,
                knowledgeObjectRepository,
                eventPublisher
        );
    }

    private void configureSavedKnowledgeObject(
            Store store,
            KnowledgeContextRoot contextRoot,
            LocalDateTime createdAt
    ) {
        when(savedKnowledgeObject.getId())
                .thenReturn(KNOWLEDGE_OBJECT_ID);

        when(savedKnowledgeObject.getStore())
                .thenReturn(store);

        when(savedKnowledgeObject.getCode())
                .thenReturn(
                        KnowledgeCode.of("KS-100")
                );

        when(savedKnowledgeObject.getTypeCode())
                .thenReturn(firstTypeCode());

        when(savedKnowledgeObject.getDomain())
                .thenReturn(firstDomain());

        when(savedKnowledgeObject.getClassification())
                .thenReturn(firstClassification());

        when(savedKnowledgeObject.getRiskLevel())
                .thenReturn(firstRiskLevel());

        when(savedKnowledgeObject.getStatus())
                .thenReturn(KnowledgeStatus.DRAFT);

        when(savedKnowledgeObject.getContextRoot())
                .thenReturn(contextRoot);

        when(savedKnowledgeObject.getCreatedBy())
                .thenReturn(ACTOR);

        when(savedKnowledgeObject.getCreatedAt())
                .thenReturn(createdAt);

        when(savedKnowledgeObject.getLockVersion())
                .thenReturn(0L);
    }

    private static CreateKnowledgeObjectCommand projectCommand() {
        return new CreateKnowledgeObjectCommand(
                STORE_ID,
                "KS-100",
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeContextType.PROJECT,
                "ROBERT-SLINGERLAND",
                ACTOR
        );
    }

    private static Store activeStore(Long id) {
        Store store = new Store();

        store.setId(id);
        store.setNombre("WebEmpresarial Test");
        store.setDominio("test.web-empresarial.local");
        store.setActiva(true);

        return store;
    }

    private static KnowledgeTypeCode firstTypeCode() {
        return KnowledgeTypeCode.values()[0];
    }

    private static KnowledgeDomain firstDomain() {
        return KnowledgeDomain.values()[0];
    }

    private static KnowledgeClassification firstClassification() {
        return KnowledgeClassification.values()[0];
    }

    private static KnowledgeRiskLevel firstRiskLevel() {
        return KnowledgeRiskLevel.values()[0];
    }
}