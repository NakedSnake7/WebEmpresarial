package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicSynthesisStore;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.SubmitStrategicSynthesisForReviewService;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.SubmitStrategicSynthesisResult;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultSubmitStrategicSynthesisForReviewCommandTest {

    @Mock
    private StrategicSynthesisStore synthesisStore;

    @Mock
    private SubmitStrategicSynthesisForReviewService submitService;

    private DefaultSubmitStrategicSynthesisForReviewCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        command =
                new DefaultSubmitStrategicSynthesisForReviewCommand(
                        synthesisStore,
                        submitService
                );
    }

    @Test
    void shouldSubmitStoredSynthesisForReviewAndPersistNewSnapshot() {
        StrategicSynthesis original =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        StoredStrategicSynthesis storedOriginal =
                stored(
                        41L,
                        original
                );

        StrategicSynthesis updated =
                original.withStatus(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        SubmitStrategicSynthesisResult submitResult =
                new SubmitStrategicSynthesisResult(
                        updated,
                        StrategicSynthesisStatus.READY,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        StoredStrategicSynthesis storedUpdated =
                stored(
                        42L,
                        updated
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                Optional.of(
                        storedOriginal
                )
        );

        when(
                submitService.submit(
                        original
                )
        ).thenReturn(
                submitResult
        );

        when(
                synthesisStore.saveSnapshot(
                        updated
                )
        ).thenReturn(
                storedUpdated
        );

        StoredStrategicSynthesis result =
                command.submit(
                        10L,
                        20L,
                        41L
                );

        assertThat(result)
                .isSameAs(
                        storedUpdated
                );

        assertThat(result.id())
                .isEqualTo(
                        42L
                );

        assertThat(result.synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        verify(synthesisStore)
                .findSnapshot(
                        10L,
                        20L,
                        41L
                );

        verify(submitService)
                .submit(
                        original
                );

        verify(synthesisStore)
                .saveSnapshot(
                        updated
                );
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.submit(
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
                synthesisStore,
                submitService
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        null,
                        41L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                synthesisStore,
                submitService
        );
    }

    @Test
    void shouldRejectInvalidSynthesisIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        20L,
                        -1L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "synthesisId"
                );

        verifyNoInteractions(
                synthesisStore,
                submitService
        );
    }

    @Test
    void shouldRejectWhenSnapshotDoesNotBelongToTenantProject() {
        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        999L
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        20L,
                        999L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece al proyecto"
                );

        verifyNoInteractions(
                submitService
        );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectNullSubmitResultWithoutPersistence() {
        StrategicSynthesis original =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                original
                        )
                )
        );

        when(
                submitService.submit(
                        original
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        20L,
                        41L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectNullUpdatedSynthesisWithoutPersistence() {
        StrategicSynthesis original =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        SubmitStrategicSynthesisResult submitResult =
                mock(
                        SubmitStrategicSynthesisResult.class
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                original
                        )
                )
        );

        when(
                submitService.submit(
                        original
                )
        ).thenReturn(
                submitResult
        );

        when(
                submitResult.synthesis()
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        20L,
                        41L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "síntesis nula"
                );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectNullStoredSnapshotReturnedByStore() {
        StrategicSynthesis original =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        StrategicSynthesis updated =
                original.withStatus(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        SubmitStrategicSynthesisResult submitResult =
                new SubmitStrategicSynthesisResult(
                        updated,
                        StrategicSynthesisStatus.READY,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                original
                        )
                )
        );

        when(
                submitService.submit(
                        original
                )
        ).thenReturn(
                submitResult
        );

        when(
                synthesisStore.saveSnapshot(
                        updated
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        20L,
                        41L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "snapshot nulo"
                );
    }

    @Test
    void shouldNotPersistWhenLifecycleRejectsSubmission() {
        StrategicSynthesis alreadyInReview =
                synthesis(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        when(
                synthesisStore.findSnapshot(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                alreadyInReview
                        )
                )
        );

        when(
                submitService.submit(
                        alreadyInReview
                )
        ).thenThrow(
                new IllegalStateException(
                        "Solo una síntesis READY o DRAFT puede enviarse a revisión"
                )
        );

        assertThatThrownBy(() ->
                command.submit(
                        10L,
                        20L,
                        41L
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "READY o DRAFT"
                );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldUseTenantProjectAndSnapshotIdentityWhenLoadingSynthesis() {
        StrategicSynthesis original =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        StrategicSynthesis updated =
                original.withStatus(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        SubmitStrategicSynthesisResult submitResult =
                new SubmitStrategicSynthesisResult(
                        updated,
                        StrategicSynthesisStatus.READY,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        when(
                synthesisStore.findSnapshot(
                        77L,
                        200L,
                        900L
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                900L,
                                original
                        )
                )
        );

        when(
                submitService.submit(
                        original
                )
        ).thenReturn(
                submitResult
        );

        when(
                synthesisStore.saveSnapshot(
                        updated
                )
        ).thenReturn(
                stored(
                        901L,
                        updated
                )
        );

        command.submit(
                77L,
                200L,
                900L
        );

        verify(synthesisStore)
                .findSnapshot(
                        77L,
                        200L,
                        900L
                );
    }

    private static StoredStrategicSynthesis stored(
            Long id,
            StrategicSynthesis synthesis
    ) {
        return new StoredStrategicSynthesis(
                id,
                synthesis,
                Instant.parse(
                        "2026-08-21T02:00:00Z"
                )
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
                                "EVD-001"
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