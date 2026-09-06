package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisReviewTest {

    @Test
    void shouldRecordApproval() {
        StrategicSynthesis synthesis =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        Instant reviewedAt =
                Instant.parse(
                        "2026-08-10T15:00:00Z"
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        synthesis,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis representa correctamente la estrategia.",
                        reviewedAt
                );

        assertThat(review.approved())
                .isTrue();

        assertThat(review.rejected())
                .isFalse();

        assertThat(review.getPreviousStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(review.getResultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.APPROVED
                );

        assertThat(review.getReviewer())
                .isEqualTo(
                        "consultant@webempresarial.com"
                );

        assertThat(review.getReviewedAt())
                .isEqualTo(
                        reviewedAt
                );
    }

    @Test
    void shouldRecordRejection() {
        StrategicSynthesis synthesis =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        synthesis,
                        "reviewer",
                        StrategicSynthesisReviewerType.PROJECT_OWNER,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La tesis necesita una revisión estratégica adicional.",
                        Instant.parse(
                                "2026-08-10T15:00:00Z"
                        )
                );

        assertThat(review.rejected())
                .isTrue();

        assertThat(review.getResultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REJECTED
                );
    }

    @Test
    void shouldNormalizeReviewerAndReason() {
        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        synthesis(
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        "  reviewer@example.com  ",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "  Validación estratégica completada.  ",
                        Instant.parse(
                                "2026-08-10T15:00:00Z"
                        )
                );

        assertThat(review.getReviewer())
                .isEqualTo(
                        "reviewer@example.com"
                );

        assertThat(review.getReason())
                .isEqualTo(
                        "Validación estratégica completada."
                );
    }

    @Test
    void shouldRejectReviewWhenSynthesisIsNotAwaitingReview() {
        StrategicSynthesis synthesis =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        assertThatThrownBy(() ->
                StrategicSynthesisReview.record(
                        synthesis,
                        "reviewer",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved",
                        Instant.now()
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
    void shouldRejectBlankReviewer() {
        assertThatThrownBy(() ->
                StrategicSynthesisReview.record(
                        synthesis(
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        " ",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved",
                        Instant.now()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "reviewer"
                );
    }

    @Test
    void shouldRejectBlankReason() {
        assertThatThrownBy(() ->
                StrategicSynthesisReview.record(
                        synthesis(
                                StrategicSynthesisStatus.REQUIRES_REVIEW
                        ),
                        "reviewer",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        " ",
                        Instant.now()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "razón"
                );
    }

    private static StrategicSynthesis synthesis(
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
                        List.of(
                                "EVD-AUDIT-001"
                        ),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                StrategicSynthesisOrigin.DETERMINISTIC,
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