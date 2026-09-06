package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultRequestStrategicInterpretationCommandTest {

    @Mock
    private StrategicSynthesisStore synthesisStore;

    @Mock
    private StrategicInterpretationOrchestrator
            interpretationOrchestrator;
    
    @Mock
    private ObjectProvider<StrategicInterpretationOrchestrator>
            interpretationOrchestratorProvider;



    private DefaultRequestStrategicInterpretationCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(
                interpretationOrchestratorProvider.getIfAvailable()
        ).thenReturn(
                interpretationOrchestrator
        );

        command =
                new DefaultRequestStrategicInterpretationCommand(
                        synthesisStore,
                        interpretationOrchestratorProvider
                );
    }
    
    
    
    @Test
    void shouldRejectInterpretationWhenAiOrchestratorIsUnavailable() {
        when(
                interpretationOrchestratorProvider.getIfAvailable()
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "IA no está disponible"
                );

        verifyNoInteractions(
                synthesisStore,
                interpretationOrchestrator
        );
    }

    @Test
    void shouldInterpretLatestDeterministicSynthesisAndPersistAiSnapshot() {
        StrategicSynthesis deterministic =
                synthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        StoredStrategicSynthesis storedDeterministic =
                stored(
                        41L,
                        deterministic
                );

        StrategicSynthesis ai =
                synthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        "AI interpreted thesis"
                );

        StrategicInterpretationOutcome outcome =
                mock(
                        StrategicInterpretationOutcome.class
                );

        StoredStrategicSynthesis storedAi =
                stored(
                        42L,
                        ai
                );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        storedDeterministic
                )
        );

        when(
                interpretationOrchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                outcome
        );

        when(
        		outcome.synthesis()
        		).thenReturn(
                ai
        );

        when(
                synthesisStore.saveSnapshot(
                        ai
                )
        ).thenReturn(
                storedAi
        );

        RequestStrategicInterpretationResult result =
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                );

        assertThat(result.deterministicSynthesis())
                .isSameAs(
                        storedDeterministic
                );

        assertThat(result.aiSynthesis())
                .isSameAs(
                        storedAi
                );

        assertThat(result.outcome())
                .isSameAs(
                        outcome
                );

        assertThat(result.aiSynthesis().synthesis().getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.AI_ASSISTED
                );

        assertThat(result.aiSynthesis().synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        verify(synthesisStore)
                .findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        verify(interpretationOrchestrator)
                .interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                );

        verify(synthesisStore)
                .saveSnapshot(
                        ai
                );
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.interpret(
                        0L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
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
                interpretationOrchestrator
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeCallingDependencies() {
        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        null,
                        StrategicInterpretationMode.REFINE_THESIS
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
                interpretationOrchestrator
        );
    }

    @Test
    void shouldRejectNullModeBeforeLoadingSynthesis() {
        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "modo"
                );

        verifyNoInteractions(
                synthesisStore,
                interpretationOrchestrator
        );
    }

    @Test
    void shouldRejectWhenNoDeterministicSynthesisExists() {
        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "No existe una síntesis determinista"
                );

        verifyNoInteractions(
                interpretationOrchestrator
        );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectNullOutcomeWithoutPersistence() {
        StrategicSynthesis deterministic =
                synthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                deterministic
                        )
                )
        );

        when(
                interpretationOrchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
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
    void shouldRejectNullAiSynthesisWithoutPersistence() {
        StrategicSynthesis deterministic =
                synthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        StrategicInterpretationOutcome outcome =
                mock(
                        StrategicInterpretationOutcome.class
                );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                deterministic
                        )
                )
        );

        when(
                interpretationOrchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                outcome
        );

        when(
        		outcome.synthesis()
        		).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "AI nula"
                );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectNonAiAssistedSynthesisWithoutPersistence() {
        StrategicSynthesis deterministic =
                synthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        StrategicInterpretationOutcome outcome =
                mock(
                        StrategicInterpretationOutcome.class
                );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                deterministic
                        )
                )
        );

        when(
                interpretationOrchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                outcome
        );

        when(
        		outcome.synthesis()
        		).thenReturn(
                deterministic
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "AI_ASSISTED"
                );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectAiSynthesisThatDoesNotRequireReview() {
        StrategicSynthesis deterministic =
                synthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        StrategicSynthesis invalidAi =
                synthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.APPROVED,
                        "AI thesis"
                );

        StrategicInterpretationOutcome outcome =
                mock(
                        StrategicInterpretationOutcome.class
                );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                deterministic
                        )
                )
        );

        when(
                interpretationOrchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                outcome
        );

        when(
        		outcome.synthesis()
        		).thenReturn(
                invalidAi
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "requerir revisión"
                );

        verify(synthesisStore, never())
                .saveSnapshot(
                        any()
                );
    }

    @Test
    void shouldRejectNullStoredAiSnapshot() {
        StrategicSynthesis deterministic =
                synthesis(
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        StrategicSynthesis ai =
                synthesis(
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        "AI thesis"
                );

        StrategicInterpretationOutcome outcome =
                mock(
                        StrategicInterpretationOutcome.class
                );

        when(
                synthesisStore.findLatestSnapshot(
                        10L,
                        20L,
                        StrategicSynthesisOrigin.DETERMINISTIC
                )
        ).thenReturn(
                Optional.of(
                        stored(
                                41L,
                                deterministic
                        )
                )
        );

        when(
                interpretationOrchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                outcome
        );

        when(
        		outcome.synthesis()
        		).thenReturn(
                ai
        );

        when(
                synthesisStore.saveSnapshot(
                        ai
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                command.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "snapshot AI nulo"
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
                        "2026-08-20T18:00:00Z"
                )
        );
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisOrigin origin,
            StrategicSynthesisStatus status,
            String thesis
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                thesis,
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001"
                        ),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
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