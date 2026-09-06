package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIProviderException;
import com.webempresarial.store.digitaltransformation.infrastructure.ai.StrategicAIProviderFailure;

public class OpenAiStrategicAIException
        extends StrategicAIProviderException {

    public OpenAiStrategicAIException(
            StrategicAIProviderFailure failure,
            String message
    ) {
        super(
                failure,
                message
        );
    }

    public OpenAiStrategicAIException(
            StrategicAIProviderFailure failure,
            String message,
            Throwable cause
    ) {
        super(
                failure,
                message,
                cause
        );
    }

    /*
     * Compatibilidad temporal con llamadas existentes.
     */
    public OpenAiStrategicAIException(
            String message
    ) {
        this(
                StrategicAIProviderFailure.INVALID_RESPONSE,
                message
        );
    }

    public OpenAiStrategicAIException(
            String message,
            Throwable cause
    ) {
        this(
                StrategicAIProviderFailure.UNKNOWN,
                message,
                cause
        );
    }
}