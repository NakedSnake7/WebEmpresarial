package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_strategic_syntheses",
        indexes = {
                @Index(
                        name = "idx_strategic_synthesis_project",
                        columnList = "project_id,created_at"
                ),
                @Index(
                        name = "idx_strategic_synthesis_project_origin",
                        columnList = "project_id,origin,created_at"
                ),
                @Index(
                        name = "idx_strategic_synthesis_project_status",
                        columnList = "project_id,status"
                )
        }
)
public class StrategicSynthesisRecord {

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
                    name = "fk_strategic_synthesis_project"
            )
    )
    private TransformationProject project;

    @Column(
            name = "finding_statement",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String findingStatement;

    @Column(
            name = "business_problem_statement",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String businessProblemStatement;

    @Column(
            name = "business_objective_statement",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String businessObjectiveStatement;

    @Column(
            name = "strategic_opportunity_statement",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String strategicOpportunityStatement;

    @Column(
            name = "strategic_thesis",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String strategicThesis;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "coverage_status",
            nullable = false,
            length = 40
    )
    private StrategicEvidenceCoverageStatus coverageStatus;

    @Column(
            name = "coverage_percentage",
            nullable = false
    )
    private int coveragePercentage;

    @Column(
            name = "maximum_trace_depth",
            nullable = false
    )
    private int maximumTraceDepth;

    @ElementCollection
    @CollectionTable(
            name = "transformation_strategic_synthesis_evidence_codes",
            joinColumns = @JoinColumn(
                    name = "synthesis_id",
                    foreignKey = @ForeignKey(
                            name = "fk_strategic_synthesis_evidence"
                    )
            )
    )
    @OrderColumn(
            name = "position"
    )
    @Column(
            name = "evidence_code",
            nullable = false,
            length = 180
    )
    private List<String> evidenceCodes =
            new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StrategicSynthesisConfidence confidence;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StrategicSynthesisOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StrategicSynthesisStatus status;

    @ElementCollection
    @CollectionTable(
            name = "transformation_strategic_synthesis_artifact_codes",
            joinColumns = @JoinColumn(
                    name = "synthesis_id",
                    foreignKey = @ForeignKey(
                            name = "fk_strategic_synthesis_artifacts"
                    )
            )
    )
    @OrderColumn(
            name = "position"
    )
    @Column(
            name = "artifact_code",
            nullable = false,
            length = 180
    )
    private List<String> sourceArtifactCodes =
            new ArrayList<>();

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected StrategicSynthesisRecord() {
    }

    private StrategicSynthesisRecord(
            StrategicSynthesis synthesis
    ) {
        Objects.requireNonNull(
                synthesis,
                "La síntesis es obligatoria"
        );

        this.project =
                synthesis.getProject();

        this.findingStatement =
                synthesis.getFindingStatement();

        this.businessProblemStatement =
                synthesis.getBusinessProblemStatement();

        this.businessObjectiveStatement =
                synthesis.getBusinessObjectiveStatement();

        this.strategicOpportunityStatement =
                synthesis.getStrategicOpportunityStatement();

        this.strategicThesis =
                synthesis.getStrategicThesis();

        this.coverageStatus =
                synthesis.getEvidenceSummary()
                        .getCoverageStatus();

        this.coveragePercentage =
                synthesis.getEvidenceSummary()
                        .getCoveragePercentage();

        this.maximumTraceDepth =
                synthesis.getEvidenceSummary()
                        .getMaximumTraceDepth();

        this.evidenceCodes =
                new ArrayList<>(
                        synthesis.getEvidenceSummary()
                                .getEvidenceCodes()
                );

        this.confidence =
                synthesis.getConfidence();

        this.origin =
                synthesis.getOrigin();

        this.status =
                synthesis.getStatus();

        this.sourceArtifactCodes =
                new ArrayList<>(
                        synthesis.getSourceArtifactCodes()
                );
    }

    public static StrategicSynthesisRecord from(
            StrategicSynthesis synthesis
    ) {
        return new StrategicSynthesisRecord(
                synthesis
        );
    }

    public StrategicSynthesis toDomain() {

        StrategicSynthesisEvidenceSummary evidenceSummary =
                StrategicSynthesisEvidenceSummary.of(
                        coverageStatus,
                        coveragePercentage,
                        List.copyOf(
                                evidenceCodes
                        ),
                        maximumTraceDepth
                );

        return StrategicSynthesis.create(
                project,
                findingStatement,
                businessProblemStatement,
                businessObjectiveStatement,
                strategicOpportunityStatement,
                strategicThesis,
                evidenceSummary,
                confidence,
                origin,
                status,
                List.copyOf(
                        sourceArtifactCodes
                )
        );
    }
    public StoredStrategicSynthesis toStoredSynthesis() {
        if (id == null) {
            throw new IllegalStateException(
                    "El StrategicSynthesisRecord todavía no está persistido"
            );
        }

        if (createdAt == null) {
            throw new IllegalStateException(
                    "El StrategicSynthesisRecord no tiene createdAt"
            );
        }

        return new StoredStrategicSynthesis(
                id,
                toDomain(),
                createdAt
        );
    }
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt =
                    Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public StrategicSynthesisOrigin getOrigin() {
        return origin;
    }

    public StrategicSynthesisStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}