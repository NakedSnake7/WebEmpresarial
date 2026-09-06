package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIClient;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIRequest;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIResponse;

import java.util.List;
import java.util.Objects;

public class OpenAiStrategicAIClient
        implements StrategicAIClient {

    private final OpenAiStructuredResponseGateway
            gateway;

    public OpenAiStrategicAIClient(
            OpenAiStructuredResponseGateway gateway
    ) {
        this.gateway =
                Objects.requireNonNull(
                        gateway,
                        "OpenAiStructuredResponseGateway es obligatorio"
                );
    }

    @Override
    public StrategicAIResponse generate(
            StrategicAIRequest request
    ) {
        Objects.requireNonNull(
                request,
                "StrategicAIRequest es obligatorio"
        );

        String input =
                OpenAiStrategicPromptBuilder
                        .buildInput(
                                request
                        );

        final OpenAiStrategicOutput output;

        try {
            output =
                    gateway.generate(
                            request.systemInstruction(),
                            input
                    );

        } catch (RuntimeException exception) {
            throw new OpenAiStrategicAIException(
                    "Falló la generación estratégica mediante OpenAI",
                    exception
            );
        }

        if (output == null) {
            throw new OpenAiStrategicAIException(
                    "OpenAI devolvió una respuesta estructurada nula"
            );
        }

        if (output.interpretedThesis == null
                || output.interpretedThesis.isBlank()) {

            throw new OpenAiStrategicAIException(
                    "OpenAI no devolvió una tesis estratégica válida"
            );
        }

        return new StrategicAIResponse(
                output.interpretedThesis,
                output.executiveNarrative,
                output.referencedArtifactCodes == null
                        ? List.of()
                        : output.referencedArtifactCodes
        );
    }
}