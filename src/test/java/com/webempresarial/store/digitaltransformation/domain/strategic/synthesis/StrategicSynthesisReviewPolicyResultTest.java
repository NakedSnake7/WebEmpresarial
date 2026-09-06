package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisReviewPolicyResultTest {

    @Test
    void shouldExposeAuthorizedResult() {
        StrategicSynthesisReviewPolicyResult result =
                StrategicSynthesisReviewPolicyResult.of(
                        StrategicSynthesisReviewAuthorization.AUTHORIZED,
                        StrategicSynthesisReviewRequirement.NONE,
                        List.of(
                                new StrategicSynthesisReviewPolicyReason(
                                        StrategicSynthesisReviewPolicyReasonCode.REVIEW_AUTHORIZED,
                                        "Authorized"
                                )
                        )
                );

        assertThat(result.isAuthorized())
                .isTrue();

        assertThat(result.isDenied())
                .isFalse();
    }

    @Test
    void shouldExposeHumanReviewRequirement() {
        StrategicSynthesisReviewPolicyResult result =
                StrategicSynthesisReviewPolicyResult.of(
                        StrategicSynthesisReviewAuthorization.HUMAN_REVIEW_REQUIRED,
                        StrategicSynthesisReviewRequirement.HUMAN_REVIEW,
                        List.of(
                                new StrategicSynthesisReviewPolicyReason(
                                        StrategicSynthesisReviewPolicyReasonCode.AI_SYNTHESIS_REQUIRES_HUMAN,
                                        "Human required"
                                )
                        )
                );

        assertThat(result.requiresHumanReview())
                .isTrue();

        assertThat(result.isAuthorized())
                .isFalse();
    }

    @Test
    void shouldRejectEmptyReasons() {
        assertThatThrownBy(() ->
                StrategicSynthesisReviewPolicyResult.of(
                        StrategicSynthesisReviewAuthorization.AUTHORIZED,
                        StrategicSynthesisReviewRequirement.NONE,
                        List.of()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "al menos una razón"
                );
    }
}