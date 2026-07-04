package com.webempresarial.store.feature.runtime;

import java.util.UUID;

public class ExecutionContext {

    private final String correlationId;
    private final String executionId;
    private final String parentExecutionId;
    private final String spanId;

    public ExecutionContext() {
        this.correlationId = UUID.randomUUID().toString();
        this.executionId = UUID.randomUUID().toString();
        this.parentExecutionId = null;
        this.spanId = UUID.randomUUID().toString();
    }

    public ExecutionContext(String correlationId) {
        this.correlationId = correlationId;
        this.executionId = UUID.randomUUID().toString();
        this.parentExecutionId = null;
        this.spanId = UUID.randomUUID().toString();
    }

    public ExecutionContext(
            String correlationId,
            String parentExecutionId
    ) {
        this.correlationId = correlationId;
        this.executionId = UUID.randomUUID().toString();
        this.parentExecutionId = parentExecutionId;
        this.spanId = UUID.randomUUID().toString();
    }

    public static ExecutionContext childOf(ExecutionContext parent) {
        if (parent == null) {
            return new ExecutionContext();
        }

        return new ExecutionContext(
                parent.correlationId(),
                parent.executionId()
        );
    }

    public ExecutionContext childSpan() {
        return ExecutionContext.childOf(this);
    }
    
    public ExecutionContext childSpan(String name) {
        return ExecutionContext.childOf(this);
    }
    
    public String correlationId() {
        return correlationId;
    }

    public String executionId() {
        return executionId;
    }

    public String parentExecutionId() {
        return parentExecutionId;
    }

    public String spanId() {
        return spanId;
    }
}