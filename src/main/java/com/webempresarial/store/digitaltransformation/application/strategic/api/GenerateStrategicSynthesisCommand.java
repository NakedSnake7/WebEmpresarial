package com.webempresarial.store.digitaltransformation.application.strategic.api;

public interface GenerateStrategicSynthesisCommand {

    GenerateStrategicSynthesisResult generate(
            Long storeId,
            Long projectId,
            Long findingArtifactId
    );
}