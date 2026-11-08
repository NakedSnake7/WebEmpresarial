package com.webempresarial.store.model;

import java.math.BigDecimal;

public enum PilotCohort {

    A("990.00"),
    B("1490.00");

    public static final BigDecimal RENEWAL_AMOUNT_MXN =
            new BigDecimal("790.00");

    private final BigDecimal initialAmountMxn;

    PilotCohort(String amount) {
        this.initialAmountMxn = new BigDecimal(amount);
    }

    public BigDecimal initialAmountMxn() {
        return initialAmountMxn;
    }
}
