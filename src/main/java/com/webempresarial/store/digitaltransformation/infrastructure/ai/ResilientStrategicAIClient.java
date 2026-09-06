package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import java.util.Objects;

public class ResilientStrategicAIClient
        implements StrategicAIClient {

    private final StrategicAIClient delegate;
    private final StrategicAIResiliencePolicy resiliencePolicy;

    public ResilientStrategicAIClient(
            StrategicAIClient delegate,
            StrategicAIResiliencePolicy resiliencePolicy
    ) {
        this.delegate =
                Objects.requireNonNull(
                        delegate,
                        "StrategicAIClient delegate es obligatorio"
                );

        this.resiliencePolicy =
                Objects.requireNonNull(
                        resiliencePolicy,
                        "StrategicAIResiliencePolicy es obligatoria"
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

        int attempt = 1;

        while (true) {
            try {
                StrategicAIResponse response =
                        delegate.generate(
                                request
                        );

                if (response == null) {
                    throw new StrategicAIProviderException(
                            StrategicAIProviderFailure.INVALID_RESPONSE,
                            "El proveedor AI devolvió una respuesta nula"
                    );
                }

                return response;

            } catch (StrategicAIProviderException exception) {

                StrategicAIResilienceDecision decision =
                        resiliencePolicy.evaluate(
                                exception.getFailure(),
                                attempt
                        );

                if (decision
                        == StrategicAIResilienceDecision.FAIL_CLOSED) {

                    throw exception;
                }

                attempt++;
            }
        }
    }
}