package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisGateResultTest {

    @Test
    void shouldExposeAutoApprovedResult() {
        StrategicSynthesisGateResult result =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.AUTO_APPROVED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        4,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                                        StrategicSynthesisGateSeverity.INFO,
                                        "Allowed"
                                )
                        )
                );

        assertThat(result.isEligible())
                .isTrue();

        assertThat(result.requiresHumanReview())
                .isFalse();

        assertThat(result.isRejected())
                .isFalse();

        assertThat(result.getVerifiedArtifactCount())
                .isEqualTo(4);
    }

    @Test
    void shouldExposeBlockingReasons() {
        StrategicSynthesisGateResult result =
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.HUMAN_REVIEW_REQUIRED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.PARTIALLY_SUPPORTED,
                        3,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.EVIDENCE_PARTIALLY_SUPPORTED,
                                        StrategicSynthesisGateSeverity.BLOCKING,
                                        "Review required"
                                )
                        )
                );

        assertThat(result.isEligible())
                .isFalse();

        assertThat(result.requiresHumanReview())
                .isTrue();

        assertThat(result.getBlockingReasons())
                .hasSize(1);
    }

    @Test
    void shouldRejectInvalidVerifiedCount() {
        assertThatThrownBy(() ->
                StrategicSynthesisGateResult.of(
                        StrategicSynthesisDecision.AUTO_APPROVED,
                        StrategicTraversalStatus.COMPLETE,
                        StrategicChainCompleteness.COMPLETE,
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        5,
                        4,
                        List.of(
                                new StrategicSynthesisGateReason(
                                        StrategicSynthesisGateReasonCode.AUTO_SYNTHESIS_ALLOWED,
                                        StrategicSynthesisGateSeverity.INFO,
                                        "Allowed"
                                )
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "superar"
                );
    }
}