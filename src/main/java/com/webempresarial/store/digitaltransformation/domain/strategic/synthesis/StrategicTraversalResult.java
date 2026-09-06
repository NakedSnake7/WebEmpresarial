package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class StrategicTraversalResult {

    private final StrategicTraversalStatus status;

    private final StrategicArtifact finding;
    private final StrategicArtifact businessProblem;
    private final StrategicArtifact businessObjective;
    private final StrategicArtifact strategicOpportunity;

    private final List<StrategicChainGap> gaps;
    private final List<StrategicTraversalAmbiguity> ambiguities;

    private StrategicTraversalResult(
            StrategicTraversalStatus status,
            StrategicArtifact finding,
            StrategicArtifact businessProblem,
            StrategicArtifact businessObjective,
            StrategicArtifact strategicOpportunity,
            List<StrategicChainGap> gaps,
            List<StrategicTraversalAmbiguity> ambiguities
    ) {
        this.status =
                Objects.requireNonNull(
                        status,
                        "El estado del traversal es obligatorio"
                );

        this.finding = finding;
        this.businessProblem = businessProblem;
        this.businessObjective = businessObjective;
        this.strategicOpportunity = strategicOpportunity;

        this.gaps =
                gaps == null
                        ? List.of()
                        : List.copyOf(gaps);

        this.ambiguities =
                ambiguities == null
                        ? List.of()
                        : List.copyOf(ambiguities);
    }

    public static StrategicTraversalResult of(
            StrategicTraversalStatus status,
            StrategicArtifact finding,
            StrategicArtifact businessProblem,
            StrategicArtifact businessObjective,
            StrategicArtifact strategicOpportunity,
            List<StrategicChainGap> gaps,
            List<StrategicTraversalAmbiguity> ambiguities
    ) {
        return new StrategicTraversalResult(
                status,
                finding,
                businessProblem,
                businessObjective,
                strategicOpportunity,
                gaps,
                ambiguities
        );
    }

    public boolean isComplete() {
        return status == StrategicTraversalStatus.COMPLETE;
    }

    public boolean isAmbiguous() {
        return status == StrategicTraversalStatus.AMBIGUOUS;
    }

    public boolean canBuildChain() {
        return status == StrategicTraversalStatus.COMPLETE
                || status == StrategicTraversalStatus.INCOMPLETE;
    }

    public Optional<StrategicChain> toChain() {
        if (!canBuildChain()) {
            return Optional.empty();
        }

        if (finding == null) {
            return Optional.empty();
        }

        return Optional.of(
                StrategicChain.of(
                        finding.getProject(),
                        finding,
                        businessProblem,
                        businessObjective,
                        strategicOpportunity
                )
        );
    }

    public StrategicTraversalStatus getStatus() {
        return status;
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

    public List<StrategicChainGap> getGaps() {
        return gaps;
    }

    public List<StrategicTraversalAmbiguity> getAmbiguities() {
        return ambiguities;
    }
}