package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesisReview;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaStrategicSynthesisReviewStoreTest {

    @Mock
    private StrategicSynthesisReviewRecordRepository reviewRepository;

    @Mock
    private StrategicSynthesisRecordRepository synthesisRepository;

    private JpaStrategicSynthesisReviewStore store;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        store =
                new JpaStrategicSynthesisReviewStore(
                        reviewRepository,
                        synthesisRepository
                );
    }

    @Test
    void shouldSaveReviewBetweenPersistentSnapshots() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesis resulting =
                reviewed.withStatus(
                        StrategicSynthesisStatus.APPROVED
                );

        StrategicSynthesisReview review =
                review(
                        reviewed,
                        StrategicSynthesisReviewDecision.APPROVE
                );

        StrategicSynthesisRecord reviewedRecord =
                persistedRecord(
                        reviewed,
                        41L
                );

        StrategicSynthesisRecord resultingRecord =
                persistedRecord(
                        resulting,
                        42L
                );

        when(
                synthesisRepository.findById(
                        41L
                )
        ).thenReturn(
                Optional.of(
                        reviewedRecord
                )
        );

        when(
                synthesisRepository.findById(
                        42L
                )
        ).thenReturn(
                Optional.of(
                        resultingRecord
                )
        );

        when(
                reviewRepository.saveAndFlush(
                        any(StrategicSynthesisReviewRecord.class)
                )
        ).thenAnswer(invocation -> {
            StrategicSynthesisReviewRecord record =
                    invocation.getArgument(0);

            assignField(
                    record,
                    "id",
                    100L
            );

            return record;
        });

        StoredStrategicSynthesisReview saved =
                store.save(
                        41L,
                        42L,
                        review
                );

        assertThat(saved.id())
                .isEqualTo(
                        100L
                );

        assertThat(saved.reviewedSynthesisId())
                .isEqualTo(
                        41L
                );

        assertThat(saved.resultingSynthesisId())
                .isEqualTo(
                        42L
                );

        assertThat(saved.review().approved())
                .isTrue();

        verify(reviewRepository)
                .saveAndFlush(
                        any(StrategicSynthesisReviewRecord.class)
                );
    }

    @Test
    void shouldRejectWhenReviewedSnapshotDoesNotExist() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        when(
                synthesisRepository.findById(
                        41L
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                store.save(
                        41L,
                        42L,
                        review(
                                reviewed,
                                StrategicSynthesisReviewDecision.APPROVE
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "snapshot estratégico revisado"
                );

        verifyNoInteractions(
                reviewRepository
        );
    }

    @Test
    void shouldRejectWhenResultingSnapshotDoesNotExist() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        when(
                synthesisRepository.findById(
                        41L
                )
        ).thenReturn(
                Optional.of(
                        persistedRecord(
                                reviewed,
                                41L
                        )
                )
        );

        when(
                synthesisRepository.findById(
                        42L
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                store.save(
                        41L,
                        42L,
                        review(
                                reviewed,
                                StrategicSynthesisReviewDecision.APPROVE
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "snapshot estratégico resultante"
                );

        verifyNoInteractions(
                reviewRepository
        );
    }

    @Test
    void shouldFindLatestReviewByTenantProjectAndSynthesis() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesisReviewRecord record =
                persistedReviewRecord(
                        reviewed,
                        reviewed.withStatus(
                                StrategicSynthesisStatus.APPROVED
                        ),
                        41L,
                        42L,
                        101L
                );

        when(
                reviewRepository
                        .findFirstByReviewedSynthesisIdAndReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
                                41L,
                                20L,
                                10L
                        )
        ).thenReturn(
                Optional.of(
                        record
                )
        );

        Optional<StoredStrategicSynthesisReview> result =
                store.findLatestBySynthesis(
                        10L,
                        20L,
                        41L
                );

        assertThat(result)
                .isPresent();

        assertThat(
                result.orElseThrow().id()
        ).isEqualTo(
                101L
        );

        verify(reviewRepository)
                .findFirstByReviewedSynthesisIdAndReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
                        41L,
                        20L,
                        10L
                );
    }

    @Test
    void shouldReturnEmptyWhenNoReviewExists() {
        when(
                reviewRepository
                        .findFirstByReviewedSynthesisIdAndReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
                                41L,
                                20L,
                                10L
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThat(
                store.findLatestBySynthesis(
                        10L,
                        20L,
                        41L
                )
        ).isEmpty();
    }

    @Test
    void shouldFindAllReviewsByTenantAndProject() {
        StrategicSynthesis reviewed =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StrategicSynthesisReviewRecord first =
                persistedReviewRecord(
                        reviewed,
                        reviewed.withStatus(
                                StrategicSynthesisStatus.APPROVED
                        ),
                        41L,
                        42L,
                        101L
                );

        StrategicSynthesisReviewRecord second =
                persistedReviewRecord(
                        reviewed,
                        reviewed.withStatus(
                                StrategicSynthesisStatus.REJECTED
                        ),
                        51L,
                        52L,
                        102L,
                        StrategicSynthesisReviewDecision.REJECT
                );

        when(
                reviewRepository
                        .findAllByReviewedSynthesisProjectIdAndReviewedSynthesisProjectStoreIdOrderByReviewedAtDesc(
                                20L,
                                10L
                        )
        ).thenReturn(
                List.of(
                        second,
                        first
                )
        );

        List<StoredStrategicSynthesisReview> result =
                store.findAllByProject(
                        10L,
                        20L
                );

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).id())
                .isEqualTo(
                        102L
                );

        assertThat(result.get(0).review().rejected())
                .isTrue();

        assertThat(result.get(1).id())
                .isEqualTo(
                        101L
                );

        assertThat(result.get(1).review().approved())
                .isTrue();
    }

    @Test
    void shouldRejectInvalidReviewedSynthesisId() {
        assertThatThrownBy(() ->
                store.save(
                        0L,
                        42L,
                        mock(StrategicSynthesisReview.class)
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "reviewedSynthesisId"
                );

        verifyNoInteractions(
                synthesisRepository,
                reviewRepository
        );
    }

    @Test
    void shouldRejectInvalidResultingSynthesisId() {
        assertThatThrownBy(() ->
                store.save(
                        41L,
                        null,
                        mock(StrategicSynthesisReview.class)
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "resultingSynthesisId"
                );

        verifyNoInteractions(
                synthesisRepository,
                reviewRepository
        );
    }

    @Test
    void shouldRejectInvalidStoreIdWhenFindingLatestReview() {
        assertThatThrownBy(() ->
                store.findLatestBySynthesis(
                        0L,
                        20L,
                        41L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                reviewRepository
        );
    }

    @Test
    void shouldRejectInvalidProjectIdWhenFindingAllReviews() {
        assertThatThrownBy(() ->
                store.findAllByProject(
                        10L,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                reviewRepository
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

    private static StrategicSynthesisReview review(
            StrategicSynthesis synthesis,
            StrategicSynthesisReviewDecision decision
    ) {
        return StrategicSynthesisReview.record(
                synthesis,
                "consultant@webempresarial.com",
                StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                decision,
                "Review reason",
                Instant.parse(
                        "2026-08-14T18:00:00Z"
                )
        );
    }

    private static StrategicSynthesisRecord persistedRecord(
            StrategicSynthesis synthesis,
            Long id
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
                Instant.parse(
                        "2026-08-14T17:00:00Z"
                )
        );

        return record;
    }

    private static StrategicSynthesisReviewRecord persistedReviewRecord(
            StrategicSynthesis reviewed,
            StrategicSynthesis resulting,
            Long reviewedId,
            Long resultingId,
            Long reviewId
    ) {
        return persistedReviewRecord(
                reviewed,
                resulting,
                reviewedId,
                resultingId,
                reviewId,
                StrategicSynthesisReviewDecision.APPROVE
        );
    }

    private static StrategicSynthesisReviewRecord persistedReviewRecord(
            StrategicSynthesis reviewed,
            StrategicSynthesis resulting,
            Long reviewedId,
            Long resultingId,
            Long reviewId,
            StrategicSynthesisReviewDecision decision
    ) {
        StrategicSynthesisReviewRecord record =
                StrategicSynthesisReviewRecord.from(
                        persistedRecord(
                                reviewed,
                                reviewedId
                        ),
                        persistedRecord(
                                resulting,
                                resultingId
                        ),
                        review(
                                reviewed,
                                decision
                        )
                );

        assignField(
                record,
                "id",
                reviewId
        );

        return record;
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