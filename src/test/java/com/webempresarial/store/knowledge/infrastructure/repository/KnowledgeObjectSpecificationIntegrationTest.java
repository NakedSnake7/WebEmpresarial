package com.webempresarial.store.knowledge.infrastructure.repository;

import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.infrastructure.specification.KnowledgeObjectSpecification;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class KnowledgeObjectSpecificationIntegrationTest {

    private static final String ACTOR = "integration-test";

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;
    
    @Autowired
    private KnowledgeObjectVersionRepository versionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager entityManager;
    
    
    @Test
    void shouldCombineTextDomainStatusAndConfidenceFilters() {

        Store store = createStore(
                "Knowledge Combined Search",
                "knowledge-combined.local"
        );

        KnowledgeObject matching =
                createKnowledgeObject(
                        store,
                        "KS-999",
                        "COMBINED",
                        KnowledgeDomain.ARCHITECTURE
                );

        knowledgeObjectRepository.saveAndFlush(matching);

        KnowledgeObjectVersion version =
                createVersion(
                        matching,
                        "Architecture Patterns",
                        new BigDecimal("0.9500")
                );

        versionRepository.saveAndFlush(version);

        preparePublishedKnowledgeObject(
                matching,
                version,
                LocalDateTime.now().minusDays(1),
                null
        );

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .text("architecture")
                        .domain(KnowledgeDomain.ARCHITECTURE)
                        .status(KnowledgeStatus.PUBLISHED)
                        .minimumConfidence(new BigDecimal("0.90"))
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent().getFirst()
                .getCode()
                .getValue())
                .isEqualTo("KS-999");
    }
    
    
    @Test
    void shouldEscapePercentCharacterInTextSearch() {

        Store store = createStore(
                "Knowledge Escape Store",
                "knowledge-escape.local"
        );

        KnowledgeObject matchingObject =
                createKnowledgeObject(
                        store,
                        "KS-991",
                        "ESCAPE-CONTEXT"
                );

        KnowledgeObject otherObject =
                createKnowledgeObject(
                        store,
                        "KS-992",
                        "ESCAPE-CONTEXT"
                );

        knowledgeObjectRepository.saveAndFlush(matchingObject);
        knowledgeObjectRepository.saveAndFlush(otherObject);

        KnowledgeObjectVersion matchingVersion =
                KnowledgeObjectVersion.create(
                        matchingObject,
                        SemanticVersion.of(1,0,0),
                        "100% Architecture",
                        "Enterprise Architecture",
                        "Content",
                        "MARKDOWN",
                        KnowledgeConfidence.of(new BigDecimal("0.9000")),
                        "integration-test",
                        ACTOR
                );

        KnowledgeObjectVersion otherVersion =
                KnowledgeObjectVersion.create(
                        otherObject,
                        SemanticVersion.of(1,0,0),
                        "Architecture Guide",
                        "Architecture",
                        "Content",
                        "MARKDOWN",
                        KnowledgeConfidence.of(new BigDecimal("0.9000")),
                        "integration-test",
                        ACTOR
                );

        versionRepository.saveAndFlush(matchingVersion);
        versionRepository.saveAndFlush(otherVersion);

        setCurrentVersion(matchingObject, matchingVersion);
        setCurrentVersion(otherObject, otherVersion);

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .text("100%")
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0,20)
                );

        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent())
                .extracting(k -> k.getCode().getValue())
                .containsExactly("KS-991");
    }
    
    @Test
    void shouldSearchKnowledgeObjectsByCodeIgnoringCase() {

        Store store = createStore(
                "Knowledge Code Search Store",
                "knowledge-code-search.local"
        );

        KnowledgeObject matchingObject =
                createKnowledgeObject(
                        store,
                        "KS-981",
                        "CODE-SEARCH-CONTEXT"
                );

        KnowledgeObject otherObject =
                createKnowledgeObject(
                        store,
                        "KS-982",
                        "CODE-SEARCH-CONTEXT"
                );

        knowledgeObjectRepository.saveAndFlush(matchingObject);
        knowledgeObjectRepository.saveAndFlush(otherObject);

        KnowledgeObjectVersion matchingVersion =
                createVersion(
                        matchingObject,
                        "Architecture Knowledge",
                        new BigDecimal("0.9000")
                );

        KnowledgeObjectVersion otherVersion =
                createVersion(
                        otherObject,
                        "Commerce Knowledge",
                        new BigDecimal("0.9000")
                );

        versionRepository.saveAndFlush(matchingVersion);
        versionRepository.saveAndFlush(otherVersion);

        setCurrentVersion(
                matchingObject,
                matchingVersion
        );

        setCurrentVersion(
                otherObject,
                otherVersion
        );

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .text("ks-981")
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
                .extracting(
                        knowledgeObject ->
                                knowledgeObject
                                        .getCode()
                                        .getValue()
                )
                .containsExactly("KS-981");
    }
    
    
    @Test
    void shouldSearchKnowledgeObjectsByCurrentVersionSummaryIgnoringCase() {

        Store store = createStore(
                "Knowledge Summary Search Store",
                "knowledge-summary-search.local"
        );

        KnowledgeObject matchingObject =
                createKnowledgeObject(
                        store,
                        "KS-971",
                        "SUMMARY-CONTEXT"
                );

        KnowledgeObject otherObject =
                createKnowledgeObject(
                        store,
                        "KS-972",
                        "SUMMARY-CONTEXT"
                );

        knowledgeObjectRepository.saveAndFlush(matchingObject);
        knowledgeObjectRepository.saveAndFlush(otherObject);

        KnowledgeObjectVersion matchingVersion =
                createVersion(
                        matchingObject,
                        "Architecture",
                        new BigDecimal("0.9000")
                );

        matchingVersion.revise(
                "Architecture",
                "DDD Tactical Patterns",
                "Architecture content",
                "MARKDOWN",
                KnowledgeConfidence.of(
                        new BigDecimal("0.9000")
                ),
                "integration-test",
                ACTOR
        ); 

        KnowledgeObjectVersion otherVersion =
                createVersion(
                        otherObject,
                        "Commerce",
                        new BigDecimal("0.9000")
                );

        otherVersion.revise(
                "Commerce",
                "Product catalogue management",
                "Commerce content",
                "MARKDOWN",
                KnowledgeConfidence.of(
                        new BigDecimal("0.9000")
                ),
                "integration-test",
                ACTOR
        );

        versionRepository.saveAndFlush(matchingVersion);
        versionRepository.saveAndFlush(otherVersion);

        setCurrentVersion(
                matchingObject,
                matchingVersion
        );

        setCurrentVersion(
                otherObject,
                otherVersion
        );

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .text("TACTICAL")
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
                .extracting(k -> k.getCode().getValue())
                .containsExactly("KS-971");
    }
    
    @Test
    void shouldSearchKnowledgeObjectsByCurrentVersionTitleIgnoringCase() {

        Store store = createStore(
                "Knowledge Text Search Store",
                "knowledge-text-search.local"
        );

        KnowledgeObject matchingObject =
                createKnowledgeObject(
                        store,
                        "KS-961",
                        "TEXT-SEARCH-CONTEXT"
                );

        KnowledgeObject nonMatchingObject =
                createKnowledgeObject(
                        store,
                        "KS-962",
                        "TEXT-SEARCH-CONTEXT"
                );

        knowledgeObjectRepository.saveAndFlush(
                matchingObject
        );

        knowledgeObjectRepository.saveAndFlush(
                nonMatchingObject
        );

        KnowledgeObjectVersion matchingVersion =
                matchingObject.createVersion(
                        SemanticVersion.of(1, 0, 0),
                        "Spring Security Architecture",
                        "Authentication and authorisation design",
                        "Detailed security architecture content",
                        "MARKDOWN",
                        KnowledgeConfidence.of(
                                new BigDecimal("0.9000")
                        ),
                        "integration-test",
                        ACTOR
                );

        KnowledgeObjectVersion nonMatchingVersion =
                nonMatchingObject.createVersion(
                        SemanticVersion.of(1, 0, 0),
                        "Commerce Product Catalogue",
                        "Product catalogue management",
                        "Detailed ecommerce catalogue content",
                        "MARKDOWN",
                        KnowledgeConfidence.of(
                                new BigDecimal("0.9000")
                        ),
                        "integration-test",
                        ACTOR
                );

        versionRepository.saveAndFlush(
                matchingVersion
        );

        versionRepository.saveAndFlush(
                nonMatchingVersion
        );

        setCurrentVersion(
                matchingObject,
                matchingVersion
        );

        setCurrentVersion(
                nonMatchingObject,
                nonMatchingVersion
        );

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .text("security")
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
                .extracting(
                        knowledgeObject ->
                                knowledgeObject
                                        .getCode()
                                        .getValue()
                )
                .containsExactly("KS-961");

        KnowledgeObject returnedObject =
                result.getContent().getFirst();

        assertThat(returnedObject.getCurrentVersion())
                .isNotNull();

        assertThat(
                returnedObject
                        .getCurrentVersion()
                        .getTitle()
        ).isEqualTo(
                "Spring Security Architecture"
        );
    }
    
    
    private void preparePublishedKnowledgeObject(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion currentVersion,
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) {
        if (validUntil == null) {
            entityManager.createNativeQuery("""
                    UPDATE knowledge_objects
                       SET status = 'PUBLISHED',
                           current_version_id = :versionId,
                           valid_from = :validFrom,
                           valid_until = NULL
                     WHERE id = :knowledgeObjectId
                    """)
                    .setParameter(
                            "versionId",
                            currentVersion.getId()
                    )
                    .setParameter(
                            "validFrom",
                            validFrom
                    )
                    .setParameter(
                            "knowledgeObjectId",
                            knowledgeObject.getId()
                    )
                    .executeUpdate();

            return;
        }

        entityManager.createNativeQuery("""
                UPDATE knowledge_objects
                   SET status = 'PUBLISHED',
                       current_version_id = :versionId,
                       valid_from = :validFrom,
                       valid_until = :validUntil
                 WHERE id = :knowledgeObjectId
                """)
                .setParameter(
                        "versionId",
                        currentVersion.getId()
                )
                .setParameter(
                        "validFrom",
                        validFrom
                )
                .setParameter(
                        "validUntil",
                        validUntil
                )
                .setParameter(
                        "knowledgeObjectId",
                        knowledgeObject.getId()
                )
                .executeUpdate();
    }
    
    @Test
    void shouldFilterKnowledgeObjectsEffectiveAtGivenMoment() {

        Store store = createStore(
                "Knowledge Effective Store",
                "knowledge-effective.local"
        );

        LocalDateTime effectiveMoment =
                LocalDateTime.of(
                        2026,
                        7,
                        29,
                        12,
                        0
                );

        KnowledgeObject effectiveObject =
                createKnowledgeObject(
                        store,
                        "KS-951",
                        "EFFECTIVE-CONTEXT"
                );

        KnowledgeObject expiredObject =
                createKnowledgeObject(
                        store,
                        "KS-952",
                        "EFFECTIVE-CONTEXT"
                );

        KnowledgeObject futureObject =
                createKnowledgeObject(
                        store,
                        "KS-953",
                        "EFFECTIVE-CONTEXT"
                );

        KnowledgeObject indefiniteObject =
                createKnowledgeObject(
                        store,
                        "KS-954",
                        "EFFECTIVE-CONTEXT"
                );

        knowledgeObjectRepository.saveAndFlush(
                effectiveObject
        );

        knowledgeObjectRepository.saveAndFlush(
                expiredObject
        );

        knowledgeObjectRepository.saveAndFlush(
                futureObject
        );

        knowledgeObjectRepository.saveAndFlush(
                indefiniteObject
        );

        KnowledgeObjectVersion effectiveVersion =
                createVersion(
                        effectiveObject,
                        "Effective knowledge",
                        new BigDecimal("0.9000")
                );

        KnowledgeObjectVersion expiredVersion =
                createVersion(
                        expiredObject,
                        "Expired knowledge",
                        new BigDecimal("0.9000")
                );

        KnowledgeObjectVersion futureVersion =
                createVersion(
                        futureObject,
                        "Future knowledge",
                        new BigDecimal("0.9000")
                );

        KnowledgeObjectVersion indefiniteVersion =
                createVersion(
                        indefiniteObject,
                        "Indefinite knowledge",
                        new BigDecimal("0.9000")
                );

        versionRepository.saveAndFlush(
                effectiveVersion
        );

        versionRepository.saveAndFlush(
                expiredVersion
        );

        versionRepository.saveAndFlush(
                futureVersion
        );

        versionRepository.saveAndFlush(
                indefiniteVersion
        );

        /*
         * Vigente en el momento consultado:
         *
         * 2026-07-28 <= 2026-07-29
         * 2026-07-30 > 2026-07-29
         */
        preparePublishedKnowledgeObject(
                effectiveObject,
                effectiveVersion,
                effectiveMoment.minusDays(1),
                effectiveMoment.plusDays(1)
        );

        /*
         * Expirado:
         *
         * validUntil es anterior al momento consultado.
         */
        preparePublishedKnowledgeObject(
                expiredObject,
                expiredVersion,
                effectiveMoment.minusDays(5),
                effectiveMoment.minusDays(1)
        );

        /*
         * Futuro:
         *
         * validFrom es posterior al momento consultado.
         */
        preparePublishedKnowledgeObject(
                futureObject,
                futureVersion,
                effectiveMoment.plusDays(1),
                effectiveMoment.plusDays(5)
        );

        /*
         * Vigencia indefinida:
         *
         * validUntil == null.
         */
        preparePublishedKnowledgeObject(
                indefiniteObject,
                indefiniteVersion,
                effectiveMoment.minusDays(10),
                null
        );

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .effectiveAt(effectiveMoment)
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(2);

        assertThat(result.getContent())
                .extracting(
                        knowledgeObject ->
                                knowledgeObject
                                        .getCode()
                                        .getValue()
                )
                .containsExactlyInAnyOrder(
                        "KS-951",
                        "KS-954"
                );

        assertThat(result.getContent())
                .allSatisfy(
                        knowledgeObject -> {
                            assertThat(
                                    knowledgeObject.getStatus()
                            ).isEqualTo(
                                    KnowledgeStatus.PUBLISHED
                            );

                            assertThat(
                                    knowledgeObject.getCurrentVersion()
                            ).isNotNull();

                            assertThat(
                                    knowledgeObject.getValidFrom()
                            ).isNotNull();

                            assertThat(
                                    knowledgeObject.getValidFrom()
                            ).isBeforeOrEqualTo(
                                    effectiveMoment
                            );
                        }
                );
    }
    
    @Test
    void shouldFilterByMinimumConfidence() {

        Store store = createStore(
                "Knowledge Confidence Store",
                "knowledge-confidence.local"
        );

        KnowledgeObject highConfidenceObject =
                createKnowledgeObject(
                        store,
                        "KS-941",
                        "CONFIDENCE-CONTEXT",
                        KnowledgeDomain.ARCHITECTURE
                );

        KnowledgeObject lowConfidenceObject =
                createKnowledgeObject(
                        store,
                        "KS-942",
                        "CONFIDENCE-CONTEXT",
                        KnowledgeDomain.ARCHITECTURE
                );

        knowledgeObjectRepository.saveAndFlush(highConfidenceObject);
        knowledgeObjectRepository.saveAndFlush(lowConfidenceObject);

        KnowledgeObjectVersion highConfidenceVersion =
                highConfidenceObject.createVersion(
                        SemanticVersion.of(1, 0, 0),
                        "High confidence knowledge",
                        "Knowledge with high confidence",
                        "Validated architectural knowledge",
                        "MARKDOWN",
                        KnowledgeConfidence.of(
                                new BigDecimal("0.8500")
                        ),
                        "integration-test",
                        "test-user"
                );

        KnowledgeObjectVersion lowConfidenceVersion =
                lowConfidenceObject.createVersion(
                        SemanticVersion.of(1, 0, 0),
                        "Low confidence knowledge",
                        "Knowledge with low confidence",
                        "Preliminary architectural knowledge",
                        "MARKDOWN",
                        KnowledgeConfidence.of(
                                new BigDecimal("0.4000")
                        ),
                        "integration-test",
                        "test-user"
                );

        versionRepository.saveAndFlush(highConfidenceVersion);
        versionRepository.saveAndFlush(lowConfidenceVersion);

        entityManager.createNativeQuery("""
                UPDATE knowledge_objects
                   SET current_version_id = :versionId
                 WHERE id = :knowledgeObjectId
                """)
                .setParameter(
                        "versionId",
                        highConfidenceVersion.getId()
                )
                .setParameter(
                        "knowledgeObjectId",
                        highConfidenceObject.getId()
                )
                .executeUpdate();

        entityManager.createNativeQuery("""
                UPDATE knowledge_objects
                   SET current_version_id = :versionId
                 WHERE id = :knowledgeObjectId
                """)
                .setParameter(
                        "versionId",
                        lowConfidenceVersion.getId()
                )
                .setParameter(
                        "knowledgeObjectId",
                        lowConfidenceObject.getId()
                )
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .minimumConfidence(
                                new BigDecimal("0.7000")
                        )
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
        .extracting(KnowledgeObject::getId)
        .containsExactly(
                highConfidenceObject.getId()
        );

        KnowledgeObject returnedObject =
                result.getContent().getFirst();

        assertThat(returnedObject.getCurrentVersion())
                .isNotNull();

        assertThat(
                returnedObject
                        .getCurrentVersion()
                        .getConfidence()
                        .getValue()
        ).isEqualByComparingTo("0.8500");
    }
    
    @Test
    void queryMustFilterKnowledgeObjectsByContext() {

        Store store = createStore(
                "Knowledge Context Store",
                "knowledge-context-store.local"
        );

        KnowledgeObject targetObject = createKnowledgeObject(
                store,
                "KS-931",
                "STORE-CONTEXT-A"
        );

        KnowledgeObject otherObject = createKnowledgeObject(
                store,
                "KS-932",
                "STORE-CONTEXT-B"
        );

        knowledgeObjectRepository.save(targetObject);
        knowledgeObjectRepository.save(otherObject);

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .context(
                                KnowledgeContextType.STORE,
                                "STORE-CONTEXT-A"
                        )
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
                .hasSize(1);

        KnowledgeObject returnedObject =
                result.getContent().getFirst();

        assertThat(returnedObject.getCode().getValue())
                .isEqualTo("KS-931");

        assertThat(returnedObject.getContextRoot().getType())
        .isEqualTo(KnowledgeContextType.STORE);

assertThat(returnedObject.getContextRoot().getReference())
        .isEqualTo("STORE-CONTEXT-A");

        assertThat(returnedObject.getStore().getId())
                .isEqualTo(store.getId());
    }
    
    @Test
    void queryMustFilterKnowledgeObjectsByDomain() {

        Store store = createStore(
                "Knowledge Domain Store",
                "knowledge-domain-store.local"
        );

        KnowledgeDomain targetDomain =
                KnowledgeDomain.values()[0];

        KnowledgeDomain otherDomain =
                KnowledgeDomain.values()[1];

        KnowledgeObject targetObject =
                createKnowledgeObject(
                        store,
                        "KS-921",
                        "DOMAIN-TARGET",
                        targetDomain
                );

        KnowledgeObject otherObject =
                createKnowledgeObject(
                        store,
                        "KS-922",
                        "DOMAIN-OTHER",
                        otherDomain
                );

        knowledgeObjectRepository.save(targetObject);
        knowledgeObjectRepository.save(otherObject);

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .domain(targetDomain)
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getContent())
                .hasSize(1);

        KnowledgeObject returnedObject =
                result.getContent().getFirst();

        assertThat(returnedObject.getCode().getValue())
                .isEqualTo("KS-921");

        assertThat(returnedObject.getDomain())
                .isEqualTo(targetDomain);

        assertThat(returnedObject.getDomain())
                .isNotEqualTo(otherDomain);

        assertThat(returnedObject.getStore().getId())
                .isEqualTo(store.getId());
    }
    
    @Test
    void queryMustFilterKnowledgeObjectsByStatus() {

        Store store = createStore(
                "Knowledge Status Store",
                "knowledge-status-store.local"
        );

        KnowledgeObject draftObject = createKnowledgeObject(
                store,
                "KS-911",
                "STATUS-DRAFT"
        );

        KnowledgeObject publishedObject = createKnowledgeObject(
                store,
                "KS-912",
                "STATUS-PUBLISHED"
        );

        knowledgeObjectRepository.save(draftObject);
        knowledgeObjectRepository.save(publishedObject);

        entityManager.flush();

        /*
         * Esta actualización directa mantiene la prueba enfocada
         * exclusivamente en KnowledgeObjectSpecification.
         *
         * El ciclo real de publicación ya se prueba en KE-01.
         */
        entityManager.createNativeQuery("""
                UPDATE knowledge_objects
                   SET status = 'PUBLISHED'
                 WHERE id = :knowledgeObjectId
                """)
                .setParameter(
                        "knowledgeObjectId",
                        publishedObject.getId()
                )
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(store.getId())
                        .status(KnowledgeStatus.PUBLISHED)
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        KnowledgeObject returnedObject =
                result.getContent().getFirst();

        assertThat(returnedObject.getCode().getValue())
                .isEqualTo("KS-912");

        assertThat(returnedObject.getStatus())
                .isEqualTo(KnowledgeStatus.PUBLISHED);

        assertThat(returnedObject.getStore().getId())
                .isEqualTo(store.getId());
    }

    @Test
    void queryMustNeverReturnKnowledgeFromAnotherStore() {

        Store storeA = createStore(
                "Knowledge Store A",
                "knowledge-store-a.local"
        );

        Store storeB = createStore(
                "Knowledge Store B",
                "knowledge-store-b.local"
        );

        KnowledgeObject objectA = createKnowledgeObject(
                storeA,
                "KS-901",
                "STORE-A"
        );

        KnowledgeObject objectB = createKnowledgeObject(
                storeB,
                "KS-902",
                "STORE-B"
        );

        knowledgeObjectRepository.save(objectA);
        knowledgeObjectRepository.save(objectB);

        entityManager.flush();
        entityManager.clear();

        KnowledgeQueryCriteria criteria =
                KnowledgeQueryCriteria.builder(storeA.getId())
                        .build();

        Page<KnowledgeObject> result =
                knowledgeObjectRepository.findAll(
                        KnowledgeObjectSpecification.from(criteria),
                        PageRequest.of(0, 20)
                );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        KnowledgeObject returnedObject =
                result.getContent().getFirst();

        assertThat(returnedObject.getStore().getId())
                .isEqualTo(storeA.getId());

        assertThat(returnedObject.getCode().getValue())
                .isEqualTo("KS-901");

        assertThat(returnedObject.getStore().getId())
                .isNotEqualTo(storeB.getId());
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

    private KnowledgeObject createKnowledgeObject(
            Store store,
            String code,
            String contextReference
    ) {
        return createKnowledgeObject(
                store,
                code,
                contextReference,
                KnowledgeDomain.values()[0]
        );
    }

    private KnowledgeObject createKnowledgeObject(
            Store store,
            String code,
            String contextReference,
            KnowledgeDomain domain
    ) {
        return KnowledgeObject.create(
                store,
                KnowledgeCode.of(code),
                KnowledgeTypeCode.values()[0],
                domain,
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                createContextRoot(contextReference),
                ACTOR
        );
    }

    private KnowledgeContextRoot createContextRoot(
            String reference
    ) {
        return KnowledgeContextRoot.of(
                KnowledgeContextType.STORE,
                reference
        );
        
        
    }
    private KnowledgeObjectVersion createVersion(
            KnowledgeObject knowledgeObject,
            String title,
            BigDecimal confidence
    ) {
        return knowledgeObject.createVersion(
                SemanticVersion.of(1, 0, 0),
                title,
                title + " summary",
                title + " content",
                "MARKDOWN",
                KnowledgeConfidence.of(confidence),
                "integration-test",
                ACTOR
        );
    }
    private void setCurrentVersion(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion version
    ) {
        entityManager.createNativeQuery("""
                UPDATE knowledge_objects
                   SET current_version_id = :versionId
                 WHERE id = :knowledgeObjectId
                """)
                .setParameter(
                        "versionId",
                        version.getId()
                )
                .setParameter(
                        "knowledgeObjectId",
                        knowledgeObject.getId()
                )
                .executeUpdate();
    }
}