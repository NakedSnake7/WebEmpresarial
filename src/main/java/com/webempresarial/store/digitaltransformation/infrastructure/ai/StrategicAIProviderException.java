package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import java.util.Objects;

public class StrategicAIProviderException
        extends RuntimeException {

    private final StrategicAIProviderFailure failure;

    public StrategicAIProviderException(
            StrategicAIProviderFailure failure,
            String message
    ) {
        super(message);

        this.failure =
                Objects.requireNonNull(
                        failure,
                        "El tipo de fallo es obligatorio"
                );
    }

    public StrategicAIProviderException(
            StrategicAIProviderFailure failure,
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );

        this.failure =
                Objects.requireNonNull(
                        failure,
                        "El tipo de fallo es obligatorio"
                );
    }

    public StrategicAIProviderFailure getFailure() {
        return failure;
    }
}