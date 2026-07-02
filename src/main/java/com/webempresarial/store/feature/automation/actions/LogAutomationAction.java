package com.webempresarial.store.feature.automation.actions;

import com.webempresarial.store.feature.automation.AutomationAction;
import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.automation.AutomationExecutionResult;

import org.springframework.stereotype.Component;

@Component
public class LogAutomationAction implements AutomationAction {

    @Override
    public AutomationExecutionResult execute(AutomationContext context) {
        String message = "Trigger ejecutado: " + context.trigger();

        System.out.println("[Automation] " + message);

        return AutomationExecutionResult.success(message);
    }
}