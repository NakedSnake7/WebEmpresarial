package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.ArchiveKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.ArchiveKnowledgeResult;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.event.KnowledgeArchivedEvent;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveKnowledgeServiceTest {

    private static final Long STORE_ID = 15L;
    private static final Long KNOWLEDGE_OBJECT_ID = 100L;
    private static final Long KNOWLEDGE_VERSION_ID = 200L;

    private static final String ACTOR =
            "archiver@webempresarial.com";

    private static final String REASON =
            "Contenido temporalmente fuera de vigencia";

    private static final LocalDateTime VALID_FROM =
            LocalDateTime.of(
                    2026,
                    7,
                    22,
                    8,
                    0
            );

    private static final LocalDateTime UPDATED_AT =
            LocalDateTime.of(
                    2026,
                    7,
                    22,
                    10,
                    30
            );

    @Mock
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private KnowledgeObject savedKnowledgeObject;

    @Mock
    private KnowledgeObjectVersion savedCurrentVersion;

    private ArchiveKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new ArchiveKnowledgeService(
                knowledgeObjectRepository,
                eventPublisher
        );
    }

    @Test
    void shouldArchivePublishedKnowledgeObject()
            throws Exception {

        PublishedKnowledgeFixture fixture =
                publishedKnowledgeFixture();

        KnowledgeObject knowledgeObject =
                fixture.knowledgeObject();

        KnowledgeObjectVersion currentVersion =
                fixture.version();

        when(
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.of(knowledgeObject));

        configureSavedKnowledgeObject();

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        ArchiveKnowledgeResult result =
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
                KnowledgeStatus.PUBLISHED,
                result.previousStatus()
        );

        assertEquals(
                KnowledgeStatus.ARCHIVED,
                result.currentStatus()
        );

        assertEquals(REASON, result.reason());
        assertEquals(ACTOR, result.updatedBy());
        assertEquals(UPDATED_AT, result.updatedAt());
        assertEquals(4L, result.lockVersion());

        ArgumentCaptor<KnowledgeObject> objectCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObject.class
                );

        verify(knowledgeObjectRepository)
                .saveAndFlush(objectCaptor.capture());

        KnowledgeObject archivedObject =
                objectCaptor.getValue();

        assertEquals(
                KnowledgeStatus.ARCHIVED,
                archivedObject.getStatus()
        );

        assertSame(
                currentVersion,
                archivedObject.getCurrentVersion()
        );

        assertEquals(
                VALID_FROM,
                archivedObject.getValidFrom()
        );

        ArgumentCaptor<KnowledgeArchivedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeArchivedEvent.class
                );

        verify(eventPublisher).publishEvent(
                eventCaptor.capture()
        );

        KnowledgeArchivedEvent event =
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
                event.currentVersionId()
        );

        assertEquals(
                "1.0.0",
                event.currentSemanticVersion()
        );

        assertEquals(
                KnowledgeStatus.PUBLISHED,
                event.previousStatus()
        );

        assertEquals(
                KnowledgeStatus.ARCHIVED,
                event.currentStatus()
        );

        assertEquals(REASON, event.reason());
        assertEquals(ACTOR, event.actor());
        assertEquals(UPDATED_AT, event.occurredAt());
    }

    @Test
    void shouldRejectMissingKnowledgeObjectInsideStore() {
        when(
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
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

        verify(
                knowledgeObjectRepository,
                never()
        ).saveAndFlush(
                any(KnowledgeObject.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectKnowledgeObjectOutsidePublishedStatus() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        when(
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
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
                        .contains("PUBLISHED")
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
    void shouldRejectPublishedObjectWithoutCurrentVersion()
            throws Exception {

        KnowledgeObject knowledgeObject =
                publishedKnowledgeFixture()
                        .knowledgeObject();

        setField(
                knowledgeObject,
                "currentVersion",
                null
        );

        when(
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
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
                        .contains("versión vigente")
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
    void shouldRejectCurrentVersionFromAnotherObject()
            throws Exception {

        PublishedKnowledgeFixture fixture =
                publishedKnowledgeFixture();

        KnowledgeObject knowledgeObject =
                fixture.knowledgeObject();

        Store store = activeStore();

        KnowledgeObject foreignObject =
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
                foreignObject.createVersion(
                        SemanticVersion.initial(),
                        "Foreign knowledge",
                        "Foreign summary",
                        "Foreign content",
                        "MARKDOWN",
                        KnowledgeConfidence.full(),
                        null,
                        ACTOR
                );

        setId(foreignVersion, 300L);

        setField(
                knowledgeObject,
                "currentVersion",
                foreignVersion
        );

        when(
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
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
                eventPublisher
        );
    }

    private void configureSavedKnowledgeObject() {
        Store store = activeStore();

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
                        KnowledgeStatus.ARCHIVED
                );

        when(savedKnowledgeObject.getUpdatedBy())
                .thenReturn(ACTOR);

        when(savedKnowledgeObject.getUpdatedAt())
                .thenReturn(UPDATED_AT);

        when(savedKnowledgeObject.getLockVersion())
                .thenReturn(4L);

        when(savedKnowledgeObject.getCurrentVersion())
                .thenReturn(savedCurrentVersion);

        when(savedCurrentVersion.getId())
                .thenReturn(KNOWLEDGE_VERSION_ID);

        when(savedCurrentVersion.getSemanticVersion())
                .thenReturn(
                        SemanticVersion.initial()
                );

        /*
         * Como savedCurrentVersion es un mock, Mockito no ejecuta
         * automáticamente la implementación real de belongsTo().
         */
        when(
                savedCurrentVersion.belongsTo(
                        savedKnowledgeObject
                )
        ).thenReturn(true);
    }

    private static ArchiveKnowledgeCommand command() {
        return new ArchiveKnowledgeCommand(
                STORE_ID,
                KNOWLEDGE_OBJECT_ID,
                REASON,
                ACTOR
        );
    }

    private static PublishedKnowledgeFixture
    publishedKnowledgeFixture()
            throws Exception {

        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        KnowledgeObjectVersion version =
                knowledgeObject.createVersion(
                        SemanticVersion.initial(),
                        "Knowledge Objects Specification",
                        "Specification summary",
                        "# Knowledge Objects",
                        "MARKDOWN",
                        KnowledgeConfidence.full(),
                        null,
                        ACTOR
                );

        setId(
                knowledgeObject,
                KNOWLEDGE_OBJECT_ID
        );

        setId(
                version,
                KNOWLEDGE_VERSION_ID
        );

        knowledgeObject.submitForReview(
                "reviewer@webempresarial.com"
        );

        knowledgeObject.approve(
                "approver@webempresarial.com"
        );

        knowledgeObject.publish(
                version,
                VALID_FROM,
                null,
                "publisher@webempresarial.com"
        );

        return new PublishedKnowledgeFixture(
                knowledgeObject,
                version
        );
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

    private static Store activeStore() {
        Store store = new Store();

        store.setId(STORE_ID);
        store.setNombre("WebEmpresarial Test");
        store.setDominio(
                "test.web-empresarial.local"
        );
        store.setActiva(true);

        return store;
    }

    private static void setId(
            Object target,
            Long id
    ) throws Exception {
        setField(target, "id", id);
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) throws Exception {
        Field field =
                target.getClass()
                        .getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(target, value);
    }

    private static KnowledgeTypeCode firstTypeCode() {
        return KnowledgeTypeCode.values()[0];
    }

    private static KnowledgeDomain firstDomain() {
        return KnowledgeDomain.values()[0];
    }

    private static KnowledgeClassification
    firstClassification() {
        return KnowledgeClassification.values()[0];
    }

    private static KnowledgeRiskLevel firstRiskLevel() {
        return KnowledgeRiskLevel.values()[0];
    }

    private record PublishedKnowledgeFixture(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion version
    ) {
    }
}