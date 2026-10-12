package com.webempresarial.store.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class StoreSettingsTransferPaymentSettingsTest {

    @Test
    void shouldDisableBankTransferByDefault() throws Exception {
        StoreSettings settings = new StoreSettings();

        Method enabledGetter =
                StoreSettings.class.getMethod(
                        "isTransferEnabled"
                );

        boolean enabled =
                (boolean) enabledGetter.invoke(settings);

        assertThat(enabled).isFalse();
    }

    @Test
    void shouldExposeStoreSpecificTransferAccountData()
            throws Exception {

        StoreSettings settings = new StoreSettings();

        Method bankNameGetter =
                StoreSettings.class.getMethod(
                        "getTransferBankName"
                );

        Method accountHolderGetter =
                StoreSettings.class.getMethod(
                        "getTransferAccountHolder"
                );

        Method clabeGetter =
                StoreSettings.class.getMethod(
                        "getTransferClabe"
                );

        assertThat(
                bankNameGetter.invoke(settings)
        ).isNull();

        assertThat(
                accountHolderGetter.invoke(settings)
        ).isNull();

        assertThat(
                clabeGetter.invoke(settings)
        ).isNull();
    }
}
