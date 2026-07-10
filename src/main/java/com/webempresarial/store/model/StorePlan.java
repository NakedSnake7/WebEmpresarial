package com.webempresarial.store.model;

public enum StorePlan {

    BASIC(1),
    PRO(2),
    PREMIUM(3);

    private final int rank;

    StorePlan(int rank) {
        this.rank = rank;
    }

    public boolean isHigherThan(StorePlan other) {
        return other != null && this.rank > other.rank;
    }

    public boolean isLowerThan(StorePlan other) {
        return other != null && this.rank < other.rank;
    }
}