package com.webempresarial.store.crm.budget;

import java.math.BigDecimal;

public enum BudgetRange {

    UNDEFINED("Sin definir", BigDecimal.ZERO),

    LESS_THAN_5000("Menos de $5,000",
            BigDecimal.valueOf(2500)),

    FROM_5000_TO_10000("$5,000 - $10,000",
            BigDecimal.valueOf(7500)),

    FROM_10000_TO_20000("$10,000 - $20,000",
            BigDecimal.valueOf(15000)),

    FROM_20000_TO_50000("$20,000 - $50,000",
            BigDecimal.valueOf(35000)),

    MORE_THAN_50000("Más de $50,000",
            BigDecimal.valueOf(50000));

    private final String label;
    private final BigDecimal estimatedValue;

    BudgetRange(String label,
                BigDecimal estimatedValue) {
        this.label = label;
        this.estimatedValue = estimatedValue;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }
}