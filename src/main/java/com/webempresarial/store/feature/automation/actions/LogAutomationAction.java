package com.webempresarial.store.feature.automation.actions;

import com.webempresarial.store.feature.automation.AutomationAction;
import com.webempresarial.store.feature.automation.AutomationContext;
import com.webempresarial.store.feature.automation.AutomationExecutionResult;
import com.webempresarial.store.feature.runtime.ExecutionTracer;

import org.springframework.stereotype.Component;

@Component
public class LogAutomationAction implements AutomationAction {

    private final ExecutionTracer executionTracer;

    public LogAutomationAction(ExecutionTracer executionTracer) {
        this.executionTracer = executionTracer;
    }

    @Override
    public AutomationExecutionResult execute(AutomationContext context) {
        String message = "Trigger ejecutado: " + context.trigger();

        executionTracer
        .span(
                context.scope(),
                "Log interno",
                "LogAutomationAction"
        )
        .run(() -> System.out.println("[Automation] " + message));

        return AutomationExecutionResult.success(message);
    }
}