package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

public interface OpenAiStructuredResponseGateway {

    OpenAiStrategicOutput generate(
            String systemInstruction,
            String input
    );
}