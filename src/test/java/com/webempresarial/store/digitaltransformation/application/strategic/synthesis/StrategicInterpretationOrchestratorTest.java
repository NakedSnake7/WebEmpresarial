package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StrategicInterpretationOrchestratorTest {

    @Mock
    private StrategicInterpreter interpreter;

    @Mock
    private StrategicInterpretationGuardrailValidator
            guardrailValidator;
    @Mock
    private StrategicInterpretationTraceabilityRecorder
            traceabilityRecorder;

    private StrategicInterpretationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        orchestrator =
                new StrategicInterpretationOrchestrator(
                        interpreter,
                        guardrailValidator,
                        traceabilityRecorder
                );
    }

    @Test
    void shouldProduceAiAssistedSynthesisFromValidInterpretation() {
        StrategicSynthesis deterministic =
                deterministicSynthesis();
        

        StrategicInterpretationResult interpretation =
                StrategicInterpretationResult.of(
                        "AI refined strategic thesis",
                        "Executive narrative",
                        deterministic.getSourceArtifactCodes()
                );
        
        

        when(
                interpreter.interpret(
                        any()
                )
        ).thenReturn(
                interpretation
        );

        when(
                guardrailValidator.validate(
                        any(),
                        same(interpretation)
                )
        ).thenReturn(
                StrategicInterpretationValidationResult.valid()
        );

        StrategicInterpretationOutcome outcome =
                orchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                );

        assertThat(outcome.synthesis().getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.AI_ASSISTED
                );

        assertThat(outcome.synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(outcome.synthesis().getStrategicThesis())
                .isEqualTo(
                        "AI refined strategic thesis"
                );

        /*
         * El contexto estratégico autorizado
         * permanece intacto.
         */
        assertThat(outcome.synthesis().getFindingStatement())
                .isEqualTo(
                        deterministic.getFindingStatement()
                );

        assertThat(outcome.synthesis().getBusinessProblemStatement())
                .isEqualTo(
                        deterministic.getBusinessProblemStatement()
                );

        assertThat(outcome.synthesis().getBusinessObjectiveStatement())
                .isEqualTo(
                        deterministic.getBusinessObjectiveStatement()
                );

        assertThat(outcome.synthesis().getStrategicOpportunityStatement())
                .isEqualTo(
                        deterministic.getStrategicOpportunityStatement()
                );

        assertThat(outcome.synthesis().getEvidenceSummary())
                .isSameAs(
                        deterministic.getEvidenceSummary()
                );

        assertThat(outcome.synthesis().getConfidence())
                .isEqualTo(
                        deterministic.getConfidence()
                );

        assertThat(deterministic.getOrigin())
                .isEqualTo(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThat(deterministic.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );
        assertThat(outcome.audit().getMode())
        .isEqualTo(
                StrategicInterpretationMode.REFINE_THESIS
        );

assertThat(outcome.audit().getValidationStatus())
        .isEqualTo(
                StrategicInterpretationValidationStatus.VALID
        );

assertThat(outcome.audit().getSourceArtifactCodes())
        .containsExactlyElementsOf(
                deterministic.getSourceArtifactCodes()
        );

assertThat(outcome.audit().getReferencedArtifactCodes())
        .containsExactlyElementsOf(
                deterministic.getSourceArtifactCodes()
        );
        
        verify(traceabilityRecorder)
        .record(
                same(deterministic),
                same(outcome.synthesis()),
                same(outcome.audit())
        );
    }

    @Test
    void shouldPassClosedStrategicContextToInterpreter() {
        StrategicSynthesis deterministic =
                deterministicSynthesis();

        StrategicInterpretationResult interpretation =
                StrategicInterpretationResult.of(
                        "Refined thesis",
                        null,
                        deterministic.getSourceArtifactCodes()
                );

        when(
                interpreter.interpret(
                        any()
                )
        ).thenReturn(
                interpretation
        );

        when(
                guardrailValidator.validate(
                        any(),
                        same(interpretation)
                )
        ).thenReturn(
                StrategicInterpretationValidationResult.valid()
        );

        orchestrator.interpret(
                deterministic,
                StrategicInterpretationMode.EXECUTIVE_SYNTHESIS
        );

        ArgumentCaptor<StrategicInterpretationRequest> captor =
                ArgumentCaptor.forClass(
                        StrategicInterpretationRequest.class
                );

        verify(interpreter)
                .interpret(
                        captor.capture()
                );

        StrategicInterpretationRequest request =
                captor.getValue();

        assertThat(request.getMode())
                .isEqualTo(
                        StrategicInterpretationMode.EXECUTIVE_SYNTHESIS
                );

        assertThat(request.getFinding())
                .isEqualTo(
                        deterministic.getFindingStatement()
                );

        assertThat(request.getBusinessProblem())
                .isEqualTo(
                        deterministic.getBusinessProblemStatement()
                );

        assertThat(request.getBusinessObjective())
                .isEqualTo(
                        deterministic.getBusinessObjectiveStatement()
                );

        assertThat(request.getStrategicOpportunity())
                .isEqualTo(
                        deterministic.getStrategicOpportunityStatement()
                );

        assertThat(request.getConstraints())
                .contains(
                        StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_FACTS,
                        StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_OBJECTIVES,
                        StrategicInterpretationConstraint.DO_NOT_INTRODUCE_NEW_OPPORTUNITIES
                );
        StrategicInterpretationOutcome outcome =
                orchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.EXECUTIVE_SYNTHESIS
                );
        
        verify(traceabilityRecorder)
        .record(
                same(deterministic),
                same(outcome.synthesis()),
                same(outcome.audit())
        );
    }

    @Test
    void shouldRejectInvalidInterpretation() {
        StrategicSynthesis deterministic =
                deterministicSynthesis();

        StrategicInterpretationResult interpretation =
                StrategicInterpretationResult.of(
                        "Unsupported thesis",
                        null,
                        List.of(
                                "UNKNOWN-999"
                        )
                );

        StrategicInterpretationValidationResult validation =
                StrategicInterpretationValidationResult.of(
                        StrategicInterpretationValidationStatus.INVALID,
                        List.of(
                                StrategicInterpretationViolation.UNKNOWN_SOURCE_ARTIFACT
                        )
                );

        when(
                interpreter.interpret(
                        any()
                )
        ).thenReturn(
                interpretation
        );

        when(
                guardrailValidator.validate(
                        any(),
                        same(interpretation)
                )
        ).thenReturn(
                validation
        );

        assertThatThrownBy(() ->
                orchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        StrategicInterpretationRejectedException.class
                );

        assertThat(deterministic.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );
        verifyNoInteractions(
                traceabilityRecorder
        );
    }

    @Test
    void shouldNotPromoteRequiresReviewGuardrailResultIntoSynthesis() {
        StrategicSynthesis deterministic =
                deterministicSynthesis();

        StrategicInterpretationResult interpretation =
                StrategicInterpretationResult.of(
                        "Potential thesis",
                        null,
                        List.of()
                );

        StrategicInterpretationValidationResult validation =
                StrategicInterpretationValidationResult.of(
                        StrategicInterpretationValidationStatus.REQUIRES_REVIEW,
                        List.of(
                                StrategicInterpretationViolation.SOURCE_ALIGNMENT_MISSING
                        )
                );

        when(
                interpreter.interpret(
                        any()
                )
        ).thenReturn(
                interpretation
        );

        when(
                guardrailValidator.validate(
                        any(),
                        same(interpretation)
                )
        ).thenReturn(
                validation
        );

        assertThatThrownBy(() ->
                orchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        StrategicInterpretationRejectedException.class
                )
                .satisfies(error -> {
                    StrategicInterpretationRejectedException exception =
                            (StrategicInterpretationRejectedException) error;

                    assertThat(
                            exception.getValidation().getStatus()
                    ).isEqualTo(
                            StrategicInterpretationValidationStatus.REQUIRES_REVIEW
                    );
                    
                    verifyNoInteractions(
                            traceabilityRecorder
                    );
                });
    }

    @Test
    void shouldRejectNullInterpreterResult() {
        StrategicSynthesis deterministic =
                deterministicSynthesis();

        when(
                interpreter.interpret(
                        any()
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                orchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );

        verifyNoInteractions(
                guardrailValidator,
                traceabilityRecorder
        );
    }

    @Test
    void shouldRejectNullValidationResult() {
        StrategicSynthesis deterministic =
                deterministicSynthesis();

        StrategicInterpretationResult interpretation =
                StrategicInterpretationResult.of(
                        "Refined thesis",
                        null,
                        deterministic.getSourceArtifactCodes()
                );

        when(
                interpreter.interpret(
                        any()
                )
        ).thenReturn(
                interpretation
        );

        when(
                guardrailValidator.validate(
                        any(),
                        same(interpretation)
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                orchestrator.interpret(
                        deterministic,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "validator"
                );
        verifyNoInteractions(
                traceabilityRecorder
        );
    }

    private static StrategicSynthesis deterministicSynthesis() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                "Deterministic strategic thesis",
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
                StrategicSynthesisStatus.READY,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}