package com.webempresarial.store.digitaltransformation.infrastructure.ai;

public interface StrategicAIResiliencePolicy {

    StrategicAIResilienceDecision evaluate(
            StrategicAIProviderFailure failure,
            int attempt
    );
}