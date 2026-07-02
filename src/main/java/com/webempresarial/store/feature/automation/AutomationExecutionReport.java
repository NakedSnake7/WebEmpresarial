package com.webempresarial.store.feature.automation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AutomationExecutionReport {

    private final String trigger;

    private final LocalDateTime startedAt = LocalDateTime.now();

    private final List<ActionExecutionResult> actions =
            new ArrayList<>();

    public AutomationExecutionReport(String trigger) {
        this.trigger = trigger;
    }

    public void add(ActionExecutionResult result) {
        actions.add(result);
    }

    public String trigger() {
        return trigger;
    }

    public LocalDateTime startedAt() {
        return startedAt;
    }

    public List<ActionExecutionResult> actions() {
        return actions;
    }

    public boolean success() {
    	return actions.stream()
    	        .allMatch(result -> result.success());
    }

}