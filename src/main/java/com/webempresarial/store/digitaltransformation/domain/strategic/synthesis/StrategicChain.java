package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StrategicChain {

    private final TransformationProject project;

    private final StrategicArtifact finding;
    private final StrategicArtifact businessProblem;
    private final StrategicArtifact businessObjective;
    private final StrategicArtifact strategicOpportunity;

    private final StrategicChainStatus status;
    private final StrategicChainCompleteness completeness;

    private final List<StrategicChainGap> gaps;

    private StrategicChain(
            TransformationProject project,
            StrategicArtifact finding,
            StrategicArtifact businessProblem,
            StrategicArtifact businessObjective,
            StrategicArtifact strategicOpportunity
    ) {
        this.project =
                Objects.requireNonNull(
                        project,
                        "El proyecto es obligatorio"
                );

        validateArtifact(
                finding,
                StrategicArtifactType.FINDING,
                "finding"
        );

        validateArtifact(
                businessProblem,
                StrategicArtifactType.BUSINESS_PROBLEM,
                "businessProblem"
        );

        validateArtifact(
                businessObjective,
                StrategicArtifactType.BUSINESS_OBJECTIVE,
                "businessObjective"
        );

        validateArtifact(
                strategicOpportunity,
                StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                "strategicOpportunity"
        );

        ensureProject(
                finding
        );

        ensureProject(
                businessProblem
        );

        ensureProject(
                businessObjective
        );

        ensureProject(
                strategicOpportunity
        );

        this.finding = finding;
        this.businessProblem = businessProblem;
        this.businessObjective = businessObjective;
        this.strategicOpportunity = strategicOpportunity;

        this.gaps =
                List.copyOf(
                        detectGaps()
                );

        this.completeness =
                determineCompleteness();

        this.status =
                determineStatus();
    }

    public static StrategicChain of(
            TransformationProject project,
            StrategicArtifact finding,
            StrategicArtifact businessProblem,
            StrategicArtifact businessObjective,
            StrategicArtifact strategicOpportunity
    ) {
        return new StrategicChain(
                project,
                finding,
                businessProblem,
                businessObjective,
                strategicOpportunity
        );
    }

    public static StrategicChain startingWith(
            TransformationProject project,
            StrategicArtifact finding
    ) {
        return new StrategicChain(
                project,
                finding,
                null,
                null,
                null
        );
    }

    private void validateArtifact(
            StrategicArtifact artifact,
            StrategicArtifactType expectedType,
            String role
    ) {
        if (artifact == null) {
            return;
        }

        if (artifact.getArtifactType() != expectedType) {
            throw new IllegalArgumentException(
                    "El artefacto " +
                    role +
                    " debe ser de tipo " +
                    expectedType +
                    " pero es " +
                    artifact.getArtifactType()
            );
        }
    }

    private void ensureProject(
            StrategicArtifact artifact
    ) {
        if (artifact == null) {
            return;
        }

        artifact.ensureBelongsToProject(
                project
        );
    }

    private List<StrategicChainGap> detectGaps() {
        List<StrategicChainGap> result =
                new ArrayList<>();

        if (finding == null) {
            result.add(
                    new StrategicChainGap(
                            StrategicChainGapType.MISSING_FINDING,
                            "La cadena no contiene un hallazgo estratégico"
                    )
            );
        }

        if (businessProblem == null) {
            result.add(
                    new StrategicChainGap(
                            StrategicChainGapType.MISSING_BUSINESS_PROBLEM,
                            "La cadena no contiene un problema empresarial"
                    )
            );
        }

        if (businessObjective == null) {
            result.add(
                    new StrategicChainGap(
                            StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE,
                            "La cadena no contiene un objetivo empresarial"
                    )
            );
        }

        if (strategicOpportunity == null) {
            result.add(
                    new StrategicChainGap(
                            StrategicChainGapType.MISSING_STRATEGIC_OPPORTUNITY,
                            "La cadena no contiene una oportunidad estratégica"
                    )
            );
        }

        return result;
    }

    private StrategicChainCompleteness
    determineCompleteness() {

        if (finding == null
                && businessProblem == null
                && businessObjective == null
                && strategicOpportunity == null) {

            return StrategicChainCompleteness.EMPTY;
        }

        if (finding != null
                && businessProblem == null
                && businessObjective == null
                && strategicOpportunity == null) {

            return StrategicChainCompleteness.FINDING_ONLY;
        }

        if (finding != null
                && businessProblem != null
                && businessObjective == null
                && strategicOpportunity == null) {

            return StrategicChainCompleteness.FINDING_AND_PROBLEM;
        }

        if (finding != null
                && businessProblem != null
                && businessObjective != null
                && strategicOpportunity == null) {

            return StrategicChainCompleteness.THROUGH_OBJECTIVE;
        }

        if (finding != null
                && businessProblem != null
                && businessObjective != null
                && strategicOpportunity != null) {

            return StrategicChainCompleteness.COMPLETE;
        }

        return StrategicChainCompleteness.PARTIAL_NON_CANONICAL;
    }

    private StrategicChainStatus determineStatus() {
        if (completeness.isComplete()) {
            return StrategicChainStatus.COMPLETE;
        }

        return StrategicChainStatus.INCOMPLETE;
    }

    public boolean isComplete() {
        return status == StrategicChainStatus.COMPLETE;
    }

    public boolean hasGap(
            StrategicChainGapType gapType
    ) {
        Objects.requireNonNull(
                gapType,
                "El tipo de gap es obligatorio"
        );

        return gaps.stream()
                .anyMatch(
                        gap -> gap.type() == gapType
                );
    }

    public boolean canBePrioritized() {
        return isComplete()
                && allArtifactsCanDriveImplementation();
    }

    private boolean allArtifactsCanDriveImplementation() {
        return finding.canDriveImplementation()
                && businessProblem.canDriveImplementation()
                && businessObjective.canDriveImplementation()
                && strategicOpportunity.canDriveImplementation();
    }

    public int completenessPercentage() {
        return completeness.percentage();
    }

    public TransformationProject getProject() {
        return project;
    }

    public StrategicArtifact getFinding() {
        return finding;
    }

    public StrategicArtifact getBusinessProblem() {
        return businessProblem;
    }

    public StrategicArtifact getBusinessObjective() {
        return businessObjective;
    }

    public StrategicArtifact getStrategicOpportunity() {
        return strategicOpportunity;
    }

    public StrategicChainStatus getStatus() {
        return status;
    }

    public StrategicChainCompleteness getCompleteness() {
        return completeness;
    }

    public List<StrategicChainGap> getGaps() {
        return gaps;
    }
}