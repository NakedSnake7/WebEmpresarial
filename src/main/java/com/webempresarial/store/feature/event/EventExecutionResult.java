package com.webempresarial.store.feature.event;

public record EventExecutionResult(
        String listener,
        boolean success,
        long executionTimeMs,
        String message
) {}