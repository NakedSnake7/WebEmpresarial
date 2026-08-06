package com.webempresarial.store.digitaltransformation.domain.traceability;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_traceability_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_trace_link",
                        columnNames = {
                                "project_id",
                                "source_node_id",
                                "target_node_id",
                                "relation_type"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_trace_link_source",
                        columnList = "source_node_id,status"
                ),
                @Index(
                        name = "idx_transformation_trace_link_target",
                        columnList = "target_node_id,status"
                ),
                @Index(
                        name = "idx_transformation_trace_link_relation",
                        columnList = "project_id,relation_type"
                )
        }
)
public class TraceabilityLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_trace_link_project"
            )
    )
    private TransformationProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_node_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_trace_link_source"
            )
    )
    private TraceabilityNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "target_node_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_trace_link_target"
            )
    )
    private TraceabilityNode targetNode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "relation_type",
            nullable = false,
            length = 50
    )
    private TraceabilityRelationType relationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TraceabilityStrength strength;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TraceabilityLinkStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TraceabilityOrigin origin;

    @Column(length = 4000)
    private String rationale;

    @Column(name = "requires_review", nullable = false)
    private boolean requiresReview;

    @Column(name = "verified_by", length = 180)
    private String verifiedBy;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    protected TraceabilityLink() {
    }

    private TraceabilityLink(
            TransformationProject project,
            TraceabilityNode sourceNode,
            TraceabilityNode targetNode,
            TraceabilityRelationType relationType,
            TraceabilityStrength strength,
            TraceabilityOrigin origin,
            String rationale
    ) {
        this.project = Objects.requireNonNull(
                project,
                "El proyecto es obligatorio"
        );

        this.sourceNode = Objects.requireNonNull(
                sourceNode,
                "El nodo origen es obligatorio"
        );

        this.targetNode = Objects.requireNonNull(
                targetNode,
                "El nodo destino es obligatorio"
        );

        validateNodeRelationships(
                project,
                sourceNode,
                targetNode
        );

        this.relationType = Objects.requireNonNull(
                relationType,
                "El tipo de relación es obligatorio"
        );

        this.strength = Objects.requireNonNull(
                strength,
                "La fuerza de la relación es obligatoria"
        );

        this.origin = Objects.requireNonNull(
                origin,
                "El origen de la relación es obligatorio"
        );

        this.rationale =
                normalizeOptional(rationale, 4000);

        this.status = TraceabilityLinkStatus.PROPOSED;

        this.requiresReview =
                strength == TraceabilityStrength.WEAK
                || strength == TraceabilityStrength.UNCERTAIN
                || origin == TraceabilityOrigin.AI_ASSISTED
                || relationType == TraceabilityRelationType.INFERRED_FROM;
    }

    public static TraceabilityLink create(
            TransformationProject project,
            TraceabilityNode sourceNode,
            TraceabilityNode targetNode,
            TraceabilityRelationType relationType,
            TraceabilityStrength strength,
            TraceabilityOrigin origin,
            String rationale
    ) {
        return new TraceabilityLink(
                project,
                sourceNode,
                targetNode,
                relationType,
                strength,
                origin,
                rationale
        );
    }

    public void activate() {
        if (status != TraceabilityLinkStatus.PROPOSED) {
            throw new IllegalStateException(
                    "Solo una relación propuesta puede activarse"
            );
        }

        this.status = TraceabilityLinkStatus.ACTIVE;
    }

    public void verify(String verifiedBy) {
        if (status != TraceabilityLinkStatus.PROPOSED
                && status != TraceabilityLinkStatus.ACTIVE) {
            throw new IllegalStateException(
                    "La relación no puede verificarse desde el estado " +
                    status
            );
        }

        if (!sourceNode.canParticipateInVerifiedTraceability()
                || !targetNode.canParticipateInVerifiedTraceability()) {
            throw new IllegalStateException(
                    "Ambos nodos deben estar verificados antes de " +
                    "verificar la relación"
            );
        }

        this.verifiedBy = normalizeRequired(
                verifiedBy,
                "El responsable de verificación es obligatorio",
                180
        );

        this.status = TraceabilityLinkStatus.VERIFIED;
        this.requiresReview = false;
        this.verifiedAt = Instant.now();
        this.rejectionReason = null;
        this.rejectedAt = null;
    }

    public void reject(
            String reason,
            String reviewedBy
    ) {
        if (status == TraceabilityLinkStatus.ARCHIVED
                || status == TraceabilityLinkStatus.SUPERSEDED) {
            throw new IllegalStateException(
                    "La relación no puede rechazarse desde el estado " +
                    status
            );
        }

        this.rejectionReason = normalizeRequired(
                reason,
                "La razón del rechazo es obligatoria",
                2000
        );

        this.verifiedBy = normalizeRequired(
                reviewedBy,
                "El responsable de revisión es obligatorio",
                180
        );

        this.status = TraceabilityLinkStatus.REJECTED;
        this.requiresReview = false;
        this.rejectedAt = Instant.now();
    }

    public void supersede() {
        if (status != TraceabilityLinkStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo una relación verificada puede ser sustituida"
            );
        }

        this.status = TraceabilityLinkStatus.SUPERSEDED;
    }

    public void archive() {
        this.status = TraceabilityLinkStatus.ARCHIVED;
        this.requiresReview = false;
    }

    public boolean isVerifiedTrace() {
        return status == TraceabilityLinkStatus.VERIFIED;
    }

    private static void validateNodeRelationships(
            TransformationProject project,
            TraceabilityNode sourceNode,
            TraceabilityNode targetNode
    ) {
        sourceNode.ensureBelongsToProject(project);
        targetNode.ensureBelongsToProject(project);

        if (sameNode(sourceNode, targetNode)) {
            throw new IllegalArgumentException(
                    "Un nodo no puede relacionarse consigo mismo"
            );
        }
    }

    private static boolean sameNode(
            TraceabilityNode first,
            TraceabilityNode second
    ) {
        if (first.getId() != null && second.getId() != null) {
            return first.getId().equals(second.getId());
        }

        return first == second;
    }

    private static String normalizeRequired(
            String value,
            String message,
            int maxLength
    ) {
        String normalized =
                normalizeOptional(value, maxLength);

        if (normalized == null) {
            throw new IllegalArgumentException(message);
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

        String normalized = value.trim();

        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "El valor supera la longitud máxima de " +
                    maxLength + " caracteres"
            );
        }

        return normalized;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();

        if (status == null) {
            this.status = TraceabilityLinkStatus.PROPOSED;
        }
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public TraceabilityNode getSourceNode() {
        return sourceNode;
    }

    public TraceabilityNode getTargetNode() {
        return targetNode;
    }

    public TraceabilityRelationType getRelationType() {
        return relationType;
    }

    public TraceabilityStrength getStrength() {
        return strength;
    }

    public TraceabilityLinkStatus getStatus() {
        return status;
    }

    public TraceabilityOrigin getOrigin() {
        return origin;
    }

    public String getRationale() {
        return rationale;
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

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }
}