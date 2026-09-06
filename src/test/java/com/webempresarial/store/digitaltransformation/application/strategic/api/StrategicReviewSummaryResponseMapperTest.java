package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesisReview;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicReviewSummaryResponseMapperTest {

    @Test
    void shouldMapStoredReview() {
        StrategicSynthesis synthesis =
                synthesis();

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        synthesis,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved after strategic review.",
                        Instant.parse(
                                "2026-08-14T18:30:00Z"
                        )
                );

        StoredStrategicSynthesisReview stored =
                new StoredStrategicSynthesisReview(
                        100L,
                        41L,
                        42L,
                        review
                );

        StrategicReviewSummaryResponse response =
                StrategicReviewSummaryResponseMapper.toResponse(
                        stored
                );

        assertThat(response.id())
                .isEqualTo(100L);

        assertThat(response.reviewedSynthesisId())
                .isEqualTo(41L);

        assertThat(response.resultingSynthesisId())
                .isEqualTo(42L);

        assertThat(response.reviewer())
                .isEqualTo(
                        "consultant@webempresarial.com"
                );

        assertThat(response.reviewerType())
                .isEqualTo(
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );

        assertThat(response.decision())
                .isEqualTo(
                        StrategicSynthesisReviewDecision.APPROVE
                );

        assertThat(response.previousStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(response.resultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.APPROVED
                );
    }

    @Test
    void shouldReturnNullForNullReview() {
        assertThat(
                StrategicReviewSummaryResponseMapper.toResponse(
                        null
                )
        ).isNull();
    }

    private static StrategicSynthesis synthesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Problem",
                "Objective",
                "Opportunity",
                "Strategic thesis",
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001"
                        ),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                StrategicSynthesisOrigin.AI_ASSISTED,
                StrategicSynthesisStatus.REQUIRES_REVIEW,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}