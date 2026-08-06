package com.webempresarial.store.digitaltransformation.domain.traceability;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_traceability_nodes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_trace_node_project_code",
                        columnNames = {
                                "project_id",
                                "node_code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_transformation_trace_node_external_ref",
                        columnNames = {
                                "project_id",
                                "node_type",
                                "external_reference"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_trace_node_project_type",
                        columnList = "project_id,node_type"
                ),
                @Index(
                        name = "idx_transformation_trace_node_project_status",
                        columnList = "project_id,status"
                ),
                @Index(
                        name = "idx_transformation_trace_node_external_ref",
                        columnList = "external_reference"
                )
        }
)
public class TraceabilityNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_trace_node_project"
            )
    )
    private TransformationProject project;

    @Column(
            name = "node_code",
            nullable = false,
            length = 100
    )
    private String nodeCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "node_type",
            nullable = false,
            length = 60
    )
    private TraceabilityNodeType nodeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TraceabilityNodeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TraceabilityOrigin origin;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(
            name = "external_reference",
            nullable = false,
            length = 255
    )
    private String externalReference;

    @Column(
            name = "external_entity_type",
            nullable = false,
            length = 180
    )
    private String externalEntityType;

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

    protected TraceabilityNode() {
    }

    private TraceabilityNode(
            TransformationProject project,
            String nodeCode,
            TraceabilityNodeType nodeType,
            TraceabilityOrigin origin,
            String title,
            String description,
            String externalReference,
            String externalEntityType,
            boolean requiresReview
    ) {
        this.project = Objects.requireNonNull(
                project,
                "El proyecto es obligatorio"
        );

        this.nodeCode = normalizeCode(nodeCode);

        this.nodeType = Objects.requireNonNull(
                nodeType,
                "El tipo de nodo es obligatorio"
        );

        this.origin = Objects.requireNonNull(
                origin,
                "El origen del nodo es obligatorio"
        );

        this.title = normalizeRequired(
                title,
                "El título del nodo es obligatorio",
                500
        );

        this.description =
                normalizeOptional(description, 4000);

        this.externalReference = normalizeRequired(
                externalReference,
                "La referencia externa es obligatoria",
                255
        );

        this.externalEntityType = normalizeRequired(
                externalEntityType,
                "El tipo de entidad externa es obligatorio",
                180
        );

        this.status = TraceabilityNodeStatus.DRAFT;
        this.requiresReview = requiresReview;
    }

    public static TraceabilityNode create(
            TransformationProject project,
            String nodeCode,
            TraceabilityNodeType nodeType,
            TraceabilityOrigin origin,
            String title,
            String description,
            String externalReference,
            String externalEntityType,
            boolean requiresReview
    ) {
        return new TraceabilityNode(
                project,
                nodeCode,
                nodeType,
                origin,
                title,
                description,
                externalReference,
                externalEntityType,
                requiresReview
        );
    }

    public void activate() {
        if (status != TraceabilityNodeStatus.DRAFT) {
            throw new IllegalStateException(
                    "Solo un nodo en borrador puede activarse"
            );
        }

        this.status = TraceabilityNodeStatus.ACTIVE;
    }

    public void verify(String verifiedBy) {
        if (status != TraceabilityNodeStatus.DRAFT
                && status != TraceabilityNodeStatus.ACTIVE) {
            throw new IllegalStateException(
                    "El nodo no puede verificarse desde el estado " +
                    status
            );
        }

        this.verifiedBy = normalizeRequired(
                verifiedBy,
                "El responsable de verificación es obligatorio",
                180
        );

        this.status = TraceabilityNodeStatus.VERIFIED;
        this.requiresReview = false;
        this.verifiedAt = Instant.now();
        this.rejectionReason = null;
        this.rejectedAt = null;
    }

    public void reject(
            String reason,
            String reviewedBy
    ) {
        if (status == TraceabilityNodeStatus.ARCHIVED
                || status == TraceabilityNodeStatus.SUPERSEDED) {
            throw new IllegalStateException(
                    "El nodo no puede rechazarse desde el estado " +
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

        this.status = TraceabilityNodeStatus.REJECTED;
        this.requiresReview = false;
        this.rejectedAt = Instant.now();
    }

    public void supersede() {
        if (status != TraceabilityNodeStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo un nodo verificado puede ser sustituido"
            );
        }

        this.status = TraceabilityNodeStatus.SUPERSEDED;
    }

    public void archive() {
        this.status = TraceabilityNodeStatus.ARCHIVED;
        this.requiresReview = false;
    }

    public boolean canParticipateInVerifiedTraceability() {
        return status == TraceabilityNodeStatus.VERIFIED;
    }

    public void ensureBelongsToProject(
            TransformationProject expectedProject
    ) {
        Objects.requireNonNull(
                expectedProject,
                "El proyecto esperado es obligatorio"
        );

        if (!sameProject(project, expectedProject)) {
            throw new IllegalArgumentException(
                    "El nodo no pertenece al proyecto indicado"
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

        if (first.getId() != null && second.getId() != null) {
            return first.getId().equals(second.getId());
        }

        return first == second;
    }

    private static String normalizeCode(String value) {
        String normalized = normalizeRequired(
                value,
                "El código del nodo es obligatorio",
                100
        ).toUpperCase(Locale.ROOT);

        if (!normalized.matches(
                "^[A-Z0-9][A-Z0-9_.:-]{2,99}$"
        )) {
            throw new IllegalArgumentException(
                    "El código del nodo no tiene un formato válido"
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
            this.status = TraceabilityNodeStatus.DRAFT;
        }
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public TraceabilityNodeType getNodeType() {
        return nodeType;
    }

    public TraceabilityNodeStatus getStatus() {
        return status;
    }

    public TraceabilityOrigin getOrigin() {
        return origin;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getExternalEntityType() {
        return externalEntityType;
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