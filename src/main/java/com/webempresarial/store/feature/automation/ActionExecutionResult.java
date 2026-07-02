package com.webempresarial.store.feature.automation;

public record ActionExecutionResult(

        String action,

        boolean success,

        String message,

        long executionTimeMs

) {}