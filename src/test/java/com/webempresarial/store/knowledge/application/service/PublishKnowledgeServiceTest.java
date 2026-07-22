package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.PublishKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.PublishKnowledgeResult;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.event.KnowledgePublishedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import com.webempresarial.store.model.Store;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishKnowledgeServiceTest {

    private static final Long STORE_ID = 15L;
    private static final Long KNOWLEDGE_OBJECT_ID = 100L;
    private static final Long KNOWLEDGE_VERSION_ID = 200L;

    private static final String ACTOR =
            "publisher@webempresarial.com";

    private static final LocalDateTime VALID_FROM =
            LocalDateTime.of(
                    2026,
                    7,
                    22,
                    8,
                    0
            );

    private static final LocalDateTime VALID_UNTIL =
            LocalDateTime.of(
                    2027,
                    7,
                    22,
                    8,
                    0
            );

    @Mock
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Mock
    private KnowledgeObjectVersionRepository versionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private KnowledgeObject savedKnowledgeObject;

    @Mock
    private KnowledgeObjectVersion savedCurrentVersion;

    private PublishKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new PublishKnowledgeService(
                knowledgeObjectRepository,
                versionRepository,
                eventPublisher
        );
    }

    @Test
    void shouldPublishApprovedKnowledgeObject() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        KnowledgeObjectVersion version =
                createVersion(
                        knowledgeObject,
                        SemanticVersion.initial()
                );

        knowledgeObject.submitForReview(ACTOR);
        knowledgeObject.approve(ACTOR);

        LocalDateTime updatedAt =
                LocalDateTime.of(
                        2026,
                        7,
                        22,
                        8,
                        15
                );

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .findDetailedByIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_VERSION_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.of(version));

        configureSavedKnowledgeObject(
                store,
                updatedAt
        );

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        PublishKnowledgeResult result =
                service.execute(command());

        assertEquals(
                KNOWLEDGE_OBJECT_ID,
                result.knowledgeObjectId()
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
                KNOWLEDGE_VERSION_ID,
                result.currentVersionId()
        );

        assertEquals(
                "1.0.0",
                result.currentSemanticVersion()
        );

        assertEquals(
                KnowledgeStatus.APPROVED,
                result.previousStatus()
        );

        assertEquals(
                KnowledgeStatus.PUBLISHED,
                result.currentStatus()
        );

        assertEquals(
                VALID_FROM,
                result.validFrom()
        );

        assertEquals(
                VALID_UNTIL,
                result.validUntil()
        );

        assertEquals(
                ACTOR,
                result.updatedBy()
        );

        assertEquals(
                updatedAt,
                result.updatedAt()
        );

        assertEquals(
                3L,
                result.lockVersion()
        );

        ArgumentCaptor<KnowledgeObject> objectCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObject.class
                );

        verify(knowledgeObjectRepository)
                .saveAndFlush(objectCaptor.capture());

        KnowledgeObject objectToPersist =
                objectCaptor.getValue();

        assertEquals(
                KnowledgeStatus.PUBLISHED,
                objectToPersist.getStatus()
        );

        assertEquals(
                version,
                objectToPersist.getCurrentVersion()
        );

        assertEquals(
                VALID_FROM,
                objectToPersist.getValidFrom()
        );

        assertEquals(
                VALID_UNTIL,
                objectToPersist.getValidUntil()
        );

        ArgumentCaptor<KnowledgePublishedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        KnowledgePublishedEvent.class
                );

        verify(eventPublisher).publishEvent(
                eventCaptor.capture()
        );

        KnowledgePublishedEvent event =
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
                KNOWLEDGE_VERSION_ID,
                event.knowledgeVersionId()
        );

        assertEquals(
                "1.0.0",
                event.semanticVersion()
        );

        assertEquals(
                KnowledgeStatus.APPROVED,
                event.previousStatus()
        );

        assertEquals(
                KnowledgeStatus.PUBLISHED,
                event.currentStatus()
        );

        assertEquals(
                VALID_FROM,
                event.validFrom()
        );

        assertEquals(
                VALID_UNTIL,
                event.validUntil()
        );

        assertEquals(
                ACTOR,
                event.actor()
        );

        assertEquals(
                updatedAt,
                event.occurredAt()
        );
    }

    @Test
    void shouldPublishWithoutExpirationDate() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        KnowledgeObjectVersion version =
                createVersion(
                        knowledgeObject,
                        SemanticVersion.initial()
                );

        knowledgeObject.submitForReview(ACTOR);
        knowledgeObject.approve(ACTOR);

        LocalDateTime updatedAt =
                LocalDateTime.of(
                        2026,
                        7,
                        22,
                        8,
                        30
                );

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .findDetailedByIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_VERSION_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.of(version));

        configureSavedKnowledgeObjectWithoutExpiration(
                store,
                updatedAt
        );

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        PublishKnowledgeCommand command =
                new PublishKnowledgeCommand(
                        STORE_ID,
                        KNOWLEDGE_OBJECT_ID,
                        KNOWLEDGE_VERSION_ID,
                        VALID_FROM,
                        null,
                        ACTOR
                );

        PublishKnowledgeResult result =
                service.execute(command);

        assertNull(result.validUntil());

        ArgumentCaptor<KnowledgeObject> captor =
                ArgumentCaptor.forClass(
                        KnowledgeObject.class
                );

        verify(knowledgeObjectRepository)
                .saveAndFlush(captor.capture());

        assertNull(
                captor.getValue().getValidUntil()
        );
    }

    @Test
    void shouldRejectMissingKnowledgeObjectInsideStore() {
        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(command())
                );

        assertTrue(
                exception.getMessage()
                        .contains(KNOWLEDGE_OBJECT_ID.toString())
        );

        assertTrue(
                exception.getMessage()
                        .contains(STORE_ID.toString())
        );

        verifyNoInteractions(
                versionRepository,
                eventPublisher
        );
    }

    @Test
    void shouldRejectKnowledgeObjectOutsideApprovedStatus() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.execute(command())
                );

        assertTrue(
                exception.getMessage()
                        .contains("APPROVED")
        );

        verifyNoInteractions(
                versionRepository,
                eventPublisher
        );
    }

    @Test
    void shouldRejectMissingVersionInsideStore() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                approvedKnowledgeObject(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .findDetailedByIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_VERSION_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(command())
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                KNOWLEDGE_VERSION_ID.toString()
                        )
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
    void shouldRejectVersionBelongingToAnotherKnowledgeObject() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                approvedKnowledgeObject(store);

        KnowledgeObject anotherKnowledgeObject =
                KnowledgeObject.create(
                        store,
                        KnowledgeCode.of("KS-101"),
                        firstTypeCode(),
                        firstDomain(),
                        firstClassification(),
                        firstRiskLevel(),
                        KnowledgeContextRoot.of(
                                KnowledgeContextType.PROJECT,
                                "ANOTHER-PROJECT"
                        ),
                        ACTOR
                );

        KnowledgeObjectVersion foreignVersion =
                createVersion(
                        anotherKnowledgeObject,
                        SemanticVersion.initial()
                );

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .findDetailedByIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_VERSION_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.of(foreignVersion));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(command())
                );

        assertTrue(
                exception.getMessage()
                        .contains("no pertenece")
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
                knowledgeObjectRepository,
                versionRepository,
                eventPublisher
        );
    }

    private void configureSavedKnowledgeObject(
            Store store,
            LocalDateTime updatedAt
    ) {
        when(savedKnowledgeObject.getId())
                .thenReturn(KNOWLEDGE_OBJECT_ID);

        when(savedKnowledgeObject.getStore())
                .thenReturn(store);

        when(savedKnowledgeObject.getCode())
                .thenReturn(
                        KnowledgeCode.of("KS-100")
                );

        when(savedKnowledgeObject.getStatus())
                .thenReturn(
                        KnowledgeStatus.PUBLISHED
                );

        when(savedKnowledgeObject.getCurrentVersion())
                .thenReturn(savedCurrentVersion);

        when(savedKnowledgeObject.getValidFrom())
                .thenReturn(VALID_FROM);

        when(savedKnowledgeObject.getValidUntil())
                .thenReturn(VALID_UNTIL);

        when(savedKnowledgeObject.getUpdatedBy())
                .thenReturn(ACTOR);

        when(savedKnowledgeObject.getUpdatedAt())
                .thenReturn(updatedAt);

        when(savedKnowledgeObject.getLockVersion())
                .thenReturn(3L);

        configureCurrentVersionMock();
    }

    private void configureSavedKnowledgeObjectWithoutExpiration(
            Store store,
            LocalDateTime updatedAt
    ) {
        when(savedKnowledgeObject.getId())
                .thenReturn(KNOWLEDGE_OBJECT_ID);

        when(savedKnowledgeObject.getStore())
                .thenReturn(store);

        when(savedKnowledgeObject.getCode())
                .thenReturn(
                        KnowledgeCode.of("KS-100")
                );

        when(savedKnowledgeObject.getStatus())
                .thenReturn(
                        KnowledgeStatus.PUBLISHED
                );

        when(savedKnowledgeObject.getCurrentVersion())
                .thenReturn(savedCurrentVersion);

        when(savedKnowledgeObject.getValidFrom())
                .thenReturn(VALID_FROM);

        when(savedKnowledgeObject.getValidUntil())
                .thenReturn(null);

        when(savedKnowledgeObject.getUpdatedBy())
                .thenReturn(ACTOR);

        when(savedKnowledgeObject.getUpdatedAt())
                .thenReturn(updatedAt);

        when(savedKnowledgeObject.getLockVersion())
                .thenReturn(3L);

        configureCurrentVersionMock();
    }

    private void configureCurrentVersionMock() {
        when(savedCurrentVersion.getId())
                .thenReturn(KNOWLEDGE_VERSION_ID);

        when(savedCurrentVersion.getSemanticVersion())
                .thenReturn(
                        SemanticVersion.initial()
                );
    }

    private static PublishKnowledgeCommand command() {
        return new PublishKnowledgeCommand(
                STORE_ID,
                KNOWLEDGE_OBJECT_ID,
                KNOWLEDGE_VERSION_ID,
                VALID_FROM,
                VALID_UNTIL,
                ACTOR
        );
    }

    private static KnowledgeObject approvedKnowledgeObject(
            Store store
    ) {
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        knowledgeObject.submitForReview(ACTOR);
        knowledgeObject.approve(ACTOR);

        return knowledgeObject;
    }

    private static KnowledgeObject draftKnowledgeObject(
            Store store
    ) {
        return KnowledgeObject.create(
                store,
                KnowledgeCode.of("KS-100"),
                firstTypeCode(),
                firstDomain(),
                firstClassification(),
                firstRiskLevel(),
                KnowledgeContextRoot.of(
                        KnowledgeContextType.PROJECT,
                        "ROBERT-SLINGERLAND"
                ),
                ACTOR
        );
    }

    private static KnowledgeObjectVersion createVersion(
            KnowledgeObject knowledgeObject,
            SemanticVersion semanticVersion
    ) {
        return knowledgeObject.createVersion(
                semanticVersion,
                "Knowledge Objects Specification",
                "Specification summary",
                "# Knowledge Objects",
                "MARKDOWN",
                KnowledgeConfidence.full(),
                null,
                ACTOR
        );
    }

    private static Store activeStore() {
        Store store = new Store();

        store.setId(STORE_ID);
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