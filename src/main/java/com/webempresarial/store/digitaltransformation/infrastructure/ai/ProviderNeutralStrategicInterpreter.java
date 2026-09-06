package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationPromptPolicy;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpreter;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationRequest;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Objects;


public class ProviderNeutralStrategicInterpreter
        implements StrategicInterpreter {

    private final StrategicAIClient aiClient;

    private final StrategicInterpretationPromptPolicy
            promptPolicy;

    public ProviderNeutralStrategicInterpreter(
            StrategicAIClient aiClient,
            StrategicInterpretationPromptPolicy promptPolicy
    ) {
        this.aiClient =
                Objects.requireNonNull(
                        aiClient,
                        "StrategicAIClient es obligatorio"
                );

        this.promptPolicy =
                Objects.requireNonNull(
                        promptPolicy,
                        "StrategicInterpretationPromptPolicy es obligatorio"
                );
    }

    @Override
    public StrategicInterpretationResult interpret(
            StrategicInterpretationRequest request
    ) {
        Objects.requireNonNull(
                request,
                "StrategicInterpretationRequest es obligatorio"
        );

        StrategicAIRequest aiRequest =
                new StrategicAIRequest(
                        promptPolicy.systemInstruction(),
                        promptPolicy.taskInstruction(
                                request
                        ),
                        request.getFinding(),
                        request.getBusinessProblem(),
                        request.getBusinessObjective(),
                        request.getStrategicOpportunity(),
                        request.getDeterministicThesis(),
                        request.getSourceArtifactCodes(),
                        request.getConstraints()
                                .stream()
                                .map(Enum::name)
                                .toList()
                );

        StrategicAIResponse response =
                Objects.requireNonNull(
                        aiClient.generate(
                                aiRequest
                        ),
                        "StrategicAIClient devolvió una respuesta nula"
                );

        return StrategicInterpretationResult.of(
                response.interpretedThesis(),
                response.executiveNarrative(),
                response.referencedArtifactCodes()
        );
    }
}