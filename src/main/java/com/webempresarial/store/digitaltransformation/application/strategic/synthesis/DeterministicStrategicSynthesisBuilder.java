package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class DeterministicStrategicSynthesisBuilder
        implements StrategicSynthesisBuilder {

    private final StrategicThesisGenerator thesisGenerator;

    private final StrategicSynthesisEvidenceSummaryFactory
            evidenceSummaryFactory;

    private final StrategicSynthesisConfidenceEvaluator
            confidenceEvaluator;

    public DeterministicStrategicSynthesisBuilder(
            StrategicThesisGenerator thesisGenerator,
            StrategicSynthesisEvidenceSummaryFactory evidenceSummaryFactory,
            StrategicSynthesisConfidenceEvaluator confidenceEvaluator
    ) {
        this.thesisGenerator =
                Objects.requireNonNull(
                        thesisGenerator,
                        "StrategicThesisGenerator es obligatorio"
                );

        this.evidenceSummaryFactory =
                Objects.requireNonNull(
                        evidenceSummaryFactory,
                        "StrategicSynthesisEvidenceSummaryFactory es obligatorio"
                );

        this.confidenceEvaluator =
                Objects.requireNonNull(
                        confidenceEvaluator,
                        "StrategicSynthesisConfidenceEvaluator es obligatorio"
                );
    }
    private static void ensureGateMatches(
            StrategicChain chain,
            StrategicEvidenceCoverage evidenceCoverage,
            StrategicSynthesisGateResult gateResult
    ) {
        if (gateResult.getCompleteness()
                != chain.getCompleteness()) {

            throw new IllegalArgumentException(
                    "La completitud del SynthesisGateResult " +
                    "no corresponde con la cadena estratégica"
            );
        }

        if (gateResult.getEvidenceCoverageStatus()
                != evidenceCoverage.getStatus()) {

            throw new IllegalArgumentException(
                    "La cobertura de evidencia del SynthesisGateResult " +
                    "no corresponde con la cobertura evaluada"
            );
        }
    }
    @Override
    public StrategicSynthesis build(
            StrategicChain chain,
            StrategicEvidenceCoverage evidenceCoverage,
            StrategicSynthesisGateResult gateResult
    ) {
        Objects.requireNonNull(
                chain,
                "La cadena estratégica es obligatoria"
        );

        Objects.requireNonNull(
                evidenceCoverage,
                "La cobertura de evidencia es obligatoria"
        );

        Objects.requireNonNull(
                gateResult,
                "El resultado del synthesis gate es obligatorio"
        );

        /*
         * 1. Chain y EvidenceCoverage deben representar
         * exactamente la misma cadena estratégica.
         */
        ensureConsistentInput(
                chain,
                evidenceCoverage
        );

        /*
         * 2. El resultado del Gate debe corresponder
         * al estado real de esos inputs.
         */
        ensureGateMatches(
                chain,
                evidenceCoverage,
                gateResult
        );

        /*
         * 3. Solo AUTO_APPROVED puede producir
         * síntesis automática.
         */
        if (!gateResult.isEligible()) {
            throw new IllegalStateException(
                    "La cadena estratégica no está autorizada " +
                    "para síntesis automática"
            );
        }

        /*
         * Defensa adicional.
         */
        if (!chain.isComplete()) {
            throw new IllegalStateException(
                    "Solo una cadena completa puede producir síntesis"
            );
        }

        StrategicSynthesisEvidenceSummary evidenceSummary =
                evidenceSummaryFactory.create(
                        evidenceCoverage
                );

        StrategicSynthesisConfidence confidence =
                confidenceEvaluator.evaluate(
                        chain,
                        evidenceCoverage
                );

        String strategicThesis =
                thesisGenerator.generate(
                        chain
                );

        return StrategicSynthesis.create(
                chain.getProject(),
                chain.getFinding().getStatement(),
                chain.getBusinessProblem().getStatement(),
                chain.getBusinessObjective().getStatement(),
                chain.getStrategicOpportunity().getStatement(),
                strategicThesis,
                evidenceSummary,
                confidence,
                StrategicSynthesisOrigin.DETERMINISTIC,
                StrategicSynthesisStatus.READY,
                List.of(
                        chain.getFinding().getArtifactCode(),
                        chain.getBusinessProblem().getArtifactCode(),
                        chain.getBusinessObjective().getArtifactCode(),
                        chain.getStrategicOpportunity().getArtifactCode()
                )
        );
    }
    private static boolean sameProject(
            StrategicChain first,
            StrategicChain second
    ) {
        if (first.getProject() == second.getProject()) {
            return true;
        }

        if (first.getProject() == null
                || second.getProject() == null) {
            return false;
        }

        if (first.getProject().getId() == null
                || second.getProject().getId() == null) {
            return false;
        }

        return Objects.equals(
                first.getProject().getId(),
                second.getProject().getId()
        );
    }
    private static boolean sameChain(
            StrategicChain first,
            StrategicChain second
    ) {
        if (!sameProject(
                first,
                second
        )) {
            return false;
        }

        return sameArtifact(
                first.getFinding(),
                second.getFinding()
        )
                && sameArtifact(
                        first.getBusinessProblem(),
                        second.getBusinessProblem()
                )
                && sameArtifact(
                        first.getBusinessObjective(),
                        second.getBusinessObjective()
                )
                && sameArtifact(
                        first.getStrategicOpportunity(),
                        second.getStrategicOpportunity()
                );
    }
    private static boolean sameArtifact(
            StrategicArtifact first,
            StrategicArtifact second
    ) {
        if (first == null || second == null) {
            return first == second;
        }

        return Objects.equals(
                first.getArtifactCode(),
                second.getArtifactCode()
        );
    }
    private static void ensureConsistentInput(
            StrategicChain chain,
            StrategicEvidenceCoverage evidenceCoverage
    ) {
        StrategicChain coverageChain =
                Objects.requireNonNull(
                        evidenceCoverage.getChain(),
                        "EvidenceCoverage debe contener una cadena estratégica"
                );

        if (!sameChain(
                chain,
                coverageChain
        )) {
            throw new IllegalArgumentException(
                    "StrategicChain y EvidenceCoverage " +
                    "no corresponden a la misma cadena"
            );
        }
    }
    
}