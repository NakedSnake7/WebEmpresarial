package com.webempresarial.store.feature.automation;

public interface AutomationCondition {

    boolean matches(AutomationContext context);
}