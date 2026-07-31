package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.application.result.KnowledgeQueryPage;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeQueryServiceTest {

    private static final Long STORE_ID = 15L;

    @Mock
    private KnowledgeObjectRepository repository;

    private KnowledgeQueryService service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeQueryService(repository);
    }

    @Test
    void shouldReturnPagedDraftKnowledge()
            throws Exception {

        KnowledgeObject knowledgeObject =
                draftKnowledgeObject();

        PageImpl<KnowledgeObject> repositoryPage =
                new PageImpl<>(
                        List.of(knowledgeObject),
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                20
                        ),
                        1
                );

        when(
                repository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(repositoryPage);

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.forStore(
                        STORE_ID
                );

        KnowledgeQueryPage result =
                service.search(criteria);

        assertFalse(result.isEmpty());
        assertEquals(1, result.numberOfElements());
        assertEquals(1L, result.totalElements());

        assertEquals(
                "KS-100",
                result.items().get(0).code()
        );

        assertEquals(
                KnowledgeStatus.DRAFT,
                result.items().get(0).status()
        );

        assertFalse(
                result.items()
                        .get(0)
                        .hasCurrentVersion()
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        Pageable pageable =
                pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void shouldRejectNullCriteria() {
        assertThrows(
                NullPointerException.class,
                () -> service.search(null)
        );

        verifyNoInteractions(repository);
    }

    private static KnowledgeObject draftKnowledgeObject()
            throws Exception {

        com.webempresarial.store.model.Store store =
                new com.webempresarial.store.model.Store();

        store.setId(STORE_ID);
        store.setNombre("WebEmpresarial Test");
        store.setDominio(
                "test.web-empresarial.local"
        );
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

        setField(
                knowledgeObject,
                "id",
                100L
        );

        setField(
                knowledgeObject,
                "createdAt",
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        20,
                        0
                )
        );

        setField(
                knowledgeObject,
                "updatedAt",
                LocalDateTime.of(
                        2026,
                        7,
                        22,
                        10,
                        0
                )
        );

        return knowledgeObject;
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
}