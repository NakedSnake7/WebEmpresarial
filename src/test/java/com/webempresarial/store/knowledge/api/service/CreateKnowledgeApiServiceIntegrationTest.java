package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CreateKnowledgeApiServiceIntegrationTest {

    private static final String ACTOR =
            "integration-test@webempresarial.com";

    @Autowired
    private CreateKnowledgeApiService createKnowledgeApiService;

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    private KnowledgeObjectVersionRepository versionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void shouldCreateDraftKnowledgeObjectWithInitialVersion() {

        Store store =
                createStore(
                        "Knowledge Command Store",
                        "knowledge-command.local"
                );

        CreateKnowledgeRequest request =
                createRequest("KS-801");

        KnowledgeCreatedResponse response =
                createKnowledgeApiService.create(
                        store,
                        request,
                        ACTOR
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.id())
                .isNotNull();

        assertThat(response.code())
                .isEqualTo("KS-801");

        assertThat(response.status())
                .isEqualTo(KnowledgeStatus.DRAFT);

        assertThat(response.initialVersionId())
                .isNotNull();

        assertThat(response.semanticVersion())
                .isEqualTo("1.0.0");

        assertThat(response.createdAt())
                .isNotNull();

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findByIdAndStoreId(
                                response.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getCode().getValue())
                .isEqualTo("KS-801");

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.DRAFT);

        assertThat(persistedObject.getStore().getId())
                .isEqualTo(store.getId());

        /*
         * La versión inicial existe, pero todavía no representa
         * conocimiento vigente publicado.
         */
        assertThat(persistedObject.getCurrentVersion())
                .isNull();

        assertThat(persistedObject.getValidFrom())
                .isNull();

        assertThat(persistedObject.getValidUntil())
                .isNull();

        KnowledgeObjectVersion persistedVersion =
                versionRepository
                        .findByIdAndKnowledgeObjectStoreId(
                                response.initialVersionId(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedVersion.getKnowledgeObject().getId())
                .isEqualTo(persistedObject.getId());

        assertThat(persistedVersion.getSemanticVersion().toString())
                .isEqualTo("1.0.0");

        assertThat(persistedVersion.getTitle())
                .isEqualTo("Knowledge Command Architecture");

        assertThat(persistedVersion.getSummary())
                .isEqualTo(
                        "Arquitectura inicial del command side."
                );

        assertThat(persistedVersion.getContent())
                .isEqualTo(
                        "# Knowledge Command Architecture"
                );

        assertThat(persistedVersion.getContentFormat())
                .isEqualTo("MARKDOWN");

        assertThat(
                persistedVersion
                        .getConfidence()
                        .getValue()
        ).isEqualByComparingTo("0.9500");

        assertThat(persistedVersion.getSourceReference())
                .isEqualTo("KS-000");

        assertThat(persistedVersion.getCreatedBy())
                .isEqualTo(ACTOR);

        assertThat(
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                persistedObject.getId(),
                                store.getId()
                        )
        ).isEqualTo(1);
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
                "COMMAND-CONTEXT",
                "Knowledge Command Architecture",
                "Arquitectura inicial del command side.",
                "# Knowledge Command Architecture",
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