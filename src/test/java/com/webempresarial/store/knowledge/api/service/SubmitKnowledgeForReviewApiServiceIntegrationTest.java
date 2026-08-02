package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeLifecycleResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SubmitKnowledgeForReviewApiServiceIntegrationTest {

    private static final String ACTOR =
            "integration-test@webempresarial.com";

    @Autowired
    private CreateKnowledgeApiService createKnowledgeApiService;

    @Autowired
    private SubmitKnowledgeForReviewApiService
            submitKnowledgeForReviewApiService;

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void shouldSubmitDraftKnowledgeForReview() {

        Store store =
                createStore(
                        "Knowledge Review Store",
                        "knowledge-review.local"
                );

        KnowledgeCreatedResponse created =
                createKnowledgeApiService.create(
                        store,
                        createRequest("KS-821"),
                        ACTOR
                );

        KnowledgeLifecycleResponse response =
                submitKnowledgeForReviewApiService.submit(
                        store.getId(),
                        created.id(),
                        ACTOR
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.id())
                .isEqualTo(created.id());

        assertThat(response.code())
                .isEqualTo("KS-821");

        assertThat(response.status())
                .isEqualTo(KnowledgeStatus.IN_REVIEW);

        assertThat(response.currentVersionId())
                .isNull();

        assertThat(response.validFrom())
                .isNull();

        assertThat(response.validUntil())
                .isNull();

        assertThat(response.updatedAt())
                .isNotNull();

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findByIdAndStoreId(
                                created.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.IN_REVIEW);

        assertThat(persistedObject.getCurrentVersion())
                .isNull();

        assertThat(persistedObject.getValidFrom())
                .isNull();

        assertThat(persistedObject.getValidUntil())
                .isNull();

        assertThat(persistedObject.getStore().getId())
                .isEqualTo(store.getId());

        assertThat(persistedObject.getUpdatedBy())
                .isEqualTo(ACTOR);
    }

    @Test
    void shouldRejectSubmittingKnowledgeTwice() {

        Store store =
                createStore(
                        "Repeated Review Store",
                        "repeated-review.local"
                );

        KnowledgeCreatedResponse created =
                createKnowledgeApiService.create(
                        store,
                        createRequest("KS-822"),
                        ACTOR
                );

        submitKnowledgeForReviewApiService.submit(
                store.getId(),
                created.id(),
                ACTOR
        );

        assertThatThrownBy(
                () -> submitKnowledgeForReviewApiService.submit(
                        store.getId(),
                        created.id(),
                        ACTOR
                )
        )
                .isInstanceOf(IllegalStateException.class);

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findByIdAndStoreId(
                                created.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.IN_REVIEW);
    }

    @Test
    void shouldNotSubmitKnowledgeFromAnotherStore() {

        Store storeA =
                createStore(
                        "Review Store A",
                        "review-store-a.local"
                );

        Store storeB =
                createStore(
                        "Review Store B",
                        "review-store-b.local"
                );

        KnowledgeCreatedResponse created =
                createKnowledgeApiService.create(
                        storeA,
                        createRequest("KS-823"),
                        ACTOR
                );

        assertThatThrownBy(
                () -> submitKnowledgeForReviewApiService.submit(
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
                .isEqualTo(KnowledgeStatus.DRAFT);
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
                "REVIEW-CONTEXT",
                "Knowledge Review Architecture",
                "Resumen para revisión.",
                "# Knowledge Review Architecture",
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

        return storeRepository.saveAndFlush(store);
    }
}