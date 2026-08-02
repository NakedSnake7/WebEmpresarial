package com.webempresarial.store.knowledge.api.service;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeVersionRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeVersionCreatedResponse;
import com.webempresarial.store.knowledge.api.exception.DuplicateKnowledgeVersionException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CreateKnowledgeVersionApiServiceIntegrationTest {

    private static final String ACTOR =
            "integration-test@webempresarial.com";

    @Autowired
    private CreateKnowledgeApiService createKnowledgeApiService;

    @Autowired
    private CreateKnowledgeVersionApiService createKnowledgeVersionApiService;

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    private KnowledgeObjectVersionRepository versionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void shouldCreateAdditionalKnowledgeVersion() {

        Store store =
                createStore(
                        "Knowledge Version Store",
                        "knowledge-version.local"
                );

        KnowledgeCreatedResponse createdKnowledge =
                createKnowledgeApiService.create(
                        store,
                        createKnowledgeRequest("KS-811"),
                        ACTOR
                );

        CreateKnowledgeVersionRequest versionRequest =
                createVersionRequest(
                        1,
                        1,
                        0,
                        "Knowledge Command Architecture 1.1"
                );

        KnowledgeVersionCreatedResponse response =
                createKnowledgeVersionApiService.create(
                        store.getId(),
                        createdKnowledge.id(),
                        versionRequest,
                        ACTOR
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.knowledgeObjectId())
                .isEqualTo(createdKnowledge.id());

        assertThat(response.versionId())
                .isNotNull();

        assertThat(response.semanticVersion())
                .isEqualTo("1.1.0");

        assertThat(response.title())
                .isEqualTo(
                        "Knowledge Command Architecture 1.1"
                );

        assertThat(response.confidence())
                .isEqualByComparingTo("0.9700");

        assertThat(response.createdAt())
                .isNotNull();

        KnowledgeObject persistedObject =
                knowledgeObjectRepository
                        .findWithCurrentVersionByIdAndStoreId(
                                createdKnowledge.id(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(persistedObject.getStatus())
                .isEqualTo(KnowledgeStatus.DRAFT);

        /*
         * Crear una versión editorial no debe publicarla
         * ni convertirla en currentVersion.
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
                                response.versionId(),
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(
                persistedVersion
                        .getSemanticVersion()
                        .toString()
        ).isEqualTo("1.1.0");

        assertThat(persistedVersion.getTitle())
                .isEqualTo(
                        "Knowledge Command Architecture 1.1"
                );

        assertThat(persistedVersion.getSummary())
                .isEqualTo(
                        "Nueva versión editorial del Knowledge Engine."
                );

        assertThat(persistedVersion.getContent())
                .isEqualTo(
                        "# Knowledge Command Architecture 1.1"
                );

        assertThat(persistedVersion.getContentFormat())
                .isEqualTo("MARKDOWN");

        assertThat(
                persistedVersion
                        .getConfidence()
                        .getValue()
        ).isEqualByComparingTo("0.9700");

        assertThat(persistedVersion.getCreatedBy())
                .isEqualTo(ACTOR);

        assertThat(
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                createdKnowledge.id(),
                                store.getId()
                        )
        ).isEqualTo(2);
    }

    @Test
    void shouldRejectDuplicateSemanticVersion() {

        Store store =
                createStore(
                        "Duplicate Version Store",
                        "duplicate-version.local"
                );

        KnowledgeCreatedResponse createdKnowledge =
                createKnowledgeApiService.create(
                        store,
                        createKnowledgeRequest("KS-812"),
                        ACTOR
                );

        CreateKnowledgeVersionRequest duplicatedVersion =
                createVersionRequest(
                        1,
                        0,
                        0,
                        "Duplicate version"
                );

        assertThatThrownBy(
                () -> createKnowledgeVersionApiService.create(
                        store.getId(),
                        createdKnowledge.id(),
                        duplicatedVersion,
                        ACTOR
                )
        )
                .isInstanceOf(
                        DuplicateKnowledgeVersionException.class
                )
                .hasMessageContaining("1.0.0");

        assertThat(
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                createdKnowledge.id(),
                                store.getId()
                        )
        ).isEqualTo(1);
    }

    @Test
    void shouldNotCreateVersionForKnowledgeObjectFromAnotherStore() {

        Store storeA =
                createStore(
                        "Knowledge Store A",
                        "knowledge-version-a.local"
                );

        Store storeB =
                createStore(
                        "Knowledge Store B",
                        "knowledge-version-b.local"
                );

        KnowledgeCreatedResponse createdKnowledge =
                createKnowledgeApiService.create(
                        storeA,
                        createKnowledgeRequest("KS-813"),
                        ACTOR
                );

        CreateKnowledgeVersionRequest request =
                createVersionRequest(
                        1,
                        1,
                        0,
                        "Cross tenant version"
                );

        assertThatThrownBy(
                () -> createKnowledgeVersionApiService.create(
                        storeB.getId(),
                        createdKnowledge.id(),
                        request,
                        ACTOR
                )
        )
                .isInstanceOf(
                        com.webempresarial.store.knowledge.api.exception
                                .KnowledgeObjectNotFoundException.class
                );

        assertThat(
                versionRepository
                        .countByKnowledgeObjectIdAndKnowledgeObjectStoreId(
                                createdKnowledge.id(),
                                storeA.getId()
                        )
        ).isEqualTo(1);
    }

    private CreateKnowledgeRequest createKnowledgeRequest(
            String code
    ) {
        return new CreateKnowledgeRequest(
                code,
                KnowledgeTypeCode.values()[0],
                KnowledgeDomain.values()[0],
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                KnowledgeContextType.STORE,
                "VERSION-CONTEXT",
                "Knowledge Command Architecture",
                "Arquitectura inicial del command side.",
                "# Knowledge Command Architecture",
                "MARKDOWN",
                new BigDecimal("0.9500"),
                "KS-000"
        );
    }

    private CreateKnowledgeVersionRequest createVersionRequest(
            int major,
            int minor,
            int patch,
            String title
    ) {
        return new CreateKnowledgeVersionRequest(
                major,
                minor,
                patch,
                title,
                "Nueva versión editorial del Knowledge Engine.",
                "# Knowledge Command Architecture 1.1",
                "MARKDOWN",
                new BigDecimal("0.9700"),
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