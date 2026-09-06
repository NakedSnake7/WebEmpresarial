package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

public enum StrategicChainCompleteness {

    EMPTY(0),

    FINDING_ONLY(25),

    FINDING_AND_PROBLEM(50),

    THROUGH_OBJECTIVE(75),

    COMPLETE(100),

    PARTIAL_NON_CANONICAL(0);

    private final int percentage;

    StrategicChainCompleteness(
            int percentage
    ) {
        this.percentage = percentage;
    }

    public int percentage() {
        return percentage;
    }

    public boolean isComplete() {
        return this == COMPLETE;
    }
}