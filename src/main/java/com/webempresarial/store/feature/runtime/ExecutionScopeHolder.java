package com.webempresarial.store.feature.runtime;

public final class ExecutionScopeHolder {

    private static final ThreadLocal<ExecutionScope> CURRENT =
            new ThreadLocal<>();

    private ExecutionScopeHolder() {}

    public static ExecutionScope current() {
        return CURRENT.get();
    }

    public static void set(ExecutionScope scope) {
        CURRENT.set(scope);
    }

    public static void clear() {
        CURRENT.remove();
    }
}