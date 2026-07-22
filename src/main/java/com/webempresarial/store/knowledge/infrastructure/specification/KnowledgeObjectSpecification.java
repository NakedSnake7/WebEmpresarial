package com.webempresarial.store.knowledge.infrastructure.specification;

import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Construye filtros JPA dinámicos para KnowledgeObject.
 *
 * <p>La condición storeId es obligatoria y se agrega siempre,
 * preservando el aislamiento multi-tenant.</p>
 */
public final class KnowledgeObjectSpecification {

    private KnowledgeObjectSpecification() {
    }

    public static Specification<KnowledgeObject> from(
            KnowledgeQueryCriteria criteria
    ) {
        Objects.requireNonNull(
                criteria,
                "KnowledgeQueryCriteria es obligatorio"
        );

        return (root, query, builder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            /*
             * Frontera tenant obligatoria.
             */
            predicates.add(
                    builder.equal(
                            root.get("store").get("id"),
                            criteria.storeId()
                    )
            );

            if (criteria.hasCode()) {
                predicates.add(
                        builder.equal(
                                root.get("code").get("value"),
                                normalizeCode(criteria.code())
                        )
                );
            }

            if (criteria.typeCode() != null) {
                predicates.add(
                        builder.equal(
                                root.get("typeCode"),
                                criteria.typeCode()
                        )
                );
            }

            if (criteria.domain() != null) {
                predicates.add(
                        builder.equal(
                                root.get("domain"),
                                criteria.domain()
                        )
                );
            }

            if (criteria.classification() != null) {
                predicates.add(
                        builder.equal(
                                root.get("classification"),
                                criteria.classification()
                        )
                );
            }

            if (criteria.riskLevel() != null) {
                predicates.add(
                        builder.equal(
                                root.get("riskLevel"),
                                criteria.riskLevel()
                        )
                );
            }

            if (criteria.status() != null) {
                predicates.add(
                        builder.equal(
                                root.get("status"),
                                criteria.status()
                        )
                );
            }

            if (criteria.hasContext()) {
                predicates.add(
                        builder.equal(
                                root.get("contextRoot").get("type"),
                                criteria.contextType()
                        )
                );

                predicates.add(
                        builder.equal(
                                root.get("contextRoot")
                                        .get("reference"),
                                normalizeContextReference(
                                        criteria.contextReference()
                                )
                        )
                );
            }

            Join<KnowledgeObject, KnowledgeObjectVersion>
                    currentVersion = null;

            if (criteria.requiresVersionJoin()) {
                currentVersion = root.join(
                        "currentVersion",
                        JoinType.INNER
                );
            }

            if (criteria.hasMinimumConfidence()) {
                predicates.add(
                        builder.greaterThanOrEqualTo(
                                currentVersion
                                        .get("confidence")
                                        .get("value"),
                                criteria.minimumConfidence()
                        )
                );
            }

            if (criteria.hasEffectiveMoment()) {
                addEffectiveAtPredicates(
                        predicates,
                        root,
                        builder,
                        criteria.effectiveAt()
                );
            }

            if (criteria.hasText()) {
                predicates.add(
                        buildTextPredicate(
                                root,
                                currentVersion,
                                builder,
                                criteria.text()
                        )
                );
            }

            /*
             * Evita duplicados si en el futuro se agregan joins
             * hacia colecciones.
             */
            query.distinct(true);

            return builder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }

    private static void addEffectiveAtPredicates(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Root<KnowledgeObject> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            LocalDateTime moment
    ) {
        /*
         * Consultar conocimiento efectivo implica necesariamente
         * que esté publicado y tenga versión vigente.
         */
        predicates.add(
                builder.equal(
                        root.get("status"),
                        KnowledgeStatus.PUBLISHED
                )
        );

        predicates.add(
                builder.isNotNull(
                        root.get("currentVersion")
                )
        );

        predicates.add(
                builder.isNotNull(
                        root.get("validFrom")
                )
        );

        predicates.add(
                builder.lessThanOrEqualTo(
                        root.get("validFrom"),
                        moment
                )
        );

        predicates.add(
                builder.or(
                        builder.isNull(
                                root.get("validUntil")
                        ),
                        builder.greaterThan(
                                root.get("validUntil"),
                                moment
                        )
                )
        );
    }

    private static Predicate buildTextPredicate(
            jakarta.persistence.criteria.Root<KnowledgeObject> root,
            Join<KnowledgeObject, KnowledgeObjectVersion> currentVersion,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            String text
    ) {
        String pattern =
                "%"
                        + escapeLike(
                                text.trim()
                                        .toLowerCase(Locale.ROOT)
                        )
                        + "%";

        return builder.or(
                builder.like(
                        builder.lower(
                                root.get("code").get("value")
                        ),
                        pattern,
                        '\\'
                ),
                builder.like(
                        builder.lower(
                                currentVersion.get("title")
                        ),
                        pattern,
                        '\\'
                ),
                builder.like(
                        builder.lower(
                                currentVersion.get("summary")
                        ),
                        pattern,
                        '\\'
                )
        );
    }

    private static String normalizeCode(String code) {
        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private static String normalizeContextReference(
            String reference
    ) {
        return reference
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9._-]", "-")
                .replaceAll("-{2,}", "-");
    }

    /**
     * Evita que %, _ y \ recibidos como texto se interpreten
     * como comodines SQL LIKE.
     */
    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}