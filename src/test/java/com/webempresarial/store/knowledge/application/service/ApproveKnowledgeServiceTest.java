package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.command.ApproveKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.ApproveKnowledgeResult;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.event.KnowledgeApprovedEvent;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApproveKnowledgeServiceTest {

    private static final Long STORE_ID = 15L;
    private static final Long KNOWLEDGE_OBJECT_ID = 100L;
    private static final String ACTOR =
            "approver@webempresarial.com";

    @Mock
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Mock
    private KnowledgeObjectVersionRepository versionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private KnowledgeObject savedKnowledgeObject;

    private ApproveKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new ApproveKnowledgeService(
                knowledgeObjectRepository,
                versionRepository,
                eventPublisher
        );
    }

    @Test
    void shouldApproveKnowledgeObject() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        KnowledgeObjectVersion latestVersion =
                createVersion(
                        knowledgeObject,
                        SemanticVersion.initial()
                );

        knowledgeObject.submitForReview(ACTOR);

        LocalDateTime updatedAt =
                LocalDateTime.of(
                        2026,
                        7,
                        22,
                        0,
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
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(1L);

        when(
                versionRepository
                        .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.of(latestVersion));

        configureSavedKnowledgeObject(
                store,
                updatedAt
        );

        when(
                knowledgeObjectRepository.saveAndFlush(
                        any(KnowledgeObject.class)
                )
        ).thenReturn(savedKnowledgeObject);

        ApproveKnowledgeResult result =
                service.execute(command());

        assertEquals(
                KnowledgeStatus.IN_REVIEW,
                result.previousStatus()
        );

        assertEquals(
                KnowledgeStatus.APPROVED,
                result.currentStatus()
        );

        assertEquals(1L, result.versionCount());

        assertEquals(
                "1.0.0",
                result.latestSemanticVersion()
        );

        assertEquals(ACTOR, result.updatedBy());
        assertEquals(updatedAt, result.updatedAt());

        ArgumentCaptor<KnowledgeObject> objectCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeObject.class
                );

        verify(knowledgeObjectRepository)
                .saveAndFlush(objectCaptor.capture());

        assertEquals(
                KnowledgeStatus.APPROVED,
                objectCaptor.getValue().getStatus()
        );

        ArgumentCaptor<KnowledgeApprovedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        KnowledgeApprovedEvent.class
                );

        verify(eventPublisher).publishEvent(
                eventCaptor.capture()
        );

        KnowledgeApprovedEvent event =
                eventCaptor.getValue();

        assertEquals(
                KnowledgeStatus.IN_REVIEW,
                event.previousStatus()
        );

        assertEquals(
                KnowledgeStatus.APPROVED,
                event.currentStatus()
        );

        assertEquals(
                "1.0.0",
                event.latestSemanticVersion()
        );

        assertEquals(1L, event.versionCount());
        assertEquals(ACTOR, event.actor());
    }

    @Test
    void shouldRejectKnowledgeObjectOutsideReview() {
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
                        .contains("IN_REVIEW")
        );

        verifyNoInteractions(
                versionRepository,
                eventPublisher
        );
    }

    @Test
    void shouldRejectKnowledgeObjectWithoutVersions() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                knowledgeObjectInReview(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(0L);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.execute(command())
                );

        assertTrue(
                exception.getMessage()
                        .contains("no tiene versiones")
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
    void shouldRejectWhenLatestVersionCannotBeResolved() {
        Store store = activeStore();

        KnowledgeObject knowledgeObject =
                knowledgeObjectInReview(store);

        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.of(knowledgeObject));

        when(
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(1L);

        when(
                versionRepository
                        .findFirstByKnowledgeObjectIdAndKnowledgeObjectStoreIdOrderBySemanticVersionMajorDescSemanticVersionMinorDescSemanticVersionPatchDesc(
                                KNOWLEDGE_OBJECT_ID,
                                STORE_ID
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.execute(command())
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
    void shouldRejectMissingKnowledgeObjectInsideStore() {
        when(
                knowledgeObjectRepository.findByIdAndStoreId(
                        KNOWLEDGE_OBJECT_ID,
                        STORE_ID
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(command())
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
                        KnowledgeStatus.APPROVED
                );

        when(savedKnowledgeObject.getUpdatedBy())
                .thenReturn(ACTOR);

        when(savedKnowledgeObject.getUpdatedAt())
                .thenReturn(updatedAt);

        when(savedKnowledgeObject.getLockVersion())
                .thenReturn(2L);
    }

    private static ApproveKnowledgeCommand command() {
        return new ApproveKnowledgeCommand(
                STORE_ID,
                KNOWLEDGE_OBJECT_ID,
                ACTOR
        );
    }

    private static KnowledgeObject knowledgeObjectInReview(
            Store store
    ) {
        KnowledgeObject knowledgeObject =
                draftKnowledgeObject(store);

        knowledgeObject.submitForReview(ACTOR);

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
            SemanticVersion version
    ) {
        return knowledgeObject.createVersion(
                version,
                "Knowledge Objects Specification",
                "Specification summary",
                "# Content",
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