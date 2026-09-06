package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import java.util.List;
import java.util.Objects;

public final class StrategicSynthesisReviewPolicyResult {

    private final StrategicSynthesisReviewAuthorization authorization;

    private final StrategicSynthesisReviewRequirement requirement;

    private final List<StrategicSynthesisReviewPolicyReason> reasons;

    private StrategicSynthesisReviewPolicyResult(
            StrategicSynthesisReviewAuthorization authorization,
            StrategicSynthesisReviewRequirement requirement,
            List<StrategicSynthesisReviewPolicyReason> reasons
    ) {
        this.authorization =
                Objects.requireNonNull(
                        authorization,
                        "La autorización es obligatoria"
                );

        this.requirement =
                Objects.requireNonNull(
                        requirement,
                        "El requerimiento es obligatorio"
                );

        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException(
                    "El resultado de policy debe contener al menos una razón"
            );
        }

        this.reasons =
                List.copyOf(reasons);
    }

    public static StrategicSynthesisReviewPolicyResult of(
            StrategicSynthesisReviewAuthorization authorization,
            StrategicSynthesisReviewRequirement requirement,
            List<StrategicSynthesisReviewPolicyReason> reasons
    ) {
        return new StrategicSynthesisReviewPolicyResult(
                authorization,
                requirement,
                reasons
        );
    }

    public boolean isAuthorized() {
        return authorization
                == StrategicSynthesisReviewAuthorization.AUTHORIZED;
    }

    public boolean requiresHumanReview() {
        return authorization
                == StrategicSynthesisReviewAuthorization.HUMAN_REVIEW_REQUIRED
                || requirement
                == StrategicSynthesisReviewRequirement.HUMAN_REVIEW;
    }

    public boolean isDenied() {
        return authorization
                == StrategicSynthesisReviewAuthorization.NOT_AUTHORIZED;
    }

    public StrategicSynthesisReviewAuthorization getAuthorization() {
        return authorization;
    }

    public StrategicSynthesisReviewRequirement getRequirement() {
        return requirement;
    }

    public List<StrategicSynthesisReviewPolicyReason> getReasons() {
        return reasons;
    }
}