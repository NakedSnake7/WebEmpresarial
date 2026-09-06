package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_strategic_relationships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_strategic_relationship",
                        columnNames = {
                                "project_id",
                                "source_artifact_id",
                                "target_artifact_id",
                                "relationship_type"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_strategic_relationship_project",
                        columnList = "project_id"
                ),
                @Index(
                        name = "idx_strategic_relationship_source",
                        columnList = "source_artifact_id"
                ),
                @Index(
                        name = "idx_strategic_relationship_target",
                        columnList = "target_artifact_id"
                )
        }
)
public class StrategicRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_strategic_relationship_project"
            )
    )
    private TransformationProject project;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "source_artifact_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_strategic_relationship_source"
            )
    )
    private StrategicArtifact sourceArtifact;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "target_artifact_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_strategic_relationship_target"
            )
    )
    private StrategicArtifact targetArtifact;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "relationship_type",
            nullable = false,
            length = 40
    )
    private StrategicRelationshipType relationshipType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private StrategicRelationshipStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "origin",
            nullable = false,
            length = 30
    )
    private StrategicRelationshipOrigin origin;

    @Column(
            name = "rationale",
            length = 2000
    )
    private String rationale;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected StrategicRelationship() {
    }

    private StrategicRelationship(
            TransformationProject project,
            StrategicArtifact sourceArtifact,
            StrategicArtifact targetArtifact,
            StrategicRelationshipType relationshipType,
            StrategicRelationshipOrigin origin,
            String rationale
    ) {
        this.project =
                Objects.requireNonNull(
                        project,
                        "El proyecto es obligatorio"
                );

        this.sourceArtifact =
                Objects.requireNonNull(
                        sourceArtifact,
                        "El artefacto origen es obligatorio"
                );

        this.targetArtifact =
                Objects.requireNonNull(
                        targetArtifact,
                        "El artefacto destino es obligatorio"
                );

        this.relationshipType =
                Objects.requireNonNull(
                        relationshipType,
                        "El tipo de relación es obligatorio"
                );

        this.origin =
                Objects.requireNonNull(
                        origin,
                        "El origen de la relación es obligatorio"
                );

        ensureArtifactsBelongToProject();

        ensureDifferentArtifacts();

        StrategicRelationshipCompatibility
                .ensureSupported(
                        sourceArtifact.getArtifactType(),
                        targetArtifact.getArtifactType(),
                        relationshipType
                );

        this.rationale =
                normalizeNullable(rationale);

        this.status =
                StrategicRelationshipStatus.ACTIVE;

        this.createdAt =
                Instant.now();
    }

    public static StrategicRelationship create(
            TransformationProject project,
            StrategicArtifact sourceArtifact,
            StrategicArtifact targetArtifact,
            StrategicRelationshipType relationshipType,
            StrategicRelationshipOrigin origin,
            String rationale
    ) {
        return new StrategicRelationship(
                project,
                sourceArtifact,
                targetArtifact,
                relationshipType,
                origin,
                rationale
        );
    }

    private void ensureArtifactsBelongToProject() {
        sourceArtifact.ensureBelongsToProject(
                project
        );

        targetArtifact.ensureBelongsToProject(
                project
        );
    }

    private void ensureDifferentArtifacts() {
        if (sourceArtifact == targetArtifact) {
            throw new IllegalArgumentException(
                    "Un artefacto estratégico no puede " +
                    "relacionarse consigo mismo"
            );
        }

        if (sourceArtifact.getId() != null
                && targetArtifact.getId() != null
                && sourceArtifact.getId()
                .equals(targetArtifact.getId())) {

            throw new IllegalArgumentException(
                    "Un artefacto estratégico no puede " +
                    "relacionarse consigo mismo"
            );
        }
    }

    private static String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    public void reject() {
        if (status == StrategicRelationshipStatus.REJECTED) {
            return;
        }

        status =
                StrategicRelationshipStatus.REJECTED;
    }

    public boolean isActive() {
        return status
                == StrategicRelationshipStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public StrategicArtifact getSourceArtifact() {
        return sourceArtifact;
    }

    public StrategicArtifact getTargetArtifact() {
        return targetArtifact;
    }

    public StrategicRelationshipType getRelationshipType() {
        return relationshipType;
    }

    public StrategicRelationshipStatus getStatus() {
        return status;
    }

    public StrategicRelationshipOrigin getOrigin() {
        return origin;
    }

    public String getRationale() {
        return rationale;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}