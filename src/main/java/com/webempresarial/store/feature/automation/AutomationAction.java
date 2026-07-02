package com.webempresarial.store.feature.automation;

public interface AutomationAction {

    AutomationExecutionResult execute(AutomationContext context);
}