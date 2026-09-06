package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicSynthesisGateResult {

    private final StrategicSynthesisDecision decision;

    private final StrategicTraversalStatus traversalStatus;

    private final StrategicChainCompleteness completeness;

    private final StrategicEvidenceCoverageStatus evidenceCoverageStatus;

    private final int verifiedArtifactCount;

    private final int totalArtifactCount;

    private final List<StrategicSynthesisGateReason> reasons;

    private StrategicSynthesisGateResult(
            StrategicSynthesisDecision decision,
            StrategicTraversalStatus traversalStatus,
            StrategicChainCompleteness completeness,
            StrategicEvidenceCoverageStatus evidenceCoverageStatus,
            int verifiedArtifactCount,
            int totalArtifactCount,
            List<StrategicSynthesisGateReason> reasons
    ) {
        this.decision =
                Objects.requireNonNull(
                        decision,
                        "La decisión es obligatoria"
                );

        this.traversalStatus =
                Objects.requireNonNull(
                        traversalStatus,
                        "El estado de traversal es obligatorio"
                );

        this.completeness =
                Objects.requireNonNull(
                        completeness,
                        "La completitud estratégica es obligatoria"
                );

        this.evidenceCoverageStatus =
                Objects.requireNonNull(
                        evidenceCoverageStatus,
                        "El estado de cobertura es obligatorio"
                );

        if (verifiedArtifactCount < 0) {
            throw new IllegalArgumentException(
                    "verifiedArtifactCount no puede ser negativo"
            );
        }

        if (totalArtifactCount < 0) {
            throw new IllegalArgumentException(
                    "totalArtifactCount no puede ser negativo"
            );
        }

        if (verifiedArtifactCount > totalArtifactCount) {
            throw new IllegalArgumentException(
                    "Los artefactos verificados no pueden superar el total"
            );
        }

        this.verifiedArtifactCount =
                verifiedArtifactCount;

        this.totalArtifactCount =
                totalArtifactCount;

        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException(
                    "El resultado del gate debe contener al menos una razón"
            );
        }

        this.reasons =
                List.copyOf(reasons);
    }

    public static StrategicSynthesisGateResult of(
            StrategicSynthesisDecision decision,
            StrategicTraversalStatus traversalStatus,
            StrategicChainCompleteness completeness,
            StrategicEvidenceCoverageStatus evidenceCoverageStatus,
            int verifiedArtifactCount,
            int totalArtifactCount,
            List<StrategicSynthesisGateReason> reasons
    ) {
        return new StrategicSynthesisGateResult(
                decision,
                traversalStatus,
                completeness,
                evidenceCoverageStatus,
                verifiedArtifactCount,
                totalArtifactCount,
                reasons
        );
    }

    public boolean isEligible() {
        return decision
                == StrategicSynthesisDecision.AUTO_APPROVED;
    }

    public boolean requiresHumanReview() {
        return decision
                == StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED;
    }

    public boolean isRejected() {
        return decision
                == StrategicSynthesisDecision.REJECTED;
    }

    public List<StrategicSynthesisGateReason> getBlockingReasons() {
        return reasons.stream()
                .filter(
                        StrategicSynthesisGateReason::isBlocking
                )
                .toList();
    }

    public List<StrategicSynthesisGateReason> getWarnings() {
        return reasons.stream()
                .filter(
                        StrategicSynthesisGateReason::isWarning
                )
                .toList();
    }

    public StrategicSynthesisDecision getDecision() {
        return decision;
    }

    public StrategicTraversalStatus getTraversalStatus() {
        return traversalStatus;
    }

    public StrategicChainCompleteness getCompleteness() {
        return completeness;
    }

    public StrategicEvidenceCoverageStatus getEvidenceCoverageStatus() {
        return evidenceCoverageStatus;
    }

    public int getVerifiedArtifactCount() {
        return verifiedArtifactCount;
    }

    public int getTotalArtifactCount() {
        return totalArtifactCount;
    }

    public List<StrategicSynthesisGateReason> getReasons() {
        return reasons;
    }
}