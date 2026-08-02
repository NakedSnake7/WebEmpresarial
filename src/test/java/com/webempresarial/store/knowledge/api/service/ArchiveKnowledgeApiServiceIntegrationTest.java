package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeLifecycleResponse;
import com.webempresarial.store.knowledge.api.dto.PublishKnowledgeRequest;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
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
class ArchiveKnowledgeApiServiceIntegrationTest {

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
    private ArchiveKnowledgeApiService archiveKnowledgeApiService;

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void shouldArchivePublishedKnowledge() {

        Store store =
                createStore(
                        "Knowledge Archive Store",
                        "knowledge-archive.local"
                );

        KnowledgeCreatedResponse created =
                createPublishedKnowledge(
                        store,
                        "KS-851"
                );

        KnowledgeLifecycleResponse response =
                archiveKnowledgeApiService.archive(
                        store.getId(),
                        created.id(),
                        ACTOR
                );

        assertThat(response.status())
                .isEqualTo(KnowledgeStatus.ARCHIVED);

        assertThat(response.currentVersionId())
                .isEqualTo(created.initialVersionId());

        assertThat(response.validFrom())
                .isNotNull();

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
                                created.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.ARCHIVED);

        assertThat(persistedObject.getCurrentVersion())
                .isNotNull();

        assertThat(
                persistedObject
                        .getCurrentVersion()
                        .getId()
        ).isEqualTo(
                created.initialVersionId()
        );

        assertThat(persistedObject.getUpdatedBy())
                .isEqualTo(ACTOR);
    }

    @Test
    void archivedKnowledgeShouldNotBeReturnedAsEffective() {

        Store store =
                createStore(
                        "Archived Query Store",
                        "archived-query.local"
                );

        KnowledgeCreatedResponse created =
                createPublishedKnowledge(
                        store,
                        "KS-852"
                );

        archiveKnowledgeApiService.archive(
                store.getId(),
                created.id(),
                ACTOR
        );

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria
                        .builder(store.getId())
                        .effectiveAt(
                                LocalDateTime.now()
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
    void shouldRejectArchivingDraftKnowledge() {

        Store store =
                createStore(
                        "Draft Archive Store",
                        "draft-archive.local"
                );

        KnowledgeCreatedResponse created =
                createKnowledgeApiService.create(
                        store,
                        createRequest("KS-853"),
                        ACTOR
                );

        assertThatThrownBy(
                () -> archiveKnowledgeApiService.archive(
                        store.getId(),
                        created.id(),
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
    }

    @Test
    void shouldRejectArchivingKnowledgeTwice() {

        Store store =
                createStore(
                        "Repeated Archive Store",
                        "repeated-archive.local"
                );

        KnowledgeCreatedResponse created =
                createPublishedKnowledge(
                        store,
                        "KS-854"
                );

        archiveKnowledgeApiService.archive(
                store.getId(),
                created.id(),
                ACTOR
        );

        assertThatThrownBy(
                () -> archiveKnowledgeApiService.archive(
                        store.getId(),
                        created.id(),
                        ACTOR
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }

    @Test
    void shouldNotArchiveKnowledgeFromAnotherStore() {

        Store storeA =
                createStore(
                        "Archive Store A",
                        "archive-store-a.local"
                );

        Store storeB =
                createStore(
                        "Archive Store B",
                        "archive-store-b.local"
                );

        KnowledgeCreatedResponse created =
                createPublishedKnowledge(
                        storeA,
                        "KS-855"
                );

        assertThatThrownBy(
                () -> archiveKnowledgeApiService.archive(
                        storeB.getId(),
                        created.id(),
                        ACTOR
                )
        )
                .isInstanceOf(
                        KnowledgeObjectNotFoundException.class
                );

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findByIdAndStoreId(
                                created.id(),
                                storeA.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.PUBLISHED);
    }

    private KnowledgeCreatedResponse createPublishedKnowledge(
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

        publishKnowledgeApiService.publish(
                store.getId(),
                created.id(),
                new PublishKnowledgeRequest(
                        created.initialVersionId(),
                        LocalDateTime.now().minusMinutes(1),
                        null
                ),
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
                "ARCHIVE-CONTEXT",
                "Knowledge Archive Architecture",
                "Resumen del proceso de archivado.",
                "# Knowledge Archive Architecture",
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