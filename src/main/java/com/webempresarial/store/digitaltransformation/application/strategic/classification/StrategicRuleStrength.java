package com.webempresarial.store.digitaltransformation.application.strategic.classification;

public enum StrategicRuleStrength {

    WEAK(1),
    MODERATE(2),
    STRONG(3),
    DECISIVE(5);

    private final int weight;

    StrategicRuleStrength(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}