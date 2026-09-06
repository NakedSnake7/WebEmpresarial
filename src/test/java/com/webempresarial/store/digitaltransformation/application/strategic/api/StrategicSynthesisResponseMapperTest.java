package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisResponseMapperTest {

    @Test
    void shouldMapRequiresReviewSnapshot() {
        StrategicSynthesis synthesis =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StoredStrategicSynthesis stored =
                new StoredStrategicSynthesis(
                        41L,
                        synthesis,
                        Instant.parse(
                                "2026-08-14T18:00:00Z"
                        )
                );

        StrategicSynthesisResponse response =
                StrategicSynthesisResponseMapper.toResponse(
                        stored
                );

        assertThat(response.id())
                .isEqualTo(41L);

        assertThat(response.thesis())
                .isEqualTo(
                        "Strategic thesis"
                );

        assertThat(response.origin())
                .isEqualTo(
                        StrategicSynthesisOrigin.AI_ASSISTED
                );

        assertThat(response.status())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(response.requiresReview())
                .isTrue();

        assertThat(response.approved())
                .isFalse();

        assertThat(response.rejected())
                .isFalse();

        assertThat(response.sourceArtifactCodes())
                .containsExactly(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                );

        assertThat(response.createdAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-14T18:00:00Z"
                        )
                );
    }

    @Test
    void shouldMapApprovedSnapshot() {
        StrategicSynthesisResponse response =
                StrategicSynthesisResponseMapper.toResponse(
                        new StoredStrategicSynthesis(
                                42L,
                                synthesis(
                                        StrategicSynthesisStatus.APPROVED
                                ),
                                Instant.parse(
                                        "2026-08-14T18:10:00Z"
                                )
                        )
                );

        assertThat(response.requiresReview())
                .isFalse();

        assertThat(response.approved())
                .isTrue();

        assertThat(response.rejected())
                .isFalse();
    }

    @Test
    void shouldMapRejectedSnapshot() {
        StrategicSynthesisResponse response =
                StrategicSynthesisResponseMapper.toResponse(
                        new StoredStrategicSynthesis(
                                43L,
                                synthesis(
                                        StrategicSynthesisStatus.REJECTED
                                ),
                                Instant.parse(
                                        "2026-08-14T18:20:00Z"
                                )
                        )
                );

        assertThat(response.requiresReview())
                .isFalse();

        assertThat(response.approved())
                .isFalse();

        assertThat(response.rejected())
                .isTrue();
    }

    @Test
    void shouldReturnNullForNullSnapshot() {
        assertThat(
                StrategicSynthesisResponseMapper.toResponse(
                        null
                )
        ).isNull();
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisStatus status
    ) {
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