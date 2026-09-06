package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DefaultRequestStrategicInterpretationCommand
        implements RequestStrategicInterpretationCommand {

	private final StrategicSynthesisStore synthesisStore;

	private final ObjectProvider<StrategicInterpretationOrchestrator>
	        interpretationOrchestratorProvider;

	public DefaultRequestStrategicInterpretationCommand(
	        StrategicSynthesisStore synthesisStore,
	        ObjectProvider<StrategicInterpretationOrchestrator>
	                interpretationOrchestratorProvider
	) {
	    this.synthesisStore =
	            Objects.requireNonNull(
	                    synthesisStore,
	                    "StrategicSynthesisStore es obligatorio"
	            );

	    this.interpretationOrchestratorProvider =
	            Objects.requireNonNull(
	                    interpretationOrchestratorProvider,
	                    "StrategicInterpretationOrchestrator provider es obligatorio"
	            );
	}

    @Override
    @Transactional
    public RequestStrategicInterpretationResult interpret(
            Long storeId,
            Long projectId,
            StrategicInterpretationMode mode
    ) {
        requirePositive(
                storeId,
                "storeId"
        );

        requirePositive(
                projectId,
                "projectId"
        );

        Objects.requireNonNull(
                mode,
                "El modo de interpretación es obligatorio"
        );

        /*
         * AI es una capacidad opcional de la plataforma.
         *
         * El command existe siempre para que el ApplicationContext,
         * MVC y el Workspace puedan operar aun cuando ningún
         * StrategicInterpreter esté configurado.
         */
        StrategicInterpretationOrchestrator orchestrator =
                interpretationOrchestratorProvider.getIfAvailable();

        if (orchestrator == null) {
            throw new IllegalStateException(
                    "La interpretación estratégica asistida por IA no está disponible"
            );
        }

        StoredStrategicSynthesis deterministic =
                synthesisStore
                        .findLatestSnapshot(
                                storeId,
                                projectId,
                                StrategicSynthesisOrigin.DETERMINISTIC
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe una síntesis determinista disponible para interpretación"
                                )
                        );

        StrategicInterpretationOutcome outcome =
                Objects.requireNonNull(
                        orchestrator.interpret(
                                deterministic.synthesis(),
                                mode
                        ),
                        "StrategicInterpretationOrchestrator devolvió un resultado nulo"
                );

        StrategicSynthesis aiSynthesis =
                Objects.requireNonNull(
                        outcome.synthesis(),
                        "StrategicInterpretationOutcome devolvió una síntesis AI nula"
                );

        if (aiSynthesis.getOrigin()
                != StrategicSynthesisOrigin.AI_ASSISTED) {

            throw new IllegalStateException(
                    "La interpretación debe producir una síntesis AI_ASSISTED"
            );
        }

        if (aiSynthesis.getStatus()
                != StrategicSynthesisStatus.REQUIRES_REVIEW) {

            throw new IllegalStateException(
                    "La interpretación AI debe requerir revisión"
            );
        }

        StoredStrategicSynthesis storedAi =
                Objects.requireNonNull(
                        synthesisStore.saveSnapshot(
                                aiSynthesis
                        ),
                        "StrategicSynthesisStore devolvió un snapshot AI nulo"
                );

        return new RequestStrategicInterpretationResult(
                deterministic,
                storedAi,
                outcome
        );
    }

    private static void requirePositive(
            Long value,
            String name
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    name + " debe ser válido"
            );
        }
    }
}