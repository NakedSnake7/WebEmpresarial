package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DefaultStrategicSynthesisGate
        implements StrategicSynthesisGate {

    @Override
    public StrategicSynthesisGateResult evaluate(
            StrategicTraversalResult traversal,
            StrategicEvidenceCoverage evidenceCoverage
    ) {
        Objects.requireNonNull(
                traversal,
                "StrategicTraversalResult es obligatorio"
        );

        Objects.requireNonNull(
                evidenceCoverage,
                "StrategicEvidenceCoverage es obligatorio"
        );

        StrategicChain chain =
                evidenceCoverage.getChain();

        ensureSameChain(
                traversal,
                chain
        );

        List<StrategicArtifact> artifacts =
                existingArtifacts(chain);

        int totalArtifactCount =
                artifacts.size();

        int verifiedArtifactCount =
                (int) artifacts.stream()
                        .filter(this::isVerified)
                        .count();

        List<StrategicSynthesisGateReason> reasons =
                new ArrayList<>();

        /*
         * 1. Errores estructurales duros.
         */
        if (traversal.getStatus()
                == StrategicTraversalStatus.INVALID) {

            reasons.add(
                    blocking(
                            StrategicSynthesisGateReasonCode.STRATEGIC_CHAIN_INVALID,
                            "La estructura estratégica es inválida"
                    )
            );

            return result(
                    StrategicSynthesisDecision.REJECTED,
                    traversal,
                    evidenceCoverage,
                    verifiedArtifactCount,
                    totalArtifactCount,
                    reasons
            );
        }

        if (traversal.getStatus()
                == StrategicTraversalStatus.AMBIGUOUS) {

            reasons.add(
                    blocking(
                            StrategicSynthesisGateReasonCode.STRATEGIC_CHAIN_AMBIGUOUS,
                            "La cadena estratégica contiene rutas ambiguas"
                    )
            );

            return result(
                    StrategicSynthesisDecision.REJECTED,
                    traversal,
                    evidenceCoverage,
                    verifiedArtifactCount,
                    totalArtifactCount,
                    reasons
            );
        }

        /*
         * 2. Cadena incompleta.
         *
         * No la rechazamos definitivamente porque puede
         * corregirse mediante revisión estratégica.
         */
        if (!chain.isComplete()
                || traversal.getStatus()
                == StrategicTraversalStatus.INCOMPLETE) {

            reasons.add(
                    blocking(
                            StrategicSynthesisGateReasonCode.STRATEGIC_CHAIN_INCOMPLETE,
                            "La cadena estratégica está incompleta"
                    )
            );

            return result(
                    StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED,
                    traversal,
                    evidenceCoverage,
                    verifiedArtifactCount,
                    totalArtifactCount,
                    reasons
            );
        }

        reasons.add(
                info(
                        StrategicSynthesisGateReasonCode.STRATEGIC_CHAIN_COMPLETE,
                        "La cadena estratégica está estructuralmente completa"
                )
        );

        /*
         * 3. Lifecycle de artefactos.
         */
        boolean requiresArtifactReview =
                false;

        for (StrategicArtifact artifact : artifacts) {

            if (!isVerified(artifact)) {

                requiresArtifactReview =
                        true;

                reasons.add(
                        warning(
                                StrategicSynthesisGateReasonCode.ARTIFACT_NOT_VERIFIED,
                                "El artefacto " +
                                artifact.getArtifactCode() +
                                " todavía no está verificado"
                        )
                );
            }
        }

        /*
         * 4. Evidencia.
         */
        switch (evidenceCoverage.getStatus()) {

            case FULLY_SUPPORTED ->
                    reasons.add(
                            info(
                                    StrategicSynthesisGateReasonCode.EVIDENCE_FULLY_SUPPORTED,
                                    "La cadena está completamente respaldada por evidencia trazable"
                            )
                    );

            case MOSTLY_SUPPORTED ->
                    reasons.add(
                            warning(
                                    StrategicSynthesisGateReasonCode.EVIDENCE_MOSTLY_SUPPORTED,
                                    "La cadena está suficientemente respaldada, aunque contiene soporte débil"
                            )
                    );

            case PARTIALLY_SUPPORTED -> {
                reasons.add(
                        blocking(
                                StrategicSynthesisGateReasonCode.EVIDENCE_PARTIALLY_SUPPORTED,
                                "La cobertura documental es parcial y requiere revisión humana"
                        )
                );

                return result(
                        StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED,
                        traversal,
                        evidenceCoverage,
                        verifiedArtifactCount,
                        totalArtifactCount,
                        reasons
                );
            }

            case WEAKLY_SUPPORTED -> {
                reasons.add(
                        blocking(
                                StrategicSynthesisGateReasonCode.EVIDENCE_WEAKLY_SUPPORTED,
                                "El respaldo documental es demasiado débil para síntesis automática"
                        )
                );

                return result(
                        StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED,
                        traversal,
                        evidenceCoverage,
                        verifiedArtifactCount,
                        totalArtifactCount,
                        reasons
                );
            }

            case UNSUPPORTED -> {
                reasons.add(
                        blocking(
                                StrategicSynthesisGateReasonCode.EVIDENCE_UNSUPPORTED,
                                "La cadena estratégica no posee respaldo documental trazable"
                        )
                );

                return result(
                        StrategicSynthesisDecision.REJECTED,
                        traversal,
                        evidenceCoverage,
                        verifiedArtifactCount,
                        totalArtifactCount,
                        reasons
                );
            }
        }

        /*
         * 5. Si existe cualquier artefacto sin verificar,
         * no permitimos síntesis automática.
         */
        if (requiresArtifactReview) {

            reasons.add(
                    blocking(
                            StrategicSynthesisGateReasonCode.ARTIFACT_REQUIRES_REVIEW,
                            "Uno o más artefactos estratégicos requieren validación humana"
                    )
            );

            return result(
                    StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED,
                    traversal,
                    evidenceCoverage,
                    verifiedArtifactCount,
                    totalArtifactCount,
                    reasons
            );
        }

        /*
         * 6. Todo aprobado.
         */
        reasons.add(
                info(
                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                        "La cadena cumple los requisitos para síntesis estratégica automática"
                )
        );

        return result(
                StrategicSynthesisDecision.AUTO_APPROVED,
                traversal,
                evidenceCoverage,
                verifiedArtifactCount,
                totalArtifactCount,
                reasons
        );
    }

    private StrategicSynthesisGateResult result(
            StrategicSynthesisDecision decision,
            StrategicTraversalResult traversal,
            StrategicEvidenceCoverage coverage,
            int verified,
            int total,
            List<StrategicSynthesisGateReason> reasons
    ) {
        return StrategicSynthesisGateResult.of(
                decision,
                traversal.getStatus(),
                coverage.getChain().getCompleteness(),
                coverage.getStatus(),
                verified,
                total,
                reasons
        );
    }

    private List<StrategicArtifact> existingArtifacts(
            StrategicChain chain
    ) {
        List<StrategicArtifact> artifacts =
                new ArrayList<>();

        addIfPresent(
                artifacts,
                chain.getFinding()
        );

        addIfPresent(
                artifacts,
                chain.getBusinessProblem()
        );

        addIfPresent(
                artifacts,
                chain.getBusinessObjective()
        );

        addIfPresent(
                artifacts,
                chain.getStrategicOpportunity()
        );

        return List.copyOf(artifacts);
    }

    private void addIfPresent(
            List<StrategicArtifact> artifacts,
            StrategicArtifact artifact
    ) {
        if (artifact != null) {
            artifacts.add(artifact);
        }
    }

    private boolean isVerified(
            StrategicArtifact artifact
    ) {
        Objects.requireNonNull(
                artifact,
                "El artefacto estratégico es obligatorio"
        );

        return artifact.getStatus()
                == StrategicArtifactStatus.VERIFIED;
    }
    private static StrategicSynthesisGateReason info(
            StrategicSynthesisGateReasonCode code,
            String message
    ) {
        return new StrategicSynthesisGateReason(
                code,
                StrategicSynthesisGateSeverity.INFO,
                message
        );
    }

    private static StrategicSynthesisGateReason warning(
            StrategicSynthesisGateReasonCode code,
            String message
    ) {
        return new StrategicSynthesisGateReason(
                code,
                StrategicSynthesisGateSeverity.WARNING,
                message
        );
    }

    private static StrategicSynthesisGateReason blocking(
            StrategicSynthesisGateReasonCode code,
            String message
    ) {
        return new StrategicSynthesisGateReason(
                code,
                StrategicSynthesisGateSeverity.BLOCKING,
                message
        );
    }
    private void ensureSameChain(
            StrategicTraversalResult traversal,
            StrategicChain chain
    ) {
        if (traversal.getFinding()
                != chain.getFinding()
                || traversal.getBusinessProblem()
                != chain.getBusinessProblem()
                || traversal.getBusinessObjective()
                != chain.getBusinessObjective()
                || traversal.getStrategicOpportunity()
                != chain.getStrategicOpportunity()) {

            throw new IllegalArgumentException(
                    "Traversal y EvidenceCoverage no pertenecen " +
                    "a la misma cadena estratégica"
            );
        }
    }
}