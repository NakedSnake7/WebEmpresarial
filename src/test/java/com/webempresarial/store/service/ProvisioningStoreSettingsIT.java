package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreSettingsRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class ProvisioningStoreSettingsIT {

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Test
    void shouldCreateStoreSettingsDuringProvisioning() {

        long suffix = System.currentTimeMillis();

        Store store =
                provisioningService.provisionStoreFromCheckout(
                        "Provisioning Settings IT",
                        "settings-it-" + suffix,
                        "Settings Owner",
                        "settings-it-" + suffix + "@example.test",
                        StorePlan.BASIC,
                        "cus_settings_" + suffix,
                        "sub_settings_" + suffix,
                        "price_basic"
                );

        assertThat(store.getId())
                .isNotNull();

        var settings =
                storeSettingsRepository
                        .findByStoreId(store.getId());

        assertThat(settings)
                .as(
                        "StoreSettings debe existir inmediatamente "
                        + "después del provisioning"
                )
                .isPresent();

        assertThat(settings.orElseThrow().getStore().getId())
                .isEqualTo(store.getId());

        assertThat(settings.orElseThrow().getCurrency())
                .isEqualTo("MXN");
    }
}