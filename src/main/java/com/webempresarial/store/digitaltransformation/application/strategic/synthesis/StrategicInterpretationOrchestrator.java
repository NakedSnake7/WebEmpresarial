package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import java.util.Objects;

public class StrategicInterpretationOrchestrator {

    private final StrategicInterpreter interpreter;

    private final StrategicInterpretationGuardrailValidator
            guardrailValidator;

    private final StrategicInterpretationTraceabilityRecorder
            traceabilityRecorder;

    public StrategicInterpretationOrchestrator(
            StrategicInterpreter interpreter,
            StrategicInterpretationGuardrailValidator guardrailValidator,
            StrategicInterpretationTraceabilityRecorder traceabilityRecorder
    ) {
        this.interpreter =
                Objects.requireNonNull(
                        interpreter,
                        "StrategicInterpreter es obligatorio"
                );

        this.guardrailValidator =
                Objects.requireNonNull(
                        guardrailValidator,
                        "StrategicInterpretationGuardrailValidator es obligatorio"
                );

        this.traceabilityRecorder =
                Objects.requireNonNull(
                        traceabilityRecorder,
                        "StrategicInterpretationTraceabilityRecorder es obligatorio"
                );
    }

    public StrategicInterpretationOutcome interpret(
            StrategicSynthesis deterministicSynthesis,
            StrategicInterpretationMode mode
    ) {
        Objects.requireNonNull(
                deterministicSynthesis,
                "La síntesis determinista es obligatoria"
        );

        Objects.requireNonNull(
                mode,
                "El modo de interpretación es obligatorio"
        );

        StrategicInterpretationRequest request =
                StrategicInterpretationRequest.from(
                        deterministicSynthesis,
                        mode
                );

        StrategicInterpretationResult interpretation =
                Objects.requireNonNull(
                        interpreter.interpret(
                                request
                        ),
                        "StrategicInterpreter devolvió un resultado nulo"
                );

        StrategicInterpretationValidationResult validation =
                Objects.requireNonNull(
                        guardrailValidator.validate(
                                request,
                                interpretation
                        ),
                        "El guardrail validator devolvió un resultado nulo"
                );

        if (!validation.isValid()) {
            throw new StrategicInterpretationRejectedException(
                    validation
            );
        }

        StrategicSynthesis aiSynthesis =
                StrategicSynthesis.create(
                        deterministicSynthesis.getProject(),
                        deterministicSynthesis.getFindingStatement(),
                        deterministicSynthesis.getBusinessProblemStatement(),
                        deterministicSynthesis.getBusinessObjectiveStatement(),
                        deterministicSynthesis.getStrategicOpportunityStatement(),
                        interpretation.getInterpretedThesis(),
                        deterministicSynthesis.getEvidenceSummary(),
                        deterministicSynthesis.getConfidence(),
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        deterministicSynthesis.getSourceArtifactCodes()
                );

        StrategicInterpretationAudit audit =
                StrategicInterpretationAudit.from(
                        request,
                        interpretation,
                        validation
                );

        traceabilityRecorder.record(
                deterministicSynthesis,
                aiSynthesis,
                audit
        );

        return new StrategicInterpretationOutcome(
                request,
                interpretation,
                validation,
                audit,
                aiSynthesis
        );
    }
}