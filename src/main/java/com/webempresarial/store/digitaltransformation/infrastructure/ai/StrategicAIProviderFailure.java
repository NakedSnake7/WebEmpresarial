package com.webempresarial.store.digitaltransformation.infrastructure.ai;

public enum StrategicAIProviderFailure {

    TIMEOUT,
    RATE_LIMITED,
    AUTHENTICATION_FAILED,
    PROVIDER_UNAVAILABLE,
    INVALID_RESPONSE,
    MALFORMED_STRUCTURED_OUTPUT,
    SAFETY_REFUSAL,
    UNKNOWN
}