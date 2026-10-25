package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions
        .assertThat;
import static org.assertj.core.api.Assertions
        .assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StoreSettingsRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("dev")
class ProvisioningRollbackIntegrationTest {

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreSettingsRepository
            storeSettingsRepository;

    @Autowired
    private SubscriptionRepository
            subscriptionRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAccountActivationTokenRepository
            activationTokenRepository;

    @MockitoBean
    private AdminAccountActivationService
            activationService;

    @Test
    void failureAfterTenantPersistenceMustRollbackWholeProvisioning() {

        long suffix =
                System.currentTimeMillis();

        String domain =
                "rollback-it-" + suffix;

        String finalDomain =
                domain
                + ".web-empresarial.com";

        String email =
                "rollback-it-"
                + suffix
                + "@example.test";

        String stripeSubscriptionId =
                "sub_rollback_"
                + suffix;

        String stripeCustomerId =
                "cus_rollback_"
                + suffix;

        long storesBefore =
                storeRepository.count();

        long settingsBefore =
                storeSettingsRepository.count();

        long subscriptionsBefore =
                subscriptionRepository.count();

        long adminsBefore =
                adminUserRepository.count();

        long tokensBefore =
                activationTokenRepository.count();

        /*
         * createToken() ocurre después de:
         *
         * - Store
         * - StoreSettings
         * - Subscription
         * - STORE_OWNER
         *
         * Forzamos flush de todo el persistence context
         * antes del fallo para demostrar rollback DB-real,
         * no únicamente descarte de entidades pendientes.
         */
        doAnswer(invocation -> {

            storeRepository.flush();

            throw new IllegalStateException(
                    "forced activation token failure"
            );

        }).when(activationService)
                .createToken(
                        any(AdminUser.class)
                );

        assertThatThrownBy(() ->
                provisioningService
                        .provisionStoreFromCheckout(
                                "Rollback IT",
                                domain,
                                "Rollback Owner",
                                email,
                                StorePlan.PRO,
                                stripeCustomerId,
                                stripeSubscriptionId,
                                "price_rollback"
                        )
        )
        .isInstanceOf(
                IllegalStateException.class
        )
        .hasMessage(
                "forced activation token failure"
        );

        verify(activationService)
                .createToken(
                        any(AdminUser.class)
                );

        /*
         * Ninguna parte del tenant puede sobrevivir.
         */
        assertThat(
                storeRepository
                        .findByDominio(finalDomain)
        ).isEmpty();

        assertThat(
                subscriptionRepository
                        .findByStripeSubscriptionId(
                                stripeSubscriptionId
                        )
        ).isEmpty();

        assertThat(
                adminUserRepository
                        .findByEmail(email)
        ).isEmpty();

        /*
         * También verificamos que las tablas completas
         * conservan exactamente sus cardinalidades.
         */
        assertThat(storeRepository.count())
                .isEqualTo(storesBefore);

        assertThat(storeSettingsRepository.count())
                .isEqualTo(settingsBefore);

        assertThat(subscriptionRepository.count())
                .isEqualTo(subscriptionsBefore);

        assertThat(adminUserRepository.count())
                .isEqualTo(adminsBefore);

        assertThat(activationTokenRepository.count())
                .isEqualTo(tokensBefore);
    }
}
