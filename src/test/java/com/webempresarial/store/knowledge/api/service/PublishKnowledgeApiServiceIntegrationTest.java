package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeLifecycleResponse;
import com.webempresarial.store.knowledge.api.dto.PublishKnowledgeRequest;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.api.exception.KnowledgeVersionNotFoundException;
import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.specification.KnowledgeObjectSpecification;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PublishKnowledgeApiServiceIntegrationTest {

    private static final String ACTOR =
            "integration-test@webempresarial.com";

    @Autowired
    private CreateKnowledgeApiService createKnowledgeApiService;

    @Autowired
    private SubmitKnowledgeForReviewApiService
            submitKnowledgeForReviewApiService;

    @Autowired
    private ApproveKnowledgeApiService approveKnowledgeApiService;

    @Autowired
    private PublishKnowledgeApiService publishKnowledgeApiService;

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void shouldPublishApprovedKnowledgeWithSelectedVersion() {

        Store store = createStore(
                "Knowledge Publish Store",
                "knowledge-publish.local"
        );

        KnowledgeCreatedResponse created =
                createApprovedKnowledge(
                        store,
                        "KS-841"
                );

        LocalDateTime validFrom =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        11,
                        30
                );

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        created.initialVersionId(),
                        validFrom,
                        null
                );

        KnowledgeLifecycleResponse response =
                publishKnowledgeApiService.publish(
                        store.getId(),
                        created.id(),
                        request,
                        ACTOR
                );

        assertThat(response.id())
                .isEqualTo(created.id());

        assertThat(response.code())
                .isEqualTo("KS-841");

        assertThat(response.status())
                .isEqualTo(KnowledgeStatus.PUBLISHED);

        assertThat(response.currentVersionId())
                .isEqualTo(created.initialVersionId());

        assertThat(response.validFrom())
                .isEqualTo(validFrom);

        assertThat(response.validUntil())
                .isNull();

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
                                created.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.PUBLISHED);

        assertThat(persistedObject.getCurrentVersion())
                .isNotNull();

        assertThat(
                persistedObject
                        .getCurrentVersion()
                        .getId()
        ).isEqualTo(
                created.initialVersionId()
        );

        assertThat(persistedObject.getValidFrom())
                .isEqualTo(validFrom);

        assertThat(persistedObject.getValidUntil())
                .isNull();

        assertThat(persistedObject.getUpdatedBy())
                .isEqualTo(ACTOR);
    }

    @Test
    void publishedKnowledgeShouldBeResolvableByEffectiveAtQuery() {

        Store store = createStore(
                "Published Query Store",
                "published-query.local"
        );

        KnowledgeCreatedResponse created =
                createApprovedKnowledge(
                        store,
                        "KS-842"
                );

        LocalDateTime validFrom =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        10,
                        0
                );

        LocalDateTime validUntil =
                LocalDateTime.of(
                        2026,
                        9,
                        2,
                        10,
                        0
                );

        publishKnowledgeApiService.publish(
                store.getId(),
                created.id(),
                new PublishKnowledgeRequest(
                        created.initialVersionId(),
                        validFrom,
                        validUntil
                ),
                ACTOR
        );

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria
                        .builder(store.getId())
                        .effectiveAt(
                                validFrom.plusDays(1)
                        )
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(
                                criteria
                        ),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
                .extracting(
                        object ->
                                object
                                        .getCode()
                                        .getValue()
                )
                .containsExactly("KS-842");
    }

    @Test
    void publishedKnowledgeShouldNotBeEffectiveBeforeValidFrom() {

        Store store = createStore(
                "Future Knowledge Store",
                "future-knowledge.local"
        );

        KnowledgeCreatedResponse created =
                createApprovedKnowledge(
                        store,
                        "KS-843"
                );

        LocalDateTime validFrom =
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        9,
                        0
                );

        publishKnowledgeApiService.publish(
                store.getId(),
                created.id(),
                new PublishKnowledgeRequest(
                        created.initialVersionId(),
                        validFrom,
                        null
                ),
                ACTOR
        );

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria
                        .builder(store.getId())
                        .effectiveAt(
                                validFrom.minusSeconds(1)
                        )
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(
                                criteria
                        ),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isZero();
    }

    @Test
    void shouldRejectPublishingDraftKnowledge() {

        Store store = createStore(
                "Draft Publish Store",
                "draft-publish.local"
        );

        KnowledgeCreatedResponse created =
                createKnowledgeApiService.create(
                        store,
                        createRequest("KS-844"),
                        ACTOR
                );

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        created.initialVersionId(),
                        LocalDateTime.now(),
                        null
                );

        assertThatThrownBy(
                () -> publishKnowledgeApiService.publish(
                        store.getId(),
                        created.id(),
                        request,
                        ACTOR
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findByIdAndStoreId(
                                created.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.DRAFT);

        assertThat(persistedObject.getCurrentVersion())
                .isNull();
    }

    @Test
    void shouldRejectVersionBelongingToAnotherKnowledgeObject() {

        Store store = createStore(
                "Foreign Version Store",
                "foreign-version.local"
        );

        KnowledgeCreatedResponse first =
                createApprovedKnowledge(
                        store,
                        "KS-845"
                );

        KnowledgeCreatedResponse second =
                createApprovedKnowledge(
                        store,
                        "KS-846"
                );

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        second.initialVersionId(),
                        LocalDateTime.now(),
                        null
                );

        assertThatThrownBy(
                () -> publishKnowledgeApiService.publish(
                        store.getId(),
                        first.id(),
                        request,
                        ACTOR
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece"
                );
    }

    @Test
    void shouldNotPublishKnowledgeFromAnotherStore() {

        Store storeA = createStore(
                "Publish Store A",
                "publish-store-a.local"
        );

        Store storeB = createStore(
                "Publish Store B",
                "publish-store-b.local"
        );

        KnowledgeCreatedResponse created =
                createApprovedKnowledge(
                        storeA,
                        "KS-847"
                );

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        created.initialVersionId(),
                        LocalDateTime.now(),
                        null
                );

        assertThatThrownBy(
                () -> publishKnowledgeApiService.publish(
                        storeB.getId(),
                        created.id(),
                        request,
                        ACTOR
                )
        )
                .isInstanceOf(
                        KnowledgeObjectNotFoundException.class
                );
    }

    @Test
    void shouldRejectVersionFromAnotherStore() {

        Store storeA = createStore(
                "Version Store A",
                "version-store-a.local"
        );

        Store storeB = createStore(
                "Version Store B",
                "version-store-b.local"
        );

        KnowledgeCreatedResponse objectA =
                createApprovedKnowledge(
                        storeA,
                        "KS-848"
                );

        KnowledgeCreatedResponse objectB =
                createApprovedKnowledge(
                        storeB,
                        "KS-849"
                );

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        objectB.initialVersionId(),
                        LocalDateTime.now(),
                        null
                );

        assertThatThrownBy(
                () -> publishKnowledgeApiService.publish(
                        storeA.getId(),
                        objectA.id(),
                        request,
                        ACTOR
                )
        )
                .isInstanceOf(
                        KnowledgeVersionNotFoundException.class
                );
    }

    private KnowledgeCreatedResponse createApprovedKnowledge(
            Store store,
            String code
    ) {
        KnowledgeCreatedResponse created =
                createKnowledgeApiService.create(
                        store,
                        createRequest(code),
                        ACTOR
                );

        submitKnowledgeForReviewApiService.submit(
                store.getId(),
                created.id(),
                ACTOR
        );

        approveKnowledgeApiService.approve(
                store.getId(),
                created.id(),
                ACTOR
        );

        return created;
    }

    private CreateKnowledgeRequest createRequest(
            String code
    ) {
        return new CreateKnowledgeRequest(
                code,
                KnowledgeTypeCode.values()[0],
                KnowledgeDomain.values()[0],
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                KnowledgeContextType.STORE,
                "PUBLISH-CONTEXT",
                "Knowledge Publication Architecture",
                "Resumen del flujo de publicación.",
                "# Knowledge Publication Architecture",
                "MARKDOWN",
                new BigDecimal("0.9500"),
                "KS-000"
        );
    }

    private Store createStore(
            String name,
            String domain
    ) {
        Store store = new Store();

        store.setNombre(name);
        store.setDominio(domain);
        store.setActiva(true);
        store.setPlan(StorePlan.PREMIUM);

        return storeRepository.saveAndFlush(
                store
        );
    }
}