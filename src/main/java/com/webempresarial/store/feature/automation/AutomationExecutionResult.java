package com.webempresarial.store.feature.automation;

public record AutomationExecutionResult(
        boolean success,
        String message
) {

    public static AutomationExecutionResult success(String message) {
        return new AutomationExecutionResult(true, message);
    }

    public static AutomationExecutionResult failure(String message) {
        return new AutomationExecutionResult(false, message);
    }
}