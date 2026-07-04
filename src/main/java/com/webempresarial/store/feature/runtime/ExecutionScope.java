package com.webempresarial.store.feature.runtime;

public record ExecutionScope(
        ExecutionContext context
) {

    public static ExecutionScope root() {
        return new ExecutionScope(new ExecutionContext());
    }

    public static ExecutionScope of(ExecutionContext context) {
        return new ExecutionScope(context);
    }

    public ExecutionScope child() {
        return new ExecutionScope(
                ExecutionContext.childOf(context)
        );
    }
}