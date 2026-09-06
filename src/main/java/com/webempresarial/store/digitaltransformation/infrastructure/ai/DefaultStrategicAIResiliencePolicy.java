package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultStrategicAIResiliencePolicy
        implements StrategicAIResiliencePolicy {

    private static final int MAX_ATTEMPTS = 2;

    @Override
    public StrategicAIResilienceDecision evaluate(
            StrategicAIProviderFailure failure,
            int attempt
    ) {
        Objects.requireNonNull(
                failure,
                "El fallo del proveedor es obligatorio"
        );

        if (attempt < 1) {
            throw new IllegalArgumentException(
                    "El número de intento debe ser mayor que cero"
            );
        }

        /*
         * Nunca reintentamos fallos deterministas.
         */
        if (failure
                == StrategicAIProviderFailure.AUTHENTICATION_FAILED
                || failure
                == StrategicAIProviderFailure.INVALID_RESPONSE
                || failure
                == StrategicAIProviderFailure.MALFORMED_STRUCTURED_OUTPUT
                || failure
                == StrategicAIProviderFailure.SAFETY_REFUSAL) {

            return StrategicAIResilienceDecision.FAIL_CLOSED;
        }

        /*
         * Solo errores potencialmente transitorios
         * pueden tener un reintento.
         */
        if ((failure
                == StrategicAIProviderFailure.TIMEOUT
                || failure
                == StrategicAIProviderFailure.RATE_LIMITED
                || failure
                == StrategicAIProviderFailure.PROVIDER_UNAVAILABLE)
                && attempt < MAX_ATTEMPTS) {

            return StrategicAIResilienceDecision.RETRY;
        }

        return StrategicAIResilienceDecision.FAIL_CLOSED;
    }
}