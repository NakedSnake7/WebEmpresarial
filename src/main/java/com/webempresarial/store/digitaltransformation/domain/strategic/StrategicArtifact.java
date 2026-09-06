package com.webempresarial.store.digitaltransformation.domain.strategic;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence; 
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_strategic_artifacts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_strategic_project_code",
                        columnNames = {
                                "project_id",
                                "artifact_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_strategic_project_type",
                        columnList = "project_id,artifact_type"
                ),
                @Index(
                        name = "idx_transformation_strategic_project_status",
                        columnList = "project_id,status"
                ),
                @Index(
                        name = "idx_transformation_strategic_priority",
                        columnList = "project_id,priority"
                )
        }
)
public class StrategicArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_strategic_project"
            )
    )
    private TransformationProject project;

    @Column(
            name = "artifact_code",
            nullable = false,
            length = 80
    )
    private String artifactCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "artifact_type",
            nullable = false,
            length = 60
    )
    private StrategicArtifactType artifactType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StrategicArtifactStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StrategicConfidence confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StrategicArtifactOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StrategicPriority priority;

    @Column(nullable = false, length = 1000)
    private String statement;

    @Column(length = 4000)
    private String rationale;

    @Column(
            name = "business_implication",
            length = 4000
    )
    private String businessImplication;

    @Column(
            name = "requires_review",
            nullable = false
    )
    private boolean requiresReview;

    @Column(
            name = "verified_by",
            length = 180
    )
    private String verifiedBy;

    @Column(
            name = "rejection_reason",
            length = 2000
    )
    private String rejectionReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_evidence_id",
            foreignKey = @ForeignKey(
                    name = "fk_strategic_artifact_source_evidence"
            )
    )
    private SourceEvidence sourceEvidence;

    protected StrategicArtifact() {
    }

    private StrategicArtifact(
            TransformationProject project,
            String artifactCode,
            StrategicArtifactType artifactType,
            StrategicConfidence confidence,
            StrategicArtifactOrigin origin,
            String statement,
            String rationale,
            String businessImplication
    ) {
        this.project = Objects.requireNonNull(
                project,
                "El proyecto es obligatorio"
        );

        this.artifactCode =
                normalizeCode(artifactCode);

        this.artifactType =
                Objects.requireNonNull(
                        artifactType,
                        "El tipo de artefacto es obligatorio"
                );

        this.confidence =
                Objects.requireNonNull(
                        confidence,
                        "La confianza es obligatoria"
                );

        this.origin =
                Objects.requireNonNull(
                        origin,
                        "El origen es obligatorio"
                );

        this.statement =
                normalizeRequired(
                        statement,
                        "La afirmación estratégica es obligatoria",
                        1000
                );

        this.rationale =
                normalizeOptional(
                        rationale,
                        4000
                );

        this.businessImplication =
                normalizeOptional(
                        businessImplication,
                        4000
                );

        this.status =
                StrategicArtifactStatus.DRAFT;

        this.priority =
                StrategicPriority.UNASSESSED;

        this.requiresReview =
                confidence == StrategicConfidence.INFERRED
                || confidence == StrategicConfidence.WEAKLY_SUPPORTED
                || confidence == StrategicConfidence.UNCERTAIN
                || origin == StrategicArtifactOrigin.AI_ASSISTED;
    }

    public static StrategicArtifact create(
            TransformationProject project,
            String artifactCode,
            StrategicArtifactType artifactType,
            StrategicConfidence confidence,
            StrategicArtifactOrigin origin,
            String statement,
            String rationale,
            String businessImplication
    ) {
        return new StrategicArtifact(
                project,
                artifactCode,
                artifactType,
                confidence,
                origin,
                statement,
                rationale,
                businessImplication
        );
    }

    public void requireReview() {
        if (status == StrategicArtifactStatus.REJECTED
                || status == StrategicArtifactStatus.SUPERSEDED
                || status == StrategicArtifactStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "El artefacto no puede enviarse a revisión desde " +
                    status
            );
        }

        this.status =
                StrategicArtifactStatus.REVIEW_REQUIRED;

        this.requiresReview = true;
    }

    public void verify(String verifiedBy) {
        if (status != StrategicArtifactStatus.DRAFT
                && status != StrategicArtifactStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException(
                    "El artefacto no puede verificarse desde " +
                    status
            );
        }

        this.verifiedBy =
                normalizeRequired(
                        verifiedBy,
                        "El responsable de verificación es obligatorio",
                        180
                );

        this.status =
                StrategicArtifactStatus.VERIFIED;

        this.requiresReview = false;
        this.verifiedAt = Instant.now();
        this.rejectionReason = null;
        this.rejectedAt = null;
    }

    public void reject(
            String reason,
            String reviewedBy
    ) {
        if (status == StrategicArtifactStatus.REJECTED
                || status == StrategicArtifactStatus.SUPERSEDED
                || status == StrategicArtifactStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "El artefacto no puede rechazarse desde " +
                    status
            );
        }

        this.rejectionReason =
                normalizeRequired(
                        reason,
                        "La razón del rechazo es obligatoria",
                        2000
                );

        this.verifiedBy =
                normalizeRequired(
                        reviewedBy,
                        "El responsable de revisión es obligatorio",
                        180
                );

        this.status =
                StrategicArtifactStatus.REJECTED;

        this.requiresReview = false;
        this.rejectedAt = Instant.now();
    }

    public void supersede() {
        if (status != StrategicArtifactStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo un artefacto verificado puede ser sustituido"
            );
        }

        this.status =
                StrategicArtifactStatus.SUPERSEDED;

        this.requiresReview = false;
    }

    public void archive() {
        if (status == StrategicArtifactStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "El artefacto ya está archivado"
            );
        }

        this.status =
                StrategicArtifactStatus.ARCHIVED;

        this.requiresReview = false;
    }

    public void assignPriority(
            StrategicPriority priority
    ) {
        this.priority =
                Objects.requireNonNull(
                        priority,
                        "La prioridad es obligatoria"
                );
    }

    public boolean canDriveImplementation() {
        return status == StrategicArtifactStatus.VERIFIED
                && confidence != StrategicConfidence.UNCERTAIN;
    }

    public void ensureBelongsToProject(
            TransformationProject expectedProject
    ) {
        Objects.requireNonNull(
                expectedProject,
                "El proyecto esperado es obligatorio"
        );

        if (!sameProject(
                project,
                expectedProject
        )) {
            throw new IllegalArgumentException(
                    "El artefacto estratégico no pertenece al proyecto"
            );
        }
    }

    private static boolean sameProject(
            TransformationProject first,
            TransformationProject second
    ) {
        if (first == null || second == null) {
            return false;
        }

        if (first.getId() != null
                && second.getId() != null) {
            return first.getId()
                    .equals(second.getId());
        }

        return first == second;
    }

    private static String normalizeCode(
            String value
    ) {
        String normalized =
                normalizeRequired(
                        value,
                        "El código es obligatorio",
                        80
                )
                        .toUpperCase(Locale.ROOT);

        if (!normalized.matches(
                "^[A-Z0-9][A-Z0-9_-]{2,79}$"
        )) {
            throw new IllegalArgumentException(
                    "El código estratégico no tiene un formato válido"
            );
        }

        return normalized;
    }

    private static String normalizeRequired(
            String value,
            String message,
            int maxLength
    ) {
        String normalized =
                normalizeOptional(
                        value,
                        maxLength
                );

        if (normalized == null) {
            throw new IllegalArgumentException(
                    message
            );
        }

        return normalized;
    }

    private static String normalizeOptional(
            String value,
            int maxLength
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "El valor supera la longitud máxima de " +
                    maxLength +
                    " caracteres"
            );
        }

        return normalized;
    }
    
    public static StrategicArtifact deriveFromEvidence(
            TransformationProject project,
            SourceEvidence sourceEvidence,
            String artifactCode,
            StrategicArtifactType artifactType,
            StrategicConfidence confidence,
            StrategicArtifactOrigin origin,
            String statement,
            String rationale,
            String businessImplication
    ) {
        Objects.requireNonNull(
                sourceEvidence,
                "La evidencia fuente es obligatoria"
        );

        sourceEvidence.ensureBelongsToProject(
                project
        );

        StrategicArtifact artifact =
                new StrategicArtifact(
                        project,
                        artifactCode,
                        artifactType,
                        confidence,
                        origin,
                        statement,
                        rationale,
                        businessImplication
                );

        artifact.sourceEvidence =
                sourceEvidence;

        return artifact;
    }

    @PrePersist
    void onCreate() {
        Instant now =
                Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (status == null) {
            this.status =
                    StrategicArtifactStatus.DRAFT;
        }

        if (priority == null) {
            this.priority =
                    StrategicPriority.UNASSESSED;
        }
    }
    


    @PreUpdate
    void onUpdate() {
        this.updatedAt =
                Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public String getArtifactCode() {
        return artifactCode;
    }

    public StrategicArtifactType getArtifactType() {
        return artifactType;
    }

    public StrategicArtifactStatus getStatus() {
        return status;
    }

    public StrategicConfidence getConfidence() {
        return confidence;
    }

    public StrategicArtifactOrigin getOrigin() {
        return origin;
    }

    public StrategicPriority getPriority() {
        return priority;
    }

    public String getStatement() {
        return statement;
    }

    public String getRationale() {
        return rationale;
    }

    public String getBusinessImplication() {
        return businessImplication;
    }

    public boolean isRequiresReview() {
        return requiresReview;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }
    public SourceEvidence getSourceEvidence() {
        return sourceEvidence;
    }
}