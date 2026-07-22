package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.CreateKnowledgeVersionCommand;
import com.webempresarial.store.knowledge.application.result.CreateKnowledgeVersionResult;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.event.KnowledgeVersionCreatedEvent;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateKnowledgeVersionServiceTest {

    private static final Long STORE_ID = 15L;
    private static final Long KNOWLEDGE_OBJECT_ID = 100L;
    private static final Long VERSION_ID = 200L;
    private static final String ACTOR =
            "admin@webempresarial.com";

    @Mock
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Mock
    private KnowledgeObjectVersionRepository versionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private KnowledgeObjectVersion savedVersion;

    private CreateKnowledgeVersionService service;

    @BeforeEach
    void setUp() {
        service = new CreateKnowledgeVersionService(
                knowledgeObjectRepository,
                versionRepository,
                eventPublisher
        );
    }

    @Test
    void shouldCreateInitialVersionAndPublishEvent() {
        Store store = activeStore();
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        23,
                        0
                );

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID,
                                1,
                                0,
                                0
                        )
        ).thenReturn(false);

        when(
                versionRepository
                        .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.empty());

        configureSavedVersion(
                knowledgeObject,
                SemanticVersion.initial(),
                createdAt
        );

        when(
                versionRepository.saveAndFlush(
                        any(KnowledgeObjectVersion.class)
                )
        ).thenReturn(savedVersion);

        CreateKnowledgeVersionResult result =
                service.execute(initialCommand());

        assertEquals(VERSION_ID, result.id());
        assertEquals(
                KNOWLEDGE_OBJECT_ID,
                result.knowledgeObjectId()
        );
        assertEquals(STORE_ID, result.storeId());
        assertEquals("1.0.0", result.semanticVersion());
        assertEquals("MARKDOWN", result.contentFormat());
        assertEquals(
                new BigDecimal("0.9500"),
                result.confidence()
        );
        assertEquals(ACTOR, result.createdBy());
        assertEquals(createdAt, result.createdAt());
        assertEquals(0L, result.lockVersion());

        ArgumentCaptor<KnowledgeObjectVersion> versionCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObjectVersion.class
                );

        verify(versionRepository)
                .saveAndFlush(versionCaptor.capture());

        KnowledgeObjectVersion versionToPersist =
                versionCaptor.getValue();

        assertEquals(
                SemanticVersion.initial(),
                versionToPersist.getSemanticVersion()
        );

        assertEquals(
                knowledgeObject,
                versionToPersist.getKnowledgeObject()
        );

        ArgumentCaptor<KnowledgeVersionCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeVersionCreatedEvent.class
                );

        verify(eventPublisher).publishEvent(
                eventCaptor.capture()
        );

        KnowledgeVersionCreatedEvent event =
                eventCaptor.getValue();

        assertEquals(VERSION_ID, event.knowledgeVersionId());
        assertEquals(
                KNOWLEDGE_OBJECT_ID,
                event.knowledgeObjectId()
        );
        assertEquals(STORE_ID, event.storeId());
        assertEquals("1.0.0", event.semanticVersion());
        assertEquals(ACTOR, event.actor());
        assertEquals(createdAt, event.occurredAt());
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
                        () -> service.execute(initialCommand())
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
    void shouldRejectDuplicatedSemanticVersion() {
        Store store = activeStore();
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID,
                                1,
                                0,
                                0
                        )
        ).thenReturn(true);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.execute(initialCommand())
                );

        assertTrue(
                exception.getMessage()
                        .contains("1.0.0")
        );

        verify(
                versionRepository,
                never()
        ).saveAndFlush(
                any(KnowledgeObjectVersion.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectInitialVersionDifferentFromOneZeroZero() {
        Store store = activeStore();
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID,
                                1,
                                1,
                                0
                        )
        ).thenReturn(false);

        when(
                versionRepository
                        .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.empty());

        CreateKnowledgeVersionCommand command =
                commandForVersion("1.1.0");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(command)
                );

        assertTrue(
                exception.getMessage()
                        .contains("1.0.0")
        );

        verify(
                versionRepository,
                never()
        ).saveAndFlush(
                any(KnowledgeObjectVersion.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectVersionOlderThanLatestVersion() {
        Store store = activeStore();
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        KnowledgeObjectVersion latestVersion =
                knowledgeObject.createVersion(
                        SemanticVersion.of(2, 0, 0),
                        "Latest",
                        "Latest summary",
                        "Latest content",
                        "MARKDOWN",
                        KnowledgeConfidence.full(),
                        null,
                        ACTOR
                );

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .existsByKnowledgeObjectIdAndKnowledgeObjectStoreIdAndSemanticVersionMajorAndSemanticVersionMinorAndSemanticVersionPatch(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID,
                                1,
                                5,
                                0
                        )
        ).thenReturn(false);

        when(
                versionRepository
                        .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.of(latestVersion));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                commandForVersion("1.5.0")
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("2.0.0")
        );

        verify(
                versionRepository,
                never()
        ).saveAndFlush(
                any(KnowledgeObjectVersion.class)
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectInvalidConfidence() {
        Store store = activeStore();
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        CreateKnowledgeVersionCommand command =
                new CreateKnowledgeVersionCommand(
                        STORE_ID,
                        KNOWLEDGE_OBJECT_ID,
                        "1.0.0",
                        "Title",
                        "Summary",
                        "Content",
                        "MARKDOWN",
                        new BigDecimal("1.5000"),
                        null,
                        ACTOR
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(command)
        );

        verifyNoInteractions(
                versionRepository,
                eventPublisher
        );
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

    private void configureSavedVersion(
            KnowledgeObject knowledgeObject,
            SemanticVersion semanticVersion,
            LocalDateTime createdAt
    ) {
        when(savedVersion.getId())
                .thenReturn(VERSION_ID);

        when(savedVersion.getKnowledgeObject())
                .thenReturn(knowledgeObject);

        when(savedVersion.getSemanticVersion())
                .thenReturn(semanticVersion);

        when(savedVersion.getTitle())
                .thenReturn(
                        "Knowledge Objects Specification"
                );

        when(savedVersion.getSummary())
                .thenReturn(
                        "Specification summary"
                );

        when(savedVersion.getContentFormat())
                .thenReturn("MARKDOWN");

        when(savedVersion.getConfidence())
                .thenReturn(
                        KnowledgeConfidence.of(
                                new BigDecimal("0.9500")
                        )
                );

        when(savedVersion.getSourceReference())
                .thenReturn(null);

        when(savedVersion.getCreatedBy())
                .thenReturn(ACTOR);

        when(savedVersion.getCreatedAt())
                .thenReturn(createdAt);

        when(savedVersion.getLockVersion())
                .thenReturn(0L);
    }

    private static CreateKnowledgeVersionCommand initialCommand() {
        return commandForVersion("1.0.0");
    }

    private static CreateKnowledgeVersionCommand commandForVersion(
            String version
    ) {
        return new CreateKnowledgeVersionCommand(
                STORE_ID,
                KNOWLEDGE_OBJECT_ID,
                version,
                "Knowledge Objects Specification",
                "Specification summary",
                "# Content",
                "MARKDOWN",
                new BigDecimal("0.9500"),
                null,
                ACTOR
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