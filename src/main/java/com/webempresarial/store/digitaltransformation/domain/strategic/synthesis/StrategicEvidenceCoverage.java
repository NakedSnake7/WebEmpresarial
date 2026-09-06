package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicEvidenceCoverage {

    private final StrategicChain chain;

    private final List<StrategicArtifactEvidenceSupport> supports;

    private final StrategicEvidenceCoverageStatus status;

    private final int supportedArtifacts;
    private final int directArtifacts;
    private final int weakArtifacts;
    private final int unsupportedArtifacts;

    private StrategicEvidenceCoverage(
            StrategicChain chain,
            List<StrategicArtifactEvidenceSupport> supports
    ) {
        this.chain =
                Objects.requireNonNull(
                        chain,
                        "La cadena estratégica es obligatoria"
                );

        if (supports == null) {
            throw new IllegalArgumentException(
                    "Los soportes estratégicos son obligatorios"
            );
        }

        this.supports =
                List.copyOf(supports);

        validateSupports();

        this.supportedArtifacts =
                (int) this.supports.stream()
                        .filter(
                                StrategicArtifactEvidenceSupport::hasEvidence
                        )
                        .count();

        this.directArtifacts =
                (int) this.supports.stream()
                        .filter(
                                StrategicArtifactEvidenceSupport::isDirect
                        )
                        .count();

        this.weakArtifacts =
                (int) this.supports.stream()
                        .filter(
                                StrategicArtifactEvidenceSupport::isWeak
                        )
                        .count();

        this.unsupportedArtifacts =
                (int) this.supports.stream()
                        .filter(support ->
                                support.getCoverageLevel()
                                        == EvidenceCoverageLevel.NONE
                        )
                        .count();

        this.status =
                determineStatus();
    }

    public static StrategicEvidenceCoverage of(
            StrategicChain chain,
            List<StrategicArtifactEvidenceSupport> supports
    ) {
        return new StrategicEvidenceCoverage(
                chain,
                supports
        );
    }

    private void validateSupports() {
        int expected =
                existingArtifactCount();

        if (supports.size() != expected) {
            throw new IllegalArgumentException(
                    "La cantidad de soportes no coincide con los " +
                    "artefactos presentes en la cadena"
            );
        }

        ensureSupportBelongsToChain();
    }

    private int existingArtifactCount() {
        int result = 0;

        if (chain.getFinding() != null) {
            result++;
        }

        if (chain.getBusinessProblem() != null) {
            result++;
        }

        if (chain.getBusinessObjective() != null) {
            result++;
        }

        if (chain.getStrategicOpportunity() != null) {
            result++;
        }

        return result;
    }

    private void ensureSupportBelongsToChain() {
        for (StrategicArtifactEvidenceSupport support
                : supports) {

            boolean belongs =
                    support.getArtifact() == chain.getFinding()
                    || support.getArtifact()
                    == chain.getBusinessProblem()
                    || support.getArtifact()
                    == chain.getBusinessObjective()
                    || support.getArtifact()
                    == chain.getStrategicOpportunity();

            if (!belongs) {
                throw new IllegalArgumentException(
                        "El soporte contiene un artefacto que no pertenece a la cadena"
                );
            }
        }
    }

    private StrategicEvidenceCoverageStatus
    determineStatus() {

        int total =
                supports.size();

        if (total == 0
                || supportedArtifacts == 0) {
            return StrategicEvidenceCoverageStatus.UNSUPPORTED;
        }

        if (unsupportedArtifacts == 0
                && weakArtifacts == 0) {
            return StrategicEvidenceCoverageStatus.FULLY_SUPPORTED;
        }

        if (unsupportedArtifacts == 0
                && weakArtifacts == 1) {
            return StrategicEvidenceCoverageStatus.MOSTLY_SUPPORTED;
        }

        int strongOrBetter =
                supportedArtifacts - weakArtifacts;

        if (strongOrBetter >=
                Math.ceil(total * 0.50)) {
            return StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED;
        }

        return StrategicEvidenceCoverageStatus.WEAKLY_SUPPORTED;
    }

    public int coveragePercentage() {
        if (supports.isEmpty()) {
            return 0;
        }

        return (int) Math.round(
                (
                        supportedArtifacts
                        * 100.0
                )
                / supports.size()
        );
    }

    public boolean isFullySupported() {
        return status
                == StrategicEvidenceCoverageStatus.FULLY_SUPPORTED;
    }

    public boolean canProceedToSynthesis() {
        return chain.isComplete()
                && (
                        status
                        == StrategicEvidenceCoverageStatus.FULLY_SUPPORTED
                        || status
                        == StrategicEvidenceCoverageStatus.MOSTLY_SUPPORTED
                );
    }

    public StrategicChain getChain() {
        return chain;
    }

    public List<StrategicArtifactEvidenceSupport> getSupports() {
        return supports;
    }

    public StrategicEvidenceCoverageStatus getStatus() {
        return status;
    }

    public int getSupportedArtifacts() {
        return supportedArtifacts;
    }

    public int getDirectArtifacts() {
        return directArtifacts;
    }

    public int getWeakArtifacts() {
        return weakArtifacts;
    }

    public int getUnsupportedArtifacts() {
        return unsupportedArtifacts;
    }
}