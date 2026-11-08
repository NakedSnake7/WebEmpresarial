package com.webempresarial.store.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PilotCohortTest {

    @Test
    void pilotPricesMustBeDefinedByBackend() {
        assertEquals(
                new BigDecimal("990.00"),
                PilotCohort.A.initialAmountMxn()
        );

        assertEquals(
                new BigDecimal("1490.00"),
                PilotCohort.B.initialAmountMxn()
        );

        assertEquals(
                new BigDecimal("790.00"),
                PilotCohort.RENEWAL_AMOUNT_MXN
        );
    }
}
