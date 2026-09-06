package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class DefaultStrategicSynthesisReviewPolicy
        implements StrategicSynthesisReviewPolicy {

    @Override
    public StrategicSynthesisReviewPolicyResult evaluate(
            StrategicSynthesis synthesis,
            StrategicSynthesisReviewerType reviewerType,
            StrategicSynthesisReviewDecision decision
    ) {
        Objects.requireNonNull(
                synthesis,
                "La síntesis estratégica es obligatoria"
        );

        Objects.requireNonNull(
                reviewerType,
                "El tipo de reviewer es obligatorio"
        );

        Objects.requireNonNull(
                decision,
                "La decisión de revisión es obligatoria"
        );

        /*
         * Primero respetamos el lifecycle.
         */
        if (!StrategicSynthesisLifecycle.canReview(
                synthesis.getStatus()
        )) {
            return denied(
                    StrategicSynthesisReviewPolicyReasonCode.SYNTHESIS_NOT_REVIEWABLE,
                    "La síntesis no se encuentra en estado REQUIRES_REVIEW"
            );
        }

        /*
         * Síntesis generadas o interpretadas mediante IA:
         * ningún SYSTEM puede aprobarlas o rechazarlas.
         */
        if (synthesis.getOrigin()
                == StrategicSynthesisOrigin.AI_ASSISTED) {

            if (reviewerType
                    == StrategicSynthesisReviewerType.SYSTEM) {

                StrategicSynthesisReviewPolicyReasonCode code =
                        decision
                                == StrategicSynthesisReviewDecision.APPROVE
                                ? StrategicSynthesisReviewPolicyReasonCode.SYSTEM_CANNOT_APPROVE_AI_SYNTHESIS
                                : StrategicSynthesisReviewPolicyReasonCode.SYSTEM_CANNOT_REJECT_AI_SYNTHESIS;

                return humanRequired(
                        code,
                        "Una síntesis AI_ASSISTED requiere una decisión humana"
                );
            }

            return authorizedHuman(
                    reviewerType,
                    "La síntesis AI_ASSISTED será revisada por un actor humano"
            );
        }

        /*
         * LOW confidence requiere humano incluso
         * cuando la síntesis es determinista.
         */
        if (synthesis.getConfidence()
                == StrategicSynthesisConfidence.LOW
                && reviewerType
                == StrategicSynthesisReviewerType.SYSTEM) {

            return humanRequired(
                    StrategicSynthesisReviewPolicyReasonCode.LOW_CONFIDENCE_REQUIRES_HUMAN,
                    "Una síntesis con confianza LOW requiere revisión humana"
            );
        }

        /*
         * Humanos autorizados.
         */
        if (reviewerType
                == StrategicSynthesisReviewerType.HUMAN_CONSULTANT) {

            return authorized(
                    StrategicSynthesisReviewPolicyReasonCode.HUMAN_CONSULTANT_AUTHORIZED,
                    "El consultor humano está autorizado para revisar la síntesis"
            );
        }

        if (reviewerType
                == StrategicSynthesisReviewerType.PROJECT_OWNER) {

            return authorized(
                    StrategicSynthesisReviewPolicyReasonCode.PROJECT_OWNER_AUTHORIZED,
                    "El propietario del proyecto está autorizado para revisar la síntesis"
            );
        }

        /*
         * SYSTEM solo puede intervenir sobre síntesis
         * deterministas y suficientemente confiables.
         */
        if (reviewerType
                == StrategicSynthesisReviewerType.SYSTEM) {

            return authorized(
                    StrategicSynthesisReviewPolicyReasonCode.SYSTEM_AUTHORIZED_FOR_DETERMINISTIC_SYNTHESIS,
                    "El sistema está autorizado para revisar una síntesis determinista"
            );
        }

        return denied(
                StrategicSynthesisReviewPolicyReasonCode.INVALID_REVIEWER,
                "El reviewer no está autorizado"
        );
    }

    private static StrategicSynthesisReviewPolicyResult authorizedHuman(
            StrategicSynthesisReviewerType reviewerType,
            String message
    ) {
        StrategicSynthesisReviewPolicyReasonCode code =
                reviewerType
                        == StrategicSynthesisReviewerType.PROJECT_OWNER
                        ? StrategicSynthesisReviewPolicyReasonCode.PROJECT_OWNER_AUTHORIZED
                        : StrategicSynthesisReviewPolicyReasonCode.HUMAN_CONSULTANT_AUTHORIZED;

        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.AUTHORIZED,
                StrategicSynthesisReviewRequirement.HUMAN_REVIEW,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                code,
                                message
                        )
                )
        );
    }

    private static StrategicSynthesisReviewPolicyResult authorized(
            StrategicSynthesisReviewPolicyReasonCode code,
            String message
    ) {
        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.AUTHORIZED,
                StrategicSynthesisReviewRequirement.NONE,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                code,
                                message
                        )
                )
        );
    }

    private static StrategicSynthesisReviewPolicyResult humanRequired(
            StrategicSynthesisReviewPolicyReasonCode code,
            String message
    ) {
        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.HUMAN_REVIEW_REQUIRED,
                StrategicSynthesisReviewRequirement.HUMAN_REVIEW,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                code,
                                message
                        )
                )
        );
    }

    private static StrategicSynthesisReviewPolicyResult denied(
            StrategicSynthesisReviewPolicyReasonCode code,
            String message
    ) {
        return StrategicSynthesisReviewPolicyResult.of(
                StrategicSynthesisReviewAuthorization.NOT_AUTHORIZED,
                StrategicSynthesisReviewRequirement.NONE,
                List.of(
                        new StrategicSynthesisReviewPolicyReason(
                                code,
                                message
                        )
                )
        );
    }
}