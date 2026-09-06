package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.StructuredResponseCreateParams;

import java.util.Objects;

public class OpenAiSdkStructuredResponseGateway
        implements OpenAiStructuredResponseGateway {

    private final OpenAIClient client;

    private final OpenAiProperties properties;

    public OpenAiSdkStructuredResponseGateway(
            OpenAIClient client,
            OpenAiProperties properties
    ) {
        this.client =
                Objects.requireNonNull(
                        client,
                        "OpenAIClient es obligatorio"
                );

        this.properties =
                Objects.requireNonNull(
                        properties,
                        "OpenAiProperties es obligatorio"
                );

        this.properties.validate();
    }

    @Override
    public OpenAiStrategicOutput generate(
            String systemInstruction,
            String input
    ) {
        requireText(
                systemInstruction,
                "La instrucción de sistema es obligatoria"
        );

        requireText(
                input,
                "El input estratégico es obligatorio"
        );

        /*
         * Structured Outputs:
         *
         * OpenAiStrategicOutput.class genera automáticamente
         * el JSON Schema requerido por Responses API.
         */
        StructuredResponseCreateParams<OpenAiStrategicOutput>
                params =
                com.openai.models.responses.ResponseCreateParams
                        .builder()
                        .instructions(
                                systemInstruction
                        )
                        .input(
                                input
                        )
                        .text(
                                OpenAiStrategicOutput.class
                        )
                        .model(
                                properties.getModel()
                        )
                        .maxOutputTokens(
                                properties.getMaxOutputTokens()
                        )
                        .build();

        return client.responses()
                .create(
                        params
                )
                .output()
                .stream()

                /*
                 * ResponseOutputItem
                 *      ↓
                 * ResponseOutputMessage
                 */
                .flatMap(
                        item ->
                                item.message()
                                        .stream()
                )

                /*
                 * Message content
                 */
                .flatMap(
                        message ->
                                message.content()
                                        .stream()
                )

                /*
                 * Structured output
                 */
                .flatMap(
                        content ->
                                content.outputText()
                                        .stream()
                )

                /*
                 * Para nuestro contrato esperamos exactamente
                 * un objeto estructurado.
                 */
                .findFirst()
                .orElseThrow(() ->
                        new OpenAiStrategicAIException(
                                "OpenAI no devolvió contenido estratégico estructurado"
                        )
                );
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    message
            );
        }

        return value.trim();
    }
}