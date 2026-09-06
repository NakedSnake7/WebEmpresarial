package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
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

class JpaStrategicSynthesisStoreTest {

    @Mock
    private StrategicSynthesisRecordRepository repository;

    private JpaStrategicSynthesisStore store;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        store =
                new JpaStrategicSynthesisStore(
                        repository
                );
    }

    @Test
    void shouldSaveSynthesisAsPersistentSnapshot() {
        StrategicSynthesis synthesis =
                synthesis();

        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        synthesis
                );

        assignPersistenceIdentity(
                record,
                100L,
                Instant.parse(
                        "2026-08-14T17:00:00Z"
                )
        );

        when(
                repository.saveAndFlush(
                        any(StrategicSynthesisRecord.class)
                )
        ).thenReturn(
                record
        );

        StoredStrategicSynthesis saved =
                store.saveSnapshot(
                        synthesis
                );

        assertThat(saved.id())
                .isEqualTo(
                        100L
                );

        assertThat(saved.createdAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-14T17:00:00Z"
                        )
                );

        assertThat(saved.synthesis().getStrategicThesis())
                .isEqualTo(
                        synthesis.getStrategicThesis()
                );

        assertThat(saved.synthesis().getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThat(saved.synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );

        assertThat(
                saved.synthesis()
                        .getSourceArtifactCodes()
        ).containsExactlyElementsOf(
                synthesis.getSourceArtifactCodes()
        );

        verify(repository)
                .saveAndFlush(
                        any(StrategicSynthesisRecord.class)
                );
    }

    @Test
    void shouldFindLatestSnapshotByTenantProjectAndOrigin() {
        StrategicSynthesis synthesis =
                synthesis();

        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        synthesis
                );

        assignPersistenceIdentity(
                record,
                200L,
                Instant.parse(
                        "2026-08-14T18:00:00Z"
                )
        );

        when(
                repository
                        .findFirstByProjectIdAndProjectStoreIdAndOriginOrderByCreatedAtDesc(
                                20L,
                                10L,
                                StrategicSynthesisOrigin.DETERMINISTIC
                        )
        ).thenReturn(
                Optional.of(
                        record
                )
        );

        Optional<StoredStrategicSynthesis> result =
                store.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThat(result)
                .isPresent();

        StoredStrategicSynthesis stored =
                result.orElseThrow();

        assertThat(stored.id())
                .isEqualTo(
                        200L
                );

        assertThat(stored.synthesis().getStrategicThesis())
                .isEqualTo(
                        synthesis.getStrategicThesis()
                );

        verify(repository)
                .findFirstByProjectIdAndProjectStoreIdAndOriginOrderByCreatedAtDesc(
                        20L,
                        10L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                );
    }

    @Test
    void shouldReturnEmptyWhenNoSnapshotExists() {
        when(
                repository
                        .findFirstByProjectIdAndProjectStoreIdAndOriginOrderByCreatedAtDesc(
                                20L,
                                10L,
                                StrategicSynthesisOrigin.AI_ASSISTED
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThat(
                store.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.AI_ASSISTED
                )
        ).isEmpty();
    }

    @Test
    void shouldFindSnapshotByPersistentIdentityAndTenant() {
        StrategicSynthesisRecord record =
                StrategicSynthesisRecord.from(
                        synthesis()
                );

        assignPersistenceIdentity(
                record,
                300L,
                Instant.parse(
                        "2026-08-14T19:00:00Z"
                )
        );

        when(
                repository
                        .findByIdAndProjectIdAndProjectStoreId(
                                300L,
                                20L,
                                10L
                        )
        ).thenReturn(
                Optional.of(
                        record
                )
        );

        Optional<StoredStrategicSynthesis> result =
                store.findSnapshot(
                        10L,
                        20L,
                        300L
                );

        assertThat(result)
                .isPresent();

        assertThat(
                result.orElseThrow().id()
        ).isEqualTo(
                300L
        );

        verify(repository)
                .findByIdAndProjectIdAndProjectStoreId(
                        300L,
                        20L,
                        10L
                );
    }

    @Test
    void shouldFindAllSnapshots() {
        StrategicSynthesis deterministic =
                synthesis();

        StrategicSynthesis ai =
                StrategicSynthesis.create(
                        deterministic.getProject(),
                        deterministic.getFindingStatement(),
                        deterministic.getBusinessProblemStatement(),
                        deterministic.getBusinessObjectiveStatement(),
                        deterministic.getStrategicOpportunityStatement(),
                        "AI thesis",
                        deterministic.getEvidenceSummary(),
                        deterministic.getConfidence(),
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        deterministic.getSourceArtifactCodes()
                );

        StrategicSynthesisRecord aiRecord =
                StrategicSynthesisRecord.from(
                        ai
                );

        StrategicSynthesisRecord deterministicRecord =
                StrategicSynthesisRecord.from(
                        deterministic
                );

        assignPersistenceIdentity(
                aiRecord,
                400L,
                Instant.parse(
                        "2026-08-14T20:00:00Z"
                )
        );

        assignPersistenceIdentity(
                deterministicRecord,
                399L,
                Instant.parse(
                        "2026-08-14T19:00:00Z"
                )
        );

        when(
                repository
                        .findAllByProjectIdAndProjectStoreIdOrderByCreatedAtDesc(
                                20L,
                                10L
                        )
        ).thenReturn(
                List.of(
                        aiRecord,
                        deterministicRecord
                )
        );

        List<StoredStrategicSynthesis> result =
                store.findAllSnapshots(
                        10L,
                        20L
                );

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).id())
                .isEqualTo(
                        400L
                );

        assertThat(result.get(0).synthesis().getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.AI_ASSISTED
                );

        assertThat(result.get(1).id())
                .isEqualTo(
                        399L
                );

        assertThat(result.get(1).synthesis().getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );
    }

    @Test
    void shouldRejectInvalidStoreId() {
        assertThatThrownBy(() ->
                store.findLatestSnapshot(
                        0L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                repository
        );
    }

    @Test
    void shouldRejectInvalidProjectId() {
        assertThatThrownBy(() ->
                store.findAllSnapshots(
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
                repository
        );
    }

    @Test
    void shouldRejectInvalidSynthesisId() {
        assertThatThrownBy(() ->
                store.findSnapshot(
                        10L,
                        20L,
                        0L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "synthesisId"
                );

        verifyNoInteractions(
                repository
        );
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThatThrownBy(() ->
                store.findLatestSnapshot(
                        10L,
                        20L,
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "origen"
                );

        verifyNoInteractions(
                repository
        );
    }

    private static StrategicSynthesis synthesis() {
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
                                "EVD-001",
                                "EVD-002"
                        ),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                StrategicSynthesisOrigin.DETERMINISTIC,
                StrategicSynthesisStatus.READY,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }

    private static void assignPersistenceIdentity(
            StrategicSynthesisRecord record,
            Long id,
            Instant createdAt
    ) {
        try {
            Field idField =
                    StrategicSynthesisRecord.class
                            .getDeclaredField(
                                    "id"
                            );

            idField.setAccessible(
                    true
            );

            idField.set(
                    record,
                    id
            );

            Field createdAtField =
                    StrategicSynthesisRecord.class
                            .getDeclaredField(
                                    "createdAt"
                            );

            createdAtField.setAccessible(
                    true
            );

            createdAtField.set(
                    record,
                    createdAt
            );

        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "No fue posible preparar el record persistido para la prueba",
                    exception
            );
        }
    }
}