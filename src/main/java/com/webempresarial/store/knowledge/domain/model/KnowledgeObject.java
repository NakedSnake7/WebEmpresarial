package com.webempresarial.store.knowledge.domain.model;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.domain.value.KnowledgeContextRoot;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.model.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregado raíz del primer vertical de Knowledge Engine.
 *
 * <p>KnowledgeObject representa la identidad gobernada del
 * conocimiento. El contenido editorial vive en
 * KnowledgeObjectVersion.</p>
 */
@Entity
@Table(
        name = "knowledge_objects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_knowledge_object_store_code",
                        columnNames = {
                                "store_id",
                                "code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_knowledge_object_store_status",
                        columnList = "store_id, status"
                ),
                @Index(
                        name = "idx_knowledge_object_store_domain",
                        columnList = "store_id, domain"
                ),
                @Index(
                        name = "idx_knowledge_object_store_context",
                        columnList = "store_id, context_type, context_id"
                ),
                @Index(
                        name = "idx_knowledge_object_current_version",
                        columnList = "current_version_id"
                )
        }
)
public class KnowledgeObject {

    public static final int MAX_ACTOR_LENGTH = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "store_id",
            nullable = false,
            updatable = false
    )
    private Store store;

    @Embedded
    private KnowledgeCode code;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "type_code",
            nullable = false,
            length = 50
    )
    private KnowledgeTypeCode typeCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "domain",
            nullable = false,
            length = 50
    )
    private KnowledgeDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "classification",
            nullable = false,
            length = 50
    )
    private KnowledgeClassification classification;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "risk_level",
            nullable = false,
            length = 30
    )
    private KnowledgeRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private KnowledgeStatus status;

    @Embedded
    private KnowledgeContextRoot contextRoot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private KnowledgeObjectVersion currentVersion;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "created_by",
            nullable = false,
            updatable = false,
            length = MAX_ACTOR_LENGTH
    )
    private String createdBy;

    @Column(
            name = "updated_by",
            nullable = false,
            length = MAX_ACTOR_LENGTH
    )
    private String updatedBy;

    @Version
    @Column(
            name = "lock_version",
            nullable = false
    )
    private Long lockVersion;

    /**
     * Constructor requerido por JPA.
     */
    protected KnowledgeObject() {
    }

    private KnowledgeObject(
            Store store,
            KnowledgeCode code,
            KnowledgeTypeCode typeCode,
            KnowledgeDomain domain,
            KnowledgeClassification classification,
            KnowledgeRiskLevel riskLevel,
            KnowledgeContextRoot contextRoot,
            String actor
    ) {
        this.store = Objects.requireNonNull(
                store,
                "Store es obligatoria"
        );

        this.code = Objects.requireNonNull(
                code,
                "KnowledgeCode es obligatorio"
        );

        this.typeCode = Objects.requireNonNull(
                typeCode,
                "KnowledgeTypeCode es obligatorio"
        );

        this.domain = Objects.requireNonNull(
                domain,
                "KnowledgeDomain es obligatorio"
        );

        this.classification = Objects.requireNonNull(
                classification,
                "KnowledgeClassification es obligatoria"
        );

        this.riskLevel = Objects.requireNonNull(
                riskLevel,
                "KnowledgeRiskLevel es obligatorio"
        );

        this.contextRoot = Objects.requireNonNull(
                contextRoot,
                "KnowledgeContextRoot es obligatorio"
        );

        this.status = KnowledgeStatus.initial();
        this.createdBy = normalizeActor(actor);
        this.updatedBy = this.createdBy;
    }

    public static KnowledgeObject create(
            Store store,
            KnowledgeCode code,
            KnowledgeTypeCode typeCode,
            KnowledgeDomain domain,
            KnowledgeClassification classification,
            KnowledgeRiskLevel riskLevel,
            KnowledgeContextRoot contextRoot,
            String actor
    ) {
        return new KnowledgeObject(
                store,
                code,
                typeCode,
                domain,
                classification,
                riskLevel,
                contextRoot,
                actor
        );
    }

    /**
     * Factory para crear una nueva versión perteneciente
     * a este agregado.
     */
    public KnowledgeObjectVersion createVersion(
            SemanticVersion semanticVersion,
            String title,
            String summary,
            String content,
            String contentFormat,
            KnowledgeConfidence confidence,
            String sourceReference,
            String actor
    ) {
        if (!isEditable()) {
            throw new IllegalStateException(
                    "No pueden crearse versiones cuando el objeto está en estado "
                            + status
            );
        }

        return KnowledgeObjectVersion.create(
                this,
                semanticVersion,
                title,
                summary,
                content,
                contentFormat,
                confidence,
                sourceReference,
                actor
        );
    }

    public void updateGovernance(
            KnowledgeDomain domain,
            KnowledgeClassification classification,
            KnowledgeRiskLevel riskLevel,
            KnowledgeContextRoot contextRoot,
            String actor
    ) {
        ensureEditable();

        this.domain = Objects.requireNonNull(
                domain,
                "KnowledgeDomain es obligatorio"
        );

        this.classification = Objects.requireNonNull(
                classification,
                "KnowledgeClassification es obligatoria"
        );

        this.riskLevel = Objects.requireNonNull(
                riskLevel,
                "KnowledgeRiskLevel es obligatorio"
        );

        this.contextRoot = Objects.requireNonNull(
                contextRoot,
                "KnowledgeContextRoot es obligatorio"
        );

        touch(actor);
    }

    public void submitForReview(String actor) {
        transitionTo(KnowledgeStatus.IN_REVIEW, actor);
    }

    public void returnToDraft(String actor) {
        transitionTo(KnowledgeStatus.DRAFT, actor);
    }

    public void approve(String actor) {
        transitionTo(KnowledgeStatus.APPROVED, actor);
    }

    public void publish(
            KnowledgeObjectVersion version,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            String actor
    ) {
        if (status != KnowledgeStatus.APPROVED) {
            throw new IllegalStateException(
                    "Solo un KnowledgeObject aprobado puede publicarse"
            );
        }

        validateVersionOwnership(version);
        validateValidity(validFrom, validUntil);

        this.currentVersion = version;
        this.validFrom = validFrom;
        this.validUntil = validUntil;

        transitionTo(KnowledgeStatus.PUBLISHED, actor);
    }

    public void replaceCurrentVersion(
            KnowledgeObjectVersion version,
            String actor
    ) {
        if (status != KnowledgeStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Solo un KnowledgeObject publicado puede cambiar "
                            + "su versión vigente"
            );
        }

        validateVersionOwnership(version);

        if (currentVersion != null
                && !version.getSemanticVersion()
                .isNewerThan(currentVersion.getSemanticVersion())) {

            throw new IllegalArgumentException(
                    "La nueva versión vigente debe ser posterior a "
                            + currentVersion.getSemanticVersion()
            );
        }

        this.currentVersion = version;
        touch(actor);
    }

    public void archive(String actor) {
        transitionTo(KnowledgeStatus.ARCHIVED, actor);
    }

    public void restore(String actor) {
        transitionTo(KnowledgeStatus.PUBLISHED, actor);
    }

    public void retire(String actor) {
        transitionTo(KnowledgeStatus.RETIRED, actor);
    }

    private void transitionTo(
            KnowledgeStatus target,
            String actor
    ) {
        status.validateTransitionTo(target);
        status = target;
        touch(actor);
    }

    private void validateVersionOwnership(
            KnowledgeObjectVersion version
    ) {
        Objects.requireNonNull(
                version,
                "KnowledgeObjectVersion es obligatoria"
        );

        if (!version.belongsTo(this)) {
            throw new IllegalArgumentException(
                    "La versión no pertenece al KnowledgeObject"
            );
        }
    }

    private static void validateValidity(
            LocalDateTime validFrom,
            LocalDateTime validUntil
    ) {
        if (validFrom == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio de vigencia es obligatoria"
            );
        }

        if (validUntil != null
                && !validUntil.isAfter(validFrom)) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia debe ser posterior "
                            + "a la fecha inicial"
            );
        }
    }

    private void ensureEditable() {
        if (!isEditable()) {
            throw new IllegalStateException(
                    "KnowledgeObject no es editable en estado "
                            + status
            );
        }
    }

    private static String normalizeActor(String actor) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor es obligatorio"
            );
        }

        String normalized = actor.trim();

        if (normalized.length() > MAX_ACTOR_LENGTH) {
            throw new IllegalArgumentException(
                    "El actor no puede superar "
                            + MAX_ACTOR_LENGTH
                            + " caracteres"
            );
        }

        return normalized;
    }

    private void touch(String actor) {
        updatedBy = normalizeActor(actor);
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = KnowledgeStatus.initial();
        }

        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean belongsTo(Store candidateStore) {
        if (candidateStore == null || store == null) {
            return false;
        }

        if (candidateStore == store) {
            return true;
        }

        return candidateStore.getId() != null
                && candidateStore.getId().equals(store.getId());
    }

    public boolean isEditable() {
        return status != null && status.isEditable();
    }

    public boolean isCurrentVersion(
            KnowledgeObjectVersion version
    ) {
        if (version == null || currentVersion == null) {
            return false;
        }

        if (currentVersion == version) {
            return true;
        }

        return currentVersion.getId() != null
                && currentVersion.getId().equals(version.getId());
    }

    public boolean isPublishedAt(LocalDateTime moment) {
        if (status != KnowledgeStatus.PUBLISHED
                || currentVersion == null
                || moment == null) {

            return false;
        }

        boolean alreadyStarted =
                validFrom == null
                        || !moment.isBefore(validFrom);

        boolean notExpired =
                validUntil == null
                        || moment.isBefore(validUntil);

        return alreadyStarted && notExpired;
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public KnowledgeCode getCode() {
        return code;
    }

    public KnowledgeTypeCode getTypeCode() {
        return typeCode;
    }

    public KnowledgeDomain getDomain() {
        return domain;
    }

    public KnowledgeClassification getClassification() {
        return classification;
    }

    public KnowledgeRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public KnowledgeStatus getStatus() {
        return status;
    }

    public KnowledgeContextRoot getContextRoot() {
        return contextRoot;
    }

    public KnowledgeObjectVersion getCurrentVersion() {
        return currentVersion;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Long getLockVersion() {
        return lockVersion;
    }
}