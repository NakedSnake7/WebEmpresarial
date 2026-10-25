package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StoreProvisioningServiceTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private StoreDefaultDataService storeDefaultDataService;

    @Mock
    private StoreSettingsService storeSettingsService;

    private StoreProvisioningService service;

    private Store store;

    @BeforeEach
    void setUp() {

        service =
                new StoreProvisioningService(
                        subscriptionService,
                        storeDefaultDataService,
                        storeSettingsService
                );

        store = new Store();
        store.setPlan(StorePlan.PRO);
        store.setDominio("manual-provisioning.test");
    }

    @Test
    void trialShouldCreateTrialSubscription() {

        service.provision(store, "TRIAL");

        verify(subscriptionService)
                .createTrial(store, StorePlan.PRO);

        verify(subscriptionService, never())
                .createInternalSubscription(
                        store,
                        StorePlan.PRO
                );

        verify(storeSettingsService)
                .createDefaults(store);

        verify(storeDefaultDataService)
                .createDefaults(store);
    }

    @Test
    void internalShouldCreateBillingExemptSubscription() {

        service.provision(store, "INTERNAL");

        verify(subscriptionService)
                .createInternalSubscription(
                        store,
                        StorePlan.PRO
                );

        verify(subscriptionService, never())
                .createTrial(store, StorePlan.PRO);

        verify(storeSettingsService)
                .createDefaults(store);

        verify(storeDefaultDataService)
                .createDefaults(store);
    }

    @Test
    void paidShouldFailClosedInsteadOfCreatingTrial() {

        assertThatThrownBy(
                () -> service.provision(store, "PAID")
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
                "checkout SaaS"
        );

        verify(subscriptionService, never())
                .createTrial(store, StorePlan.PRO);

        verify(subscriptionService, never())
                .createInternalSubscription(
                        store,
                        StorePlan.PRO
                );

        /*
         * El provisioning no debe continuar después
         * de detectar un tipo financiero inválido.
         */
        verify(storeSettingsService, never())
                .createDefaults(store);

        verify(storeDefaultDataService, never())
                .createDefaults(store);
    }

    @Test
    void unknownSubscriptionTypeShouldFailClosed() {

        assertThatThrownBy(
                () -> service.provision(
                        store,
                        "UNKNOWN"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
                "Tipo de suscripción no soportado"
        );

        verify(subscriptionService, never())
                .createTrial(store, StorePlan.PRO);

        verify(subscriptionService, never())
                .createInternalSubscription(
                        store,
                        StorePlan.PRO
                );
    }
}
