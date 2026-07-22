package com.webempresarial.store.knowledge.domain.model;

import com.webempresarial.store.knowledge.domain.value.KnowledgeConfidence;
import com.webempresarial.store.knowledge.domain.value.SemanticVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
 * Representa una versión inmutable e identificable del contenido
 * perteneciente a un KnowledgeObject.
 *
 * <p>La identidad funcional de una versión está formada por:</p>
 *
 * <pre>
 * KnowledgeObject + SemanticVersion
 * </pre>
 */
@Entity
@Table(
        name = "knowledge_object_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_knowledge_version_object_semantic",
                        columnNames = {
                                "knowledge_object_id",
                                "version_major",
                                "version_minor",
                                "version_patch"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_knowledge_version_object",
                        columnList = "knowledge_object_id"
                ),
                @Index(
                        name = "idx_knowledge_version_created_at",
                        columnList = "created_at"
                )
        }
)
public class KnowledgeObjectVersion {

    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_SUMMARY_LENGTH = 1000;
    public static final int MAX_FORMAT_LENGTH = 50;
    public static final int MAX_SOURCE_REFERENCE_LENGTH = 500;
    public static final int MAX_ACTOR_LENGTH = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "knowledge_object_id",
            nullable = false,
            updatable = false
    )
    private KnowledgeObject knowledgeObject;

    @Embedded
    private SemanticVersion semanticVersion;

    @Column(
            name = "title",
            nullable = false,
            length = MAX_TITLE_LENGTH
    )
    private String title;

    @Column(
            name = "summary",
            nullable = false,
            length = MAX_SUMMARY_LENGTH
    )
    private String summary;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String content;

    /**
     * Formato técnico del contenido.
     *
     * <p>Ejemplos iniciales:</p>
     *
     * <pre>
     * MARKDOWN
     * HTML
     * PLAIN_TEXT
     * JSON
     * </pre>
     *
     * <p>Durante KE-01 se conserva como String controlado por
     * validación para no introducir todavía otro enum no confirmado.</p>
     */
    @Column(
            name = "content_format",
            nullable = false,
            length = MAX_FORMAT_LENGTH
    )
    private String contentFormat;

    @Embedded
    private KnowledgeConfidence confidence;

    @Column(
            name = "source_reference",
            length = MAX_SOURCE_REFERENCE_LENGTH
    )
    private String sourceReference;

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
    protected KnowledgeObjectVersion() {
    }

    private KnowledgeObjectVersion(
            KnowledgeObject knowledgeObject,
            SemanticVersion semanticVersion,
            String title,
            String summary,
            String content,
            String contentFormat,
            KnowledgeConfidence confidence,
            String sourceReference,
            String actor
    ) {
        this.knowledgeObject = Objects.requireNonNull(
                knowledgeObject,
                "KnowledgeObject es obligatorio"
        );

        this.semanticVersion = Objects.requireNonNull(
                semanticVersion,
                "SemanticVersion es obligatoria"
        );

        this.title = normalizeRequired(
                title,
                "El título es obligatorio",
                MAX_TITLE_LENGTH
        );

        this.summary = normalizeRequired(
                summary,
                "El resumen es obligatorio",
                MAX_SUMMARY_LENGTH
        );

        this.content = normalizeContent(content);

        this.contentFormat = normalizeFormat(contentFormat);

        this.confidence = Objects.requireNonNull(
                confidence,
                "KnowledgeConfidence es obligatoria"
        );

        this.sourceReference = normalizeOptional(
                sourceReference,
                MAX_SOURCE_REFERENCE_LENGTH
        );

        this.createdBy = normalizeActor(actor);
        this.updatedBy = this.createdBy;
    }

    /**
     * Crea una versión asociada al KnowledgeObject indicado.
     */
    public static KnowledgeObjectVersion create(
            KnowledgeObject knowledgeObject,
            SemanticVersion semanticVersion,
            String title,
            String summary,
            String content,
            String contentFormat,
            KnowledgeConfidence confidence,
            String sourceReference,
            String actor
    ) {
        return new KnowledgeObjectVersion(
                knowledgeObject,
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

    /**
     * Permite modificar el contenido únicamente mientras
     * el KnowledgeObject se encuentre en estado editable.
     */
    public void revise(
            String title,
            String summary,
            String content,
            String contentFormat,
            KnowledgeConfidence confidence,
            String sourceReference,
            String actor
    ) {
        ensureEditable();

        this.title = normalizeRequired(
                title,
                "El título es obligatorio",
                MAX_TITLE_LENGTH
        );

        this.summary = normalizeRequired(
                summary,
                "El resumen es obligatorio",
                MAX_SUMMARY_LENGTH
        );

        this.content = normalizeContent(content);
        this.contentFormat = normalizeFormat(contentFormat);

        this.confidence = Objects.requireNonNull(
                confidence,
                "KnowledgeConfidence es obligatoria"
        );

        this.sourceReference = normalizeOptional(
                sourceReference,
                MAX_SOURCE_REFERENCE_LENGTH
        );

        touch(actor);
    }

    /**
     * Actualiza únicamente el nivel de confianza.
     */
    public void updateConfidence(
            KnowledgeConfidence confidence,
            String actor
    ) {
        ensureEditable();

        this.confidence = Objects.requireNonNull(
                confidence,
                "KnowledgeConfidence es obligatoria"
        );

        touch(actor);
    }

    public boolean belongsTo(KnowledgeObject object) {
        if (object == null || knowledgeObject == null) {
            return false;
        }

        if (knowledgeObject == object) {
            return true;
        }

        return knowledgeObject.getId() != null
                && knowledgeObject.getId().equals(object.getId());
    }

    private void ensureEditable() {
        if (knowledgeObject == null) {
            throw new IllegalStateException(
                    "La versión no tiene KnowledgeObject asociado"
            );
        }

        if (!knowledgeObject.isEditable()) {
            throw new IllegalStateException(
                    "La versión no puede modificarse cuando el KnowledgeObject "
                            + "se encuentra en estado "
                            + knowledgeObject.getStatus()
            );
        }

        if (knowledgeObject.isCurrentVersion(this)) {
            throw new IllegalStateException(
                    "La versión vigente publicada no puede modificarse"
            );
        }
    }

    private static String normalizeRequired(
            String value,
            String requiredMessage,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(requiredMessage);
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "El valor no puede superar "
                            + maxLength
                            + " caracteres"
            );
        }

        return normalized;
    }

    private static String normalizeContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido de la versión es obligatorio"
            );
        }

        return value.trim();
    }

    private static String normalizeFormat(String value) {
        String normalized = normalizeRequired(
                value,
                "El formato del contenido es obligatorio",
                MAX_FORMAT_LENGTH
        );

        return normalized
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static String normalizeOptional(
            String value,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "El valor no puede superar "
                            + maxLength
                            + " caracteres"
            );
        }

        return normalized;
    }

    private static String normalizeActor(String actor) {
        return normalizeRequired(
                actor,
                "El actor es obligatorio",
                MAX_ACTOR_LENGTH
        );
    }

    private void touch(String actor) {
        this.updatedBy = normalizeActor(actor);
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public KnowledgeObject getKnowledgeObject() {
        return knowledgeObject;
    }

    public SemanticVersion getSemanticVersion() {
        return semanticVersion;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getContentFormat() {
        return contentFormat;
    }

    public KnowledgeConfidence getConfidence() {
        return confidence;
    }

    public String getSourceReference() {
        return sourceReference;
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