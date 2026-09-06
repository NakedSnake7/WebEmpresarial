package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DefaultStrategicSynthesisReviewPolicyTest {

    private final DefaultStrategicSynthesisReviewPolicy policy =
            new DefaultStrategicSynthesisReviewPolicy();

    @Test
    void humanConsultantShouldApproveAiAssistedSynthesis() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.AI_ASSISTED,
                                StrategicSynthesisConfidence.MEDIUM,
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result.isAuthorized())
                .isTrue();

        assertThat(result.requiresHumanReview())
                .isTrue();
    }

    @Test
    void projectOwnerShouldApproveAiAssistedSynthesis() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.AI_ASSISTED,
                                StrategicSynthesisConfidence.MEDIUM,
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        StrategicSynthesisReviewerType.PROJECT_OWNER,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result.isAuthorized())
                .isTrue();
    }

    @Test
    void systemShouldNotApproveAiAssistedSynthesis() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.AI_ASSISTED,
                                StrategicSynthesisConfidence.HIGH,
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result.getAuthorization())
                .isEqualTo(
                        StrategicSynthesisReviewAuthorization.HUMAN_REVIEW_REQUIRED
                );

        assertThat(result.requiresHumanReview())
                .isTrue();

        assertThat(result.getReasons())
                .extracting(
                        StrategicSynthesisReviewPolicyReason::code
                )
                .contains(
                        StrategicSynthesisReviewPolicyReasonCode.SYSTEM_CANNOT_APPROVE_AI_SYNTHESIS
                );
    }

    @Test
    void systemShouldNotRejectAiAssistedSynthesis() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.AI_ASSISTED,
                                StrategicSynthesisConfidence.HIGH,
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.REJECT
                );

        assertThat(result.getAuthorization())
                .isEqualTo(
                        StrategicSynthesisReviewAuthorization.HUMAN_REVIEW_REQUIRED
                );

        assertThat(result.getReasons())
                .extracting(
                        StrategicSynthesisReviewPolicyReason::code
                )
                .contains(
                        StrategicSynthesisReviewPolicyReasonCode.SYSTEM_CANNOT_REJECT_AI_SYNTHESIS
                );
    }

    @Test
    void systemShouldReviewHighConfidenceDeterministicSynthesis() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.DETERMINISTIC,
                                StrategicSynthesisConfidence.HIGH,
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result.isAuthorized())
                .isTrue();

        assertThat(result.getRequirement())
                .isEqualTo(
                        StrategicSynthesisReviewRequirement.NONE
                );
    }

    @Test
    void lowConfidenceDeterministicSynthesisShouldRequireHumanReview() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.DETERMINISTIC,
                                StrategicSynthesisConfidence.LOW,
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result.requiresHumanReview())
                .isTrue();

        assertThat(result.getReasons())
                .extracting(
                        StrategicSynthesisReviewPolicyReason::code
                )
                .contains(
                        StrategicSynthesisReviewPolicyReasonCode.LOW_CONFIDENCE_REQUIRES_HUMAN
                );
    }

    @Test
    void shouldRejectReviewWhenSynthesisIsNotReviewable() {
        StrategicSynthesisReviewPolicyResult result =
                policy.evaluate(
                        synthesis(
                                StrategicSynthesisOrigin.DETERMINISTIC,
                                StrategicSynthesisConfidence.HIGH,
                                StrategicSynthesisStatus.READY
                        ),
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(result.isDenied())
                .isTrue();

        assertThat(result.getReasons())
                .extracting(
                        StrategicSynthesisReviewPolicyReason::code
                )
                .contains(
                        StrategicSynthesisReviewPolicyReasonCode.SYNTHESIS_NOT_REVIEWABLE
                );
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisOrigin origin,
            StrategicSynthesisConfidence confidence,
            StrategicSynthesisStatus status
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                "Strategic thesis",
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of("EVD-AUDIT-001"),
                        4
                ),
                confidence,
                origin,
                status,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}