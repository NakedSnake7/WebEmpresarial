package com.webempresarial.store.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class StoreSettingsShippingPolicyTest {

    @Test
    void shouldPreserveCurrentShippingBehaviorByDefault()
            throws Exception {

        StoreSettings settings = new StoreSettings();

        Method enabledGetter =
                StoreSettings.class.getMethod(
                        "isFreeShippingEnabled"
                );

        Method thresholdGetter =
                StoreSettings.class.getMethod(
                        "getFreeShippingThreshold"
                );

        Method shippingCostGetter =
                StoreSettings.class.getMethod(
                        "getShippingCost"
                );

        boolean enabled =
                (boolean) enabledGetter.invoke(settings);

        BigDecimal threshold =
                (BigDecimal) thresholdGetter.invoke(settings);

        BigDecimal shippingCost =
                (BigDecimal) shippingCostGetter.invoke(settings);

        assertThat(enabled)
                .isTrue();

        assertThat(threshold)
                .isEqualByComparingTo("1250.00");

        assertThat(shippingCost)
                .isEqualByComparingTo("120.00");
    }
}
