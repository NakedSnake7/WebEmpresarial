package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesisReview;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StrategicSynthesisReviewRecordTest {

    @Test
    void shouldPreserveApprovedReviewThroughRecordRoundTrip() {
        StrategicSynthesis reviewedDomain =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesis resultingDomain =
                reviewedDomain.withStatus(
                        StrategicSynthesisStatus.APPROVED
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        reviewedDomain,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis representa correctamente la estrategia.",
                        Instant.parse(
                                "2026-08-14T18:00:00Z"
                        )
                );

        StrategicSynthesisRecord reviewedRecord =
                persistedRecord(
                        reviewedDomain,
                        41L,
                        Instant.parse(
                                "2026-08-14T17:50:00Z"
                        )
                );

        StrategicSynthesisRecord resultingRecord =
                persistedRecord(
                        resultingDomain,
                        42L,
                        Instant.parse(
                                "2026-08-14T18:00:01Z"
                        )
                );

        StrategicSynthesisReviewRecord record =
                StrategicSynthesisReviewRecord.from(
                        reviewedRecord,
                        resultingRecord,
                        review
                );

        assignReviewId(
                record,
                100L
        );

        StoredStrategicSynthesisReview stored =
                record.toStoredReview();

        assertThat(stored.id())
                .isEqualTo(
                        100L
                );

        assertThat(stored.reviewedSynthesisId())
                .isEqualTo(
                        41L
                );

        assertThat(stored.resultingSynthesisId())
                .isEqualTo(
                        42L
                );

        assertThat(stored.review().approved())
                .isTrue();

        assertThat(stored.review().getReviewer())
                .isEqualTo(
                        "consultant@webempresarial.com"
                );

        assertThat(stored.review().getReviewerType())
                .isEqualTo(
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );

        assertThat(stored.review().getPreviousStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(stored.review().getResultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.APPROVED
                );

        assertThat(stored.review().getReviewedAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-14T18:00:00Z"
                        )
                );
    }

    @Test
    void shouldPreserveRejectedReviewThroughRecordRoundTrip() {
        StrategicSynthesis reviewedDomain =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesis resultingDomain =
                reviewedDomain.withStatus(
                        StrategicSynthesisStatus.REJECTED
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        reviewedDomain,
                        "owner@example.com",
                        StrategicSynthesisReviewerType.PROJECT_OWNER,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La tesis requiere ajustes.",
                        Instant.parse(
                                "2026-08-14T18:10:00Z"
                        )
                );

        StrategicSynthesisReviewRecord record =
                StrategicSynthesisReviewRecord.from(
                        persistedRecord(
                                reviewedDomain,
                                51L,
                                Instant.parse(
                                        "2026-08-14T18:00:00Z"
                                )
                        ),
                        persistedRecord(
                                resultingDomain,
                                52L,
                                Instant.parse(
                                        "2026-08-14T18:10:01Z"
                                )
                        ),
                        review
                );

        assignReviewId(
                record,
                101L
        );

        StoredStrategicSynthesisReview stored =
                record.toStoredReview();

        assertThat(stored.review().rejected())
                .isTrue();

        assertThat(stored.review().getResultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REJECTED
                );
    }

    @Test
    void shouldRejectResultingSnapshotWithWrongStatus() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        reviewed,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved.",
                        Instant.parse(
                                "2026-08-14T18:20:00Z"
                        )
                );

        StrategicSynthesis wrongResult =
                reviewed.withStatus(
                        StrategicSynthesisStatus.REJECTED
                );

        assertThatThrownBy(() ->
                StrategicSynthesisReviewRecord.from(
                        persistedRecord(
                                reviewed,
                                61L,
                                Instant.parse(
                                        "2026-08-14T18:15:00Z"
                                )
                        ),
                        persistedRecord(
                                wrongResult,
                                62L,
                                Instant.parse(
                                        "2026-08-14T18:20:01Z"
                                )
                        ),
                        review
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "resultado de la revisión"
                );
    }

    @Test
    void shouldRejectReviewForDifferentStrategicThesis() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesis different =
                StrategicSynthesis.create(
                        reviewed.getProject(),
                        reviewed.getFindingStatement(),
                        reviewed.getBusinessProblemStatement(),
                        reviewed.getBusinessObjectiveStatement(),
                        reviewed.getStrategicOpportunityStatement(),
                        "Completely different thesis",
                        reviewed.getEvidenceSummary(),
                        reviewed.getConfidence(),
                        reviewed.getOrigin(),
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        reviewed.getSourceArtifactCodes()
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        different,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved.",
                        Instant.parse(
                                "2026-08-14T18:30:00Z"
                        )
                );

        StrategicSynthesis resulting =
                reviewed.withStatus(
                        StrategicSynthesisStatus.APPROVED
                );

        assertThatThrownBy(() ->
                StrategicSynthesisReviewRecord.from(
                        persistedRecord(
                                reviewed,
                                71L,
                                Instant.parse(
                                        "2026-08-14T18:25:00Z"
                                )
                        ),
                        persistedRecord(
                                resulting,
                                72L,
                                Instant.parse(
                                        "2026-08-14T18:30:01Z"
                                )
                        ),
                        review
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "síntesis persistida"
                );
    }

    @Test
    void shouldRejectConversionBeforeReviewRecordIsPersisted() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesis resulting =
                reviewed.withStatus(
                        StrategicSynthesisStatus.APPROVED
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        reviewed,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Approved.",
                        Instant.parse(
                                "2026-08-14T18:40:00Z"
                        )
                );

        StrategicSynthesisReviewRecord record =
                StrategicSynthesisReviewRecord.from(
                        persistedRecord(
                                reviewed,
                                81L,
                                Instant.parse(
                                        "2026-08-14T18:35:00Z"
                                )
                        ),
                        persistedRecord(
                                resulting,
                                82L,
                                Instant.parse(
                                        "2026-08-14T18:40:01Z"
                                )
                        ),
                        review
                );

        assertThatThrownBy(
                record::toStoredReview
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "persistido"
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

    private static StrategicSynthesisRecord persistedRecord(
            StrategicSynthesis synthesis,
            Long id,
            Instant createdAt
    ) {
        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        synthesis
                );

        assignField(
                record,
                "id",
                id
        );

        assignField(
                record,
                "createdAt",
                createdAt
        );

        return record;
    }

    private static void assignReviewId(
            StrategicSynthesisReviewRecord record,
            Long id
    ) {
        assignField(
                record,
                "id",
                id
        );
    }

    private static void assignField(
            Object target,
            String fieldName,
            Object value
    ) {
        try {
            Field field =
                    target.getClass()
                            .getDeclaredField(
                                    fieldName
                            );

            field.setAccessible(
                    true
            );

            field.set(
                    target,
                    value
            );

        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "No fue posible preparar el fixture persistente",
                    exception
            );
        }
    }
}