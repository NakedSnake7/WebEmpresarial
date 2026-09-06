package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicInterpretationRequest {

    private final StrategicInterpretationMode mode;

    private final String finding;
    private final String businessProblem;
    private final String businessObjective;
    private final String strategicOpportunity;

    private final String deterministicThesis;

    private final StrategicSynthesisEvidenceSummary evidenceSummary;

    private final List<String> sourceArtifactCodes;

    private final List<StrategicInterpretationConstraint> constraints;

    private StrategicInterpretationRequest(
            StrategicInterpretationMode mode,
            String finding,
            String businessProblem,
            String businessObjective,
            String strategicOpportunity,
            String deterministicThesis,
            StrategicSynthesisEvidenceSummary evidenceSummary,
            List<String> sourceArtifactCodes,
            List<StrategicInterpretationConstraint> constraints
    ) {
        this.mode =
                Objects.requireNonNull(
                        mode,
                        "El modo de interpretación es obligatorio"
                );

        this.finding =
                requireText(
                        finding,
                        "El finding es obligatorio"
                );

        this.businessProblem =
                requireText(
                        businessProblem,
                        "El problema de negocio es obligatorio"
                );

        this.businessObjective =
                requireText(
                        businessObjective,
                        "El objetivo de negocio es obligatorio"
                );

        this.strategicOpportunity =
                requireText(
                        strategicOpportunity,
                        "La oportunidad estratégica es obligatoria"
                );

        this.deterministicThesis =
                requireText(
                        deterministicThesis,
                        "La tesis determinista es obligatoria"
                );

        this.evidenceSummary =
                Objects.requireNonNull(
                        evidenceSummary,
                        "El resumen de evidencia es obligatorio"
                );

        if (sourceArtifactCodes == null
                || sourceArtifactCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Los artefactos fuente son obligatorios"
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
                    "Los artefactos fuente deben contener códigos válidos"
            );
        }

        if (constraints == null
                || constraints.isEmpty()) {
            throw new IllegalArgumentException(
                    "La interpretación debe contener guardrails"
            );
        }

        this.constraints =
                List.copyOf(
                        constraints
                );
    }

    public static StrategicInterpretationRequest from(
            StrategicSynthesis deterministicSynthesis,
            StrategicInterpretationMode mode
    ) {
        Objects.requireNonNull(
                deterministicSynthesis,
                "La síntesis determinista es obligatoria"
        );

        if (deterministicSynthesis.getOrigin()
                != StrategicSynthesisOrigin.DETERMINISTIC) {
            throw new IllegalArgumentException(
                    "El contexto base debe provenir de una síntesis DETERMINISTIC"
            );
        }

        if (!deterministicSynthesis.isReady()
                && deterministicSynthesis.getStatus()
                != StrategicSynthesisStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "La síntesis base debe estar READY o APPROVED"
            );
        }

        return new StrategicInterpretationRequest(
                mode,
                deterministicSynthesis.getFindingStatement(),
                deterministicSynthesis.getBusinessProblemStatement(),
                deterministicSynthesis.getBusinessObjectiveStatement(),
                deterministicSynthesis.getStrategicOpportunityStatement(),
                deterministicSynthesis.getStrategicThesis(),
                deterministicSynthesis.getEvidenceSummary(),
                deterministicSynthesis.getSourceArtifactCodes(),
                defaultConstraints()
        );
    }

    private static List<StrategicInterpretationConstraint>
    defaultConstraints() {
        return List.of(
                StrategicInterpretationConstraint.PRESERVE_FACTUAL_MEANING,
                StrategicInterpretationConstraint.PRESERVE_BUSINESS_PROBLEM,
                StrategicInterpretationConstraint.PRESERVE_BUSINESS_OBJECTIVE,
                StrategicInterpretationConstraint.PRESERVE_STRATEGIC_OPPORTUNITY,
                StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_FACTS,
                StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_OBJECTIVES,
                StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_OPPORTUNITIES,
                StrategicInterpretationConstraint.REQUIRE_SOURCE_ALIGNMENT
        );
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    public StrategicInterpretationMode getMode() {
        return mode;
    }

    public String getFinding() {
        return finding;
    }

    public String getBusinessProblem() {
        return businessProblem;
    }

    public String getBusinessObjective() {
        return businessObjective;
    }

    public String getStrategicOpportunity() {
        return strategicOpportunity;
    }

    public String getDeterministicThesis() {
        return deterministicThesis;
    }

    public StrategicSynthesisEvidenceSummary getEvidenceSummary() {
        return evidenceSummary;
    }

    public List<String> getSourceArtifactCodes() {
        return sourceArtifactCodes;
    }

    public List<StrategicInterpretationConstraint> getConstraints() {
        return constraints;
    }
}