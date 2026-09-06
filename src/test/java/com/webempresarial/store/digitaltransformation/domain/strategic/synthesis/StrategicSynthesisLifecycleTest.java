package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisLifecycleTest {

    @Test
    void readySynthesisShouldBeSubmittableForReview() {
        StrategicSynthesisStatus result =
                StrategicSynthesisLifecycle
                        .submitForReview(
                                StrategicSynthesisStatus.READY
                        );

        assertThat(result)
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );
    }

    @Test
    void draftSynthesisShouldBeSubmittableForReview() {
        StrategicSynthesisStatus result =
                StrategicSynthesisLifecycle
                        .submitForReview(
                                StrategicSynthesisStatus.DRAFT
                        );

        assertThat(result)
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );
    }

    @Test
    void reviewShouldApproveSynthesis() {
        StrategicSynthesisStatus result =
                StrategicSynthesisLifecycle.applyReview(
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result)
                .isEqualTo(
                        StrategicSynthesisStatus.APPROVED
                );
    }

    @Test
    void reviewShouldRejectSynthesis() {
        StrategicSynthesisStatus result =
                StrategicSynthesisLifecycle.applyReview(
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        StrategicSynthesisReviewDecision.REJECT
                );

        assertThat(result)
                .isEqualTo(
                        StrategicSynthesisStatus.REJECTED
                );
    }

    @Test
    void readySynthesisCannotBeApprovedDirectly() {
        assertThatThrownBy(() ->
                StrategicSynthesisLifecycle.applyReview(
                        StrategicSynthesisStatus.READY,
                        StrategicSynthesisReviewDecision.APPROVE
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "REQUIRES_REVIEW"
                );
    }

    @Test
    void rejectedSynthesisCannotBeApprovedDirectly() {
        assertThatThrownBy(() ->
                StrategicSynthesisLifecycle.applyReview(
                        StrategicSynthesisStatus.REJECTED,
                        StrategicSynthesisReviewDecision.APPROVE
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }

    @Test
    void approvedSynthesisCannotBeSubmittedAgainDirectly() {
        assertThatThrownBy(() ->
                StrategicSynthesisLifecycle.submitForReview(
                        StrategicSynthesisStatus.APPROVED
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }
}