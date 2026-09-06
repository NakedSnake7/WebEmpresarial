package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;

import java.util.List;
import java.util.Objects;

public final class StrategicSynthesis {

    private final TransformationProject project;

    private final String findingStatement;

    private final String businessProblemStatement;

    private final String businessObjectiveStatement;

    private final String strategicOpportunityStatement;

    private final String strategicThesis;

    private final StrategicSynthesisEvidenceSummary evidenceSummary;

    private final StrategicSynthesisConfidence confidence;

    private final StrategicSynthesisOrigin origin;

    private final StrategicSynthesisStatus status;

    private final List<String> sourceArtifactCodes;

    private StrategicSynthesis(
            TransformationProject project,
            String findingStatement,
            String businessProblemStatement,
            String businessObjectiveStatement,
            String strategicOpportunityStatement,
            String strategicThesis,
            StrategicSynthesisEvidenceSummary evidenceSummary,
            StrategicSynthesisConfidence confidence,
            StrategicSynthesisOrigin origin,
            StrategicSynthesisStatus status,
            List<String> sourceArtifactCodes
    ) {
        this.project =
                Objects.requireNonNull(
                        project,
                        "El proyecto es obligatorio"
                );

        this.findingStatement =
                requireText(
                        findingStatement,
                        "El finding es obligatorio"
                );

        this.businessProblemStatement =
                requireText(
                        businessProblemStatement,
                        "El problema de negocio es obligatorio"
                );

        this.businessObjectiveStatement =
                requireText(
                        businessObjectiveStatement,
                        "El objetivo de negocio es obligatorio"
                );

        this.strategicOpportunityStatement =
                requireText(
                        strategicOpportunityStatement,
                        "La oportunidad estratégica es obligatoria"
                );

        this.strategicThesis =
                requireText(
                        strategicThesis,
                        "La tesis estratégica es obligatoria"
                );

        this.evidenceSummary =
                Objects.requireNonNull(
                        evidenceSummary,
                        "El resumen de evidencia es obligatorio"
                );

        this.confidence =
                Objects.requireNonNull(
                        confidence,
                        "La confianza de síntesis es obligatoria"
                );

        this.origin =
                Objects.requireNonNull(
                        origin,
                        "El origen de síntesis es obligatorio"
                );

        this.status =
                Objects.requireNonNull(
                        status,
                        "El estado de síntesis es obligatorio"
                );

        if (sourceArtifactCodes == null
                || sourceArtifactCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "La síntesis debe contener artefactos fuente"
            );
        }

        this.sourceArtifactCodes =
                sourceArtifactCodes.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(code -> !code.isBlank())
                        .distinct()
                        .toList();

        if (this.sourceArtifactCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "La síntesis debe contener códigos de artefactos válidos"
            );
        }
    }

    public static StrategicSynthesis create(
            TransformationProject project,
            String findingStatement,
            String businessProblemStatement,
            String businessObjectiveStatement,
            String strategicOpportunityStatement,
            String strategicThesis,
            StrategicSynthesisEvidenceSummary evidenceSummary,
            StrategicSynthesisConfidence confidence,
            StrategicSynthesisOrigin origin,
            StrategicSynthesisStatus status,
            List<String> sourceArtifactCodes
    ) {
        return new StrategicSynthesis(
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
                sourceArtifactCodes
        );
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    message
            );
        }

        return value.trim();
    }

    public boolean isReady() {
        return status
                == StrategicSynthesisStatus.READY;
    }

    public boolean requiresReview() {
        return status
                == StrategicSynthesisStatus.REQUIRES_REVIEW;
    }

    public boolean isAiAssisted() {
        return origin
                == StrategicSynthesisOrigin.AI_ASSISTED;
    }
    
    public StrategicSynthesis withStatus(
            StrategicSynthesisStatus newStatus
    ) {
        Objects.requireNonNull(
                newStatus,
                "El nuevo estado es obligatorio"
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
                newStatus,
                sourceArtifactCodes
        );
    }
    
    public boolean hasSameContentAs(
            StrategicSynthesis other
    ) {
        if (other == null) {
            return false;
        }

        return Objects.equals(
                findingStatement,
                other.findingStatement
        )
                && Objects.equals(
                        businessProblemStatement,
                        other.businessProblemStatement
                )
                && Objects.equals(
                        businessObjectiveStatement,
                        other.businessObjectiveStatement
                )
                && Objects.equals(
                        strategicOpportunityStatement,
                        other.strategicOpportunityStatement
                )
                && Objects.equals(
                        strategicThesis,
                        other.strategicThesis
                )
                && Objects.equals(
                        evidenceSummary,
                        other.evidenceSummary
                )
                && confidence == other.confidence
                && origin == other.origin
                && Objects.equals(
                        sourceArtifactCodes,
                        other.sourceArtifactCodes
                );
    }

    public TransformationProject getProject() {
        return project;
    }

    public String getFindingStatement() {
        return findingStatement;
    }

    public String getBusinessProblemStatement() {
        return businessProblemStatement;
    }

    public String getBusinessObjectiveStatement() {
        return businessObjectiveStatement;
    }

    public String getStrategicOpportunityStatement() {
        return strategicOpportunityStatement;
    }

    public String getStrategicThesis() {
        return strategicThesis;
    }

    public StrategicSynthesisEvidenceSummary getEvidenceSummary() {
        return evidenceSummary;
    }

    public StrategicSynthesisConfidence getConfidence() {
        return confidence;
    }

    public StrategicSynthesisOrigin getOrigin() {
        return origin;
    }

    public StrategicSynthesisStatus getStatus() {
        return status;
    }

    public List<String> getSourceArtifactCodes() {
        return sourceArtifactCodes;
    }
}