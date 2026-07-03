package com.webempresarial.store.feature.automation;

import com.webempresarial.store.feature.runtime.ExecutionContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AutomationExecutionReport {

    private final String trigger;

    private final ExecutionContext executionContext;

    private final LocalDateTime startedAt = LocalDateTime.now();

    private final List<ActionExecutionResult> actions =
            new ArrayList<>();

    public AutomationExecutionReport(
            String trigger,
            ExecutionContext executionContext
    ) {
        this.trigger = trigger;
        this.executionContext = executionContext;
    }

    public void add(ActionExecutionResult result) {
        actions.add(result);
    }

    public String trigger() {
        return trigger;
    }

    public ExecutionContext executionContext() {
        return executionContext;
    }

    public LocalDateTime startedAt() {
        return startedAt;
    }

    public List<ActionExecutionResult> actions() {
        return actions;
    }

    public boolean success() {
        return actions.stream()
                .allMatch(ActionExecutionResult::success);
    }
}