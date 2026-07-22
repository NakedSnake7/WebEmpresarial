package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.exception.KnowledgeResolutionException;
import com.webempresarial.store.knowledge.application.result.KnowledgeSnapshot;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeResolverServiceTest {

    private static final Long STORE_ID = 15L;
    private static final Long KNOWLEDGE_OBJECT_ID = 100L;
    private static final Long KNOWLEDGE_VERSION_ID = 200L;

    private static final LocalDateTime NOW =
            LocalDateTime.of(
                    2026,
                    7,
                    22,
                    10,
                    0
            );

    @Mock
    private KnowledgeObjectRepository repository;

    private KnowledgeResolverService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-07-22T16:00:00Z"),
                ZoneId.of("America/Mexico_City")
        );

        service = new KnowledgeResolverService(
                repository,
                clock
        );
    }

    @Test
    void shouldResolvePublishedKnowledgeAtCurrentTime()
            throws Exception {

        KnowledgeObject knowledgeObject =
                publishedKnowledgeObject(
                        NOW.minusDays(1),
                        NOW.plusDays(1)
                );

        when(
                repository.findPublishedByStoreIdAndCode(
                        STORE_ID,
                        "KS-100",
                        com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus.PUBLISHED
                )
        ).thenReturn(Optional.of(knowledgeObject));

        Optional<KnowledgeSnapshot> result =
                service.resolve(
                        STORE_ID,
                        " ks-100 "
                );

        assertTrue(result.isPresent());

        KnowledgeSnapshot snapshot =
                result.get();

        assertEquals(
                KNOWLEDGE_OBJECT_ID,
                snapshot.knowledgeObjectId()
        );

        assertEquals(
                KNOWLEDGE_VERSION_ID,
                snapshot.knowledgeVersionId()
        );

        assertEquals(
                "KS-100",
                snapshot.code()
        );

        assertEquals(
                "1.0.0",
                snapshot.semanticVersion()
        );

        assertEquals(
                "# Knowledge Objects",
                snapshot.content()
        );

        assertTrue(
                snapshot.isValidAt(NOW)
        );

        verify(repository)
                .findPublishedByStoreIdAndCode(
                        STORE_ID,
                        "KS-100",
                        com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus.PUBLISHED
                );
    }

    @Test
    void shouldReturnEmptyWhenKnowledgeDoesNotExist() {
        when(
                repository.findPublishedByStoreIdAndCode(
                        STORE_ID,
                        "KS-100",
                        com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus.PUBLISHED
                )
        ).thenReturn(Optional.empty());

        Optional<KnowledgeSnapshot> result =
                service.resolveAt(
                        STORE_ID,
                        "KS-100",
                        NOW
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyBeforeValidityStarts()
            throws Exception {

        KnowledgeObject knowledgeObject =
                publishedKnowledgeObject(
                        NOW.plusHours(1),
                        NOW.plusDays(1)
                );

        when(
                repository.findPublishedByStoreIdAndCode(
                        STORE_ID,
                        "KS-100",
                        com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus.PUBLISHED
                )
        ).thenReturn(Optional.of(knowledgeObject));

        Optional<KnowledgeSnapshot> result =
                service.resolveAt(
                        STORE_ID,
                        "KS-100",
                        NOW
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyAtExpirationMoment()
            throws Exception {

        KnowledgeObject knowledgeObject =
                publishedKnowledgeObject(
                        NOW.minusDays(1),
                        NOW
                );

        when(
                repository.findPublishedByStoreIdAndCode(
                        STORE_ID,
                        "KS-100",
                        com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus.PUBLISHED
                )
        ).thenReturn(Optional.of(knowledgeObject));

        Optional<KnowledgeSnapshot> result =
                service.resolveAt(
                        STORE_ID,
                        "KS-100",
                        NOW
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void requireShouldThrowWhenKnowledgeCannotBeResolved() {
        when(
                repository.findPublishedByStoreIdAndCode(
                        STORE_ID,
                        "KS-100",
                        com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus.PUBLISHED
                )
        ).thenReturn(Optional.empty());

        KnowledgeResolutionException exception =
                assertThrows(
                        KnowledgeResolutionException.class,
                        () -> service.requireAt(
                                STORE_ID,
                                "KS-100",
                                NOW
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
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveAt(
                        null,
                        "KS-100",
                        NOW
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveAt(
                        0L,
                        "KS-100",
                        NOW
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectInvalidKnowledgeCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveAt(
                        STORE_ID,
                        "INVALID",
                        NOW
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullMoment() {
        assertThrows(
                NullPointerException.class,
                () -> service.resolveAt(
                        STORE_ID,
                        "KS-100",
                        null
                )
        );

        verifyNoInteractions(repository);
    }

    private static KnowledgeObject publishedKnowledgeObject(
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) throws Exception {

        Store store = new Store();
        store.setId(STORE_ID);
        store.setNombre("WebEmpresarial Test");
        store.setDominio("test.web-empresarial.local");
        store.setActiva(true);

        KnowledgeObject knowledgeObject =
                KnowledgeObject.create(
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
                        "admin"
                );

        KnowledgeObjectVersion version =
                knowledgeObject.createVersion(
                        SemanticVersion.initial(),
                        "Knowledge Objects Specification",
                        "Specification summary",
                        "# Knowledge Objects",
                        "MARKDOWN",
                        KnowledgeConfidence.full(),
                        null,
                        "admin"
                );

        setId(knowledgeObject, KNOWLEDGE_OBJECT_ID);
        setId(version, KNOWLEDGE_VERSION_ID);
        setCreatedAt(
                version,
                NOW.minusDays(2)
        );

        knowledgeObject.submitForReview("reviewer");
        knowledgeObject.approve("approver");

        knowledgeObject.publish(
                version,
                validFrom,
                validUntil,
                "publisher"
        );

        return knowledgeObject;
    }

    private static void setId(
            Object target,
            Long id
    ) throws Exception {

        Field field = target
                .getClass()
                .getDeclaredField("id");

        field.setAccessible(true);
        field.set(target, id);
    }

    private static void setCreatedAt(
            KnowledgeObjectVersion version,
            LocalDateTime createdAt
    ) throws Exception {

        Field field =
                KnowledgeObjectVersion.class
                        .getDeclaredField("createdAt");

        field.setAccessible(true);
        field.set(version, createdAt);
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