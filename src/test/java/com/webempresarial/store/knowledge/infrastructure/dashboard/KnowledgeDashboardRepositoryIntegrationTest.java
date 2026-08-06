package com.webempresarial.store.knowledge.infrastructure.dashboard;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectVersionRepository;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class KnowledgeDashboardRepositoryIntegrationTest {

    private static final String ACTOR =
            "knowledge-dashboard-integration-test";

    @Autowired
    private KnowledgeDashboardRepository dashboardRepository;

    @Autowired
    private KnowledgeObjectRepository knowledgeObjectRepository;

    @Autowired
    private KnowledgeObjectVersionRepository versionRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager entityManager;

    /*
     * =========================================================
     * EMPTY DASHBOARD
     * =========================================================
     */

    @Test
    void shouldReturnZeroMetricsWhenStoreHasNoKnowledge() {
        Store store =
                createStore(
                        "Empty Knowledge Dashboard",
                        "knowledge-dashboard-empty.local"
                );

        KnowledgeDashboardMetricsSnapshot metrics =
                dashboardRepository.loadMetrics(
                        store.getId()
                );

        assertThat(metrics.totalObjects())
                .isZero();

        assertThat(metrics.draftObjects())
                .isZero();

        assertThat(metrics.reviewObjects())
                .isZero();

        assertThat(metrics.approvedObjects())
                .isZero();

        assertThat(metrics.publishedObjects())
                .isZero();

        assertThat(metrics.archivedObjects())
                .isZero();

        assertThat(metrics.totalVersions())
                .isZero();

        assertThat(metrics.averageConfidence())
                .isNull();
    }

    /*
     * =========================================================
     * STATUS COUNTS
     * =========================================================
     */

    @Test
    void shouldCountKnowledgeObjectsByStatus() {
        Store store =
                createStore(
                        "Knowledge Status Dashboard",
                        "knowledge-dashboard-status.local"
                );

        KnowledgeObject draftOne =
                persistKnowledgeObject(
                        store,
                        "KS-701",
                        "DASHBOARD-DRAFT-ONE"
                );

        KnowledgeObject draftTwo =
                persistKnowledgeObject(
                        store,
                        "KS-702",
                        "DASHBOARD-DRAFT-TWO"
                );

        KnowledgeObject review =
                persistKnowledgeObject(
                        store,
                        "KS-703",
                        "DASHBOARD-REVIEW"
                );

        KnowledgeObject approved =
                persistKnowledgeObject(
                        store,
                        "KS-704",
                        "DASHBOARD-APPROVED"
                );

        KnowledgeObject publishedOne =
                persistKnowledgeObject(
                        store,
                        "KS-705",
                        "DASHBOARD-PUBLISHED-ONE"
                );

        KnowledgeObject publishedTwo =
                persistKnowledgeObject(
                        store,
                        "KS-706",
                        "DASHBOARD-PUBLISHED-TWO"
                );

        KnowledgeObject archived =
                persistKnowledgeObject(
                        store,
                        "KS-707",
                        "DASHBOARD-ARCHIVED"
                );

        updateStatus(
                review,
                KnowledgeStatus.IN_REVIEW
        );

        updateStatus(
                approved,
                KnowledgeStatus.APPROVED
        );

        updateStatus(
                publishedOne,
                KnowledgeStatus.PUBLISHED
        );

        updateStatus(
                publishedTwo,
                KnowledgeStatus.PUBLISHED
        );

        updateStatus(
                archived,
                KnowledgeStatus.ARCHIVED
        );

        flushAndClear();

        KnowledgeDashboardMetricsSnapshot metrics =
                dashboardRepository.loadMetrics(
                        store.getId()
                );

        assertThat(metrics.totalObjects())
                .isEqualTo(7);

        assertThat(metrics.draftObjects())
                .isEqualTo(2);

        assertThat(metrics.reviewObjects())
                .isEqualTo(1);

        assertThat(metrics.approvedObjects())
                .isEqualTo(1);

        assertThat(metrics.publishedObjects())
                .isEqualTo(2);

        assertThat(metrics.archivedObjects())
                .isEqualTo(1);

        /*
         * Referencias explícitas para dejar claro que ambos
         * objetos permanecieron en DRAFT.
         */
        assertThat(draftOne.getId())
                .isNotNull();

        assertThat(draftTwo.getId())
                .isNotNull();
    }

    /*
     * =========================================================
     * VERSION COUNT
     * =========================================================
     */

    @Test
    void shouldCountAllKnowledgeVersions() {
        Store store =
                createStore(
                        "Knowledge Version Dashboard",
                        "knowledge-dashboard-versions.local"
                );

        KnowledgeObject firstObject =
                persistKnowledgeObject(
                        store,
                        "KS-711",
                        "DASHBOARD-VERSIONS-FIRST"
                );

        KnowledgeObject secondObject =
                persistKnowledgeObject(
                        store,
                        "KS-712",
                        "DASHBOARD-VERSIONS-SECOND"
                );

        persistVersion(
                firstObject,
                1,
                0,
                0,
                "First object v1",
                "0.7000"
        );

        persistVersion(
                firstObject,
                1,
                1,
                0,
                "First object v1.1",
                "0.8000"
        );

        persistVersion(
                firstObject,
                2,
                0,
                0,
                "First object v2",
                "0.9500"
        );

        persistVersion(
                secondObject,
                1,
                0,
                0,
                "Second object v1",
                "0.8500"
        );

        flushAndClear();

        KnowledgeDashboardMetricsSnapshot metrics =
                dashboardRepository.loadMetrics(
                        store.getId()
                );

        assertThat(metrics.totalObjects())
                .isEqualTo(2);

        assertThat(metrics.totalVersions())
                .isEqualTo(4);
    }

    /*
     * =========================================================
     * LATEST SEMANTIC VERSION CONFIDENCE
     * =========================================================
     */

    @Test
    void shouldUseOnlyLatestSemanticVersionForAverageConfidence() {
        Store store =
                createStore(
                        "Knowledge Confidence Dashboard",
                        "knowledge-dashboard-confidence.local"
                );

        KnowledgeObject architecture =
                persistKnowledgeObject(
                        store,
                        "KS-721",
                        "DASHBOARD-CONFIDENCE-ARCHITECTURE"
                );

        KnowledgeObject strategy =
                persistKnowledgeObject(
                        store,
                        "KS-722",
                        "DASHBOARD-CONFIDENCE-STRATEGY"
                );

        /*
         * El objeto Architecture tiene tres versiones.
         * Solo v2.0.0 = 0.9000 debe participar en la media.
         */
        persistVersion(
                architecture,
                1,
                0,
                0,
                "Architecture v1",
                "0.4000"
        );

        persistVersion(
                architecture,
                1,
                1,
                0,
                "Architecture v1.1",
                "0.6000"
        );

        persistVersion(
                architecture,
                2,
                0,
                0,
                "Architecture v2",
                "0.9000"
        );

        /*
         * Para Strategy, la última versión es v1.5.0 = 0.7000.
         */
        persistVersion(
                strategy,
                1,
                0,
                0,
                "Strategy v1",
                "0.5000"
        );

        persistVersion(
                strategy,
                1,
                5,
                0,
                "Strategy v1.5",
                "0.7000"
        );

        flushAndClear();

        KnowledgeDashboardMetricsSnapshot metrics =
                dashboardRepository.loadMetrics(
                        store.getId()
                );

        /*
         * (0.9000 + 0.7000) / 2 = 0.8000
         */
        assertThat(metrics.averageConfidence())
                .isEqualByComparingTo(
                        "0.8000"
                );

        assertThat(metrics.totalVersions())
                .isEqualTo(5);
    }

    /*
     * =========================================================
     * SEMANTIC ORDER
     * =========================================================
     */

    @Test
    void shouldResolveLatestVersionUsingMajorMinorAndPatchOrder() {
        Store store =
                createStore(
                        "Knowledge Semantic Dashboard",
                        "knowledge-dashboard-semantic.local"
                );

        KnowledgeObject knowledgeObject =
                persistKnowledgeObject(
                        store,
                        "KS-731",
                        "DASHBOARD-SEMANTIC"
                );

        persistVersion(
                knowledgeObject,
                1,
                9,
                9,
                "Semantic v1.9.9",
                "0.3000"
        );

        persistVersion(
                knowledgeObject,
                2,
                0,
                0,
                "Semantic v2.0.0",
                "0.9500"
        );

        persistVersion(
                knowledgeObject,
                1,
                10,
                0,
                "Semantic v1.10.0",
                "0.6000"
        );

        flushAndClear();

        KnowledgeDashboardMetricsSnapshot metrics =
                dashboardRepository.loadMetrics(
                        store.getId()
                );

        /*
         * v2.0.0 debe ganar sobre v1.10.0 y v1.9.9.
         */
        assertThat(metrics.averageConfidence())
                .isEqualByComparingTo(
                        "0.9500"
                );
    }

    /*
     * =========================================================
     * MULTI-TENANCY
     * =========================================================
     */

    @Test
    void shouldNeverMixDashboardMetricsBetweenStores() {
        Store storeA =
                createStore(
                        "Knowledge Dashboard Store A",
                        "knowledge-dashboard-store-a.local"
                );

        Store storeB =
                createStore(
                        "Knowledge Dashboard Store B",
                        "knowledge-dashboard-store-b.local"
                );

        KnowledgeObject objectAOne =
                persistKnowledgeObject(
                        storeA,
                        "KS-741",
                        "DASHBOARD-STORE-A-ONE"
                );

        KnowledgeObject objectATwo =
                persistKnowledgeObject(
                        storeA,
                        "KS-742",
                        "DASHBOARD-STORE-A-TWO"
                );

        KnowledgeObject objectB =
                persistKnowledgeObject(
                        storeB,
                        "KS-743",
                        "DASHBOARD-STORE-B"
                );

        persistVersion(
                objectAOne,
                1,
                0,
                0,
                "Store A first",
                "0.8000"
        );

        persistVersion(
                objectATwo,
                1,
                0,
                0,
                "Store A second",
                "1.0000"
        );

        persistVersion(
                objectB,
                1,
                0,
                0,
                "Store B only",
                "0.2000"
        );

        updateStatus(
                objectATwo,
                KnowledgeStatus.PUBLISHED
        );

        updateStatus(
                objectB,
                KnowledgeStatus.ARCHIVED
        );

        flushAndClear();

        KnowledgeDashboardMetricsSnapshot storeAMetrics =
                dashboardRepository.loadMetrics(
                        storeA.getId()
                );

        KnowledgeDashboardMetricsSnapshot storeBMetrics =
                dashboardRepository.loadMetrics(
                        storeB.getId()
                );

        assertThat(storeAMetrics.totalObjects())
                .isEqualTo(2);

        assertThat(storeAMetrics.draftObjects())
                .isEqualTo(1);

        assertThat(storeAMetrics.publishedObjects())
                .isEqualTo(1);

        assertThat(storeAMetrics.archivedObjects())
                .isZero();

        assertThat(storeAMetrics.totalVersions())
                .isEqualTo(2);

        assertThat(storeAMetrics.averageConfidence())
                .isEqualByComparingTo(
                        "0.9000"
                );

        assertThat(storeBMetrics.totalObjects())
                .isEqualTo(1);

        assertThat(storeBMetrics.draftObjects())
                .isZero();

        assertThat(storeBMetrics.publishedObjects())
                .isZero();

        assertThat(storeBMetrics.archivedObjects())
                .isEqualTo(1);

        assertThat(storeBMetrics.totalVersions())
                .isEqualTo(1);

        assertThat(storeBMetrics.averageConfidence())
                .isEqualByComparingTo(
                        "0.2000"
                );
    }

    /*
     * =========================================================
     * OBJECT WITHOUT VERSIONS
     * =========================================================
     */

    @Test
    void shouldIgnoreObjectsWithoutVersionsInAverageConfidence() {
        Store store =
                createStore(
                        "Knowledge Partial Confidence Dashboard",
                        "knowledge-dashboard-partial-confidence.local"
                );

        persistKnowledgeObject(
                store,
                "KS-751",
                "DASHBOARD-WITHOUT-VERSION"
        );

        KnowledgeObject versionedObject =
                persistKnowledgeObject(
                        store,
                        "KS-752",
                        "DASHBOARD-WITH-VERSION"
                );

        persistVersion(
                versionedObject,
                1,
                0,
                0,
                "Versioned object",
                "0.8750"
        );

        flushAndClear();

        KnowledgeDashboardMetricsSnapshot metrics =
                dashboardRepository.loadMetrics(
                        store.getId()
                );

        assertThat(metrics.totalObjects())
                .isEqualTo(2);

        assertThat(metrics.totalVersions())
                .isEqualTo(1);

        assertThat(metrics.averageConfidence())
                .isEqualByComparingTo(
                        "0.8750"
                );
    }

    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */

    @Test
    void shouldRejectNullStoreId() {
        assertThatThrownBy(
                () -> dashboardRepository.loadMetrics(
                        null
                )
        )
                .isInstanceOf(
                        InvalidDataAccessApiUsageException.class
                )
                .hasMessageContaining(
                        "El storeId debe ser válido"
                )
                .hasCauseInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void shouldRejectNonPositiveStoreId() {
        assertThatThrownBy(
                () -> dashboardRepository.loadMetrics(
                        0L
                )
        )
                .isInstanceOf(
                        InvalidDataAccessApiUsageException.class
                )
                .hasMessageContaining(
                        "El storeId debe ser válido"
                )
                .hasCauseInstanceOf(
                        IllegalArgumentException.class
                );
    }
    /*
     * =========================================================
     * TEST FACTORIES
     * =========================================================
     */

    private Store createStore(
            String name,
            String domain
    ) {
        Store store =
                new Store();

        store.setNombre(
                name
        );

        store.setDominio(
                domain
        );

        store.setActiva(
                true
        );

        store.setPlan(
                StorePlan.PREMIUM
        );

        return storeRepository.saveAndFlush(
                store
        );
    }

    private KnowledgeObject persistKnowledgeObject(
            Store store,
            String code,
            String contextReference
    ) {
        KnowledgeObject knowledgeObject =
                createKnowledgeObject(
                        store,
                        code,
                        contextReference
                );

        return knowledgeObjectRepository.saveAndFlush(
                knowledgeObject
        );
    }

    private KnowledgeObject createKnowledgeObject(
            Store store,
            String code,
            String contextReference
    ) {
        return KnowledgeObject.create(
                store,
                KnowledgeCode.of(
                        code
                ),
                KnowledgeTypeCode.values()[0],
                KnowledgeDomain.values()[0],
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                KnowledgeContextRoot.of(
                        KnowledgeContextType.STORE,
                        contextReference
                ),
                ACTOR
        );
    }

    private KnowledgeObjectVersion persistVersion(
            KnowledgeObject knowledgeObject,
            int major,
            int minor,
            int patch,
            String title,
            String confidence
    ) {
        KnowledgeObjectVersion version =
                knowledgeObject.createVersion(
                        SemanticVersion.of(
                                major,
                                minor,
                                patch
                        ),
                        title,
                        title + " summary",
                        title + " content",
                        "MARKDOWN",
                        KnowledgeConfidence.of(
                                new BigDecimal(
                                        confidence
                                )
                        ),
                        "knowledge-dashboard-test",
                        ACTOR
                );

        return versionRepository.saveAndFlush(
                version
        );
    }

    private void updateStatus(
            KnowledgeObject knowledgeObject,
            KnowledgeStatus status
    ) {
        entityManager.createNativeQuery(
                """
                UPDATE knowledge_objects
                   SET status = :status
                 WHERE id = :knowledgeObjectId
                """
        )
                .setParameter(
                        "status",
                        status.name()
                )
                .setParameter(
                        "knowledgeObjectId",
                        knowledgeObject.getId()
                )
                .executeUpdate();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}