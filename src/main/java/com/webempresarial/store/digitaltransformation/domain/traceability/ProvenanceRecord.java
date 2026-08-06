package com.webempresarial.store.digitaltransformation.domain.traceability;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_provenance_records",
        indexes = {
                @Index(
                        name = "idx_transformation_provenance_project",
                        columnList = "project_id,recorded_at"
                ),
                @Index(
                        name = "idx_transformation_provenance_node",
                        columnList = "traceability_node_id,recorded_at"
                ),
                @Index(
                        name = "idx_transformation_provenance_actor",
                        columnList = "actor"
                )
        }
)
public class ProvenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_provenance_project"
            )
    )
    private TransformationProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "traceability_node_id",
            foreignKey = @ForeignKey(
                    name = "fk_transformation_provenance_node"
            )
    )
    private TraceabilityNode traceabilityNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "traceability_link_id",
            foreignKey = @ForeignKey(
                    name = "fk_transformation_provenance_link"
            )
    )
    private TraceabilityLink traceabilityLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProvenanceAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TraceabilityOrigin origin;

    @Column(nullable = false, length = 180)
    private String actor;

    @Column(name = "actor_type", nullable = false, length = 60)
    private String actorType;

    @Column(name = "process_reference", length = 255)
    private String processReference;

    @Column(length = 4000)
    private String explanation;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    protected ProvenanceRecord() {
    }

    private ProvenanceRecord(
            TransformationProject project,
            TraceabilityNode traceabilityNode,
            TraceabilityLink traceabilityLink,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    ) {
        this.project = Objects.requireNonNull(
                project,
                "El proyecto es obligatorio"
        );

        if (traceabilityNode == null
                && traceabilityLink == null) {
            throw new IllegalArgumentException(
                    "Debe indicarse un nodo o una relación"
            );
        }

        if (traceabilityNode != null
                && traceabilityLink != null) {
            throw new IllegalArgumentException(
                    "El registro no puede referenciar un nodo " +
                    "y una relación simultáneamente"
            );
        }

        if (traceabilityNode != null) {
            traceabilityNode.ensureBelongsToProject(project);
        }

        if (traceabilityLink != null
                && !sameProject(
                project,
                traceabilityLink.getProject()
        )) {
            throw new IllegalArgumentException(
                    "La relación no pertenece al proyecto indicado"
            );
        }

        this.traceabilityNode = traceabilityNode;
        this.traceabilityLink = traceabilityLink;

        this.action = Objects.requireNonNull(
                action,
                "La acción es obligatoria"
        );

        this.origin = Objects.requireNonNull(
                origin,
                "El origen es obligatorio"
        );

        this.actor = normalizeRequired(
                actor,
                "El actor es obligatorio",
                180
        );

        this.actorType = normalizeRequired(
                actorType,
                "El tipo de actor es obligatorio",
                60
        );

        this.processReference =
                normalizeOptional(processReference, 255);

        this.explanation =
                normalizeOptional(explanation, 4000);
    }

    public static ProvenanceRecord forNode(
            TransformationProject project,
            TraceabilityNode node,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    ) {
        return new ProvenanceRecord(
                project,
                node,
                null,
                action,
                origin,
                actor,
                actorType,
                processReference,
                explanation
        );
    }

    public static ProvenanceRecord forLink(
            TransformationProject project,
            TraceabilityLink link,
            ProvenanceAction action,
            TraceabilityOrigin origin,
            String actor,
            String actorType,
            String processReference,
            String explanation
    ) {
        return new ProvenanceRecord(
                project,
                null,
                link,
                action,
                origin,
                actor,
                actorType,
                processReference,
                explanation
        );
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
        this.recordedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public TraceabilityNode getTraceabilityNode() {
        return traceabilityNode;
    }

    public TraceabilityLink getTraceabilityLink() {
        return traceabilityLink;
    }

    public ProvenanceAction getAction() {
        return action;
    }

    public TraceabilityOrigin getOrigin() {
        return origin;
    }

    public String getActor() {
        return actor;
    }

    public String getActorType() {
        return actorType;
    }

    public String getProcessReference() {
        return processReference;
    }

    public String getExplanation() {
        return explanation;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}