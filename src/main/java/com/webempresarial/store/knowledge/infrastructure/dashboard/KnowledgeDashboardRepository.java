package com.webempresarial.store.knowledge.infrastructure.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Repository
@Transactional(readOnly = true)
public class KnowledgeDashboardRepository {

    private static final int CONFIDENCE_SCALE = 4;

    private final EntityManager entityManager;

    public KnowledgeDashboardRepository(
            EntityManager entityManager
    ) {
        this.entityManager =
                Objects.requireNonNull(
                        entityManager,
                        "EntityManager es obligatorio"
                );
    }

    public KnowledgeDashboardMetricsSnapshot
    loadMetrics(
            Long storeId
    ) {
        validateStoreId(
                storeId
        );

        StatusCounts statusCounts =
                loadStatusCounts(
                        storeId
                );

        long totalVersions =
                countVersions(
                        storeId
                );

        BigDecimal averageConfidence =
                calculateLatestVersionAverageConfidence(
                        storeId
                );

        return new KnowledgeDashboardMetricsSnapshot(
                statusCounts.total(),
                statusCounts.draft(),
                statusCounts.inReview(),
                statusCounts.approved(),
                statusCounts.published(),
                statusCounts.archived(),
                totalVersions,
                averageConfidence
        );
    }

    private StatusCounts loadStatusCounts(
            Long storeId
    ) {
        Query query =
                entityManager.createNativeQuery(
                        """
                        SELECT
                            COUNT(ko.id) AS total_objects,

                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN ko.status = 'DRAFT'
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ) AS draft_objects,

                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN ko.status = 'IN_REVIEW'
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ) AS review_objects,

                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN ko.status = 'APPROVED'
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ) AS approved_objects,

                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN ko.status = 'PUBLISHED'
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ) AS published_objects,

                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN ko.status = 'ARCHIVED'
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ) AS archived_objects

                        FROM knowledge_objects ko

                        WHERE ko.store_id = :storeId
                        """
                );

        query.setParameter(
                "storeId",
                storeId
        );

        Object[] row =
                (Object[]) query.getSingleResult();

        return new StatusCounts(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3]),
                toLong(row[4]),
                toLong(row[5])
        );
    }

    private long countVersions(
            Long storeId
    ) {
        Query query =
                entityManager.createNativeQuery(
                        """
                        SELECT COUNT(kov.id)

                        FROM knowledge_object_versions kov

                        INNER JOIN knowledge_objects ko
                            ON ko.id = kov.knowledge_object_id

                        WHERE ko.store_id = :storeId
                        """
                );

        query.setParameter(
                "storeId",
                storeId
        );

        return toLong(
                query.getSingleResult()
        );
    }

    /*
     * Calcula la confianza media de la versión semánticamente
     * más reciente de cada KnowledgeObject.
     *
     * De esta forma, las versiones históricas no distorsionan
     * la métrica ejecutiva del dashboard.
     */
    private BigDecimal
    calculateLatestVersionAverageConfidence(
            Long storeId
    ) {
        Query query =
                entityManager.createNativeQuery(
                        """
                        SELECT AVG(kov.confidence)

                        FROM knowledge_object_versions kov

                        INNER JOIN knowledge_objects ko
                            ON ko.id = kov.knowledge_object_id

                        WHERE ko.store_id = :storeId

                          AND kov.confidence IS NOT NULL

                          AND NOT EXISTS (

                              SELECT 1

                              FROM knowledge_object_versions newer

                              WHERE newer.knowledge_object_id =
                                    kov.knowledge_object_id

                                AND (
                                    newer.version_major >
                                        kov.version_major

                                    OR (
                                        newer.version_major =
                                            kov.version_major

                                        AND newer.version_minor >
                                            kov.version_minor
                                    )

                                    OR (
                                        newer.version_major =
                                            kov.version_major

                                        AND newer.version_minor =
                                            kov.version_minor

                                        AND newer.version_patch >
                                            kov.version_patch
                                    )
                                )
                          )
                        """
                );

        query.setParameter(
                "storeId",
                storeId
        );

        Object result =
                query.getSingleResult();

        if (result == null) {
            return null;
        }

        BigDecimal average =
                toBigDecimal(
                        result
                );

        return average.setScale(
                CONFIDENCE_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private void validateStoreId(
            Long storeId
    ) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }
    }

    private long toLong(
            Object value
    ) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(
                    value.toString()
            );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "No fue posible convertir el valor agregado a long: "
                            + value,
                    exception
            );
        }
    }

    private BigDecimal toBigDecimal(
            Object value
    ) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return new BigDecimal(
                    number.toString()
            );
        }

        try {
            return new BigDecimal(
                    value.toString()
            );

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "No fue posible convertir la confianza promedio: "
                            + value,
                    exception
            );
        }
    }

    private record StatusCounts(

            long total,

            long draft,

            long inReview,

            long approved,

            long published,

            long archived
    ) {
    }
}