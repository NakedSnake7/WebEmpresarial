package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.webempresarial.store.event.AdminAccountCreatedEvent;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StoreSettingsRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Import(
        ProvisioningRollbackIntegrationTest
                .FailingProvisioningEventListener.class
)
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

    @Autowired
    private FailingProvisioningEventListener
            failingListener;

    @Test
    void failureAfterActivationTokenPersistenceMustRollbackWholeProvisioning() {

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
         * El listener de prueba es síncrono.
         *
         * ProvisioningService publica AdminAccountCreatedEvent
         * únicamente después de que:
         *
         * - Store
         * - StoreSettings
         * - Subscription
         * - STORE_OWNER
         * - AdminAccountActivationToken
         *
         * ya fueron creados.
         *
         * El listener fuerza EntityManager.flush(), verifica que
         * el token real existe dentro de la transacción y sólo
         * entonces lanza la excepción.
         */
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
                "forced post-token event failure"
        );

        String observedToken =
                failingListener.getObservedToken();

        /*
         * Demuestra que alcanzamos realmente el punto posterior
         * a la creación del activation token.
         */
        assertThat(observedToken)
                .isNotBlank();

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
         * El token fue observado como persistido dentro de la
         * transacción y debe desaparecer después del rollback.
         */
        assertThat(
                activationTokenRepository
                        .findByToken(observedToken)
        ).isEmpty();

        /*
         * Las tablas completas deben conservar exactamente
         * sus cardinalidades anteriores.
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

    static class FailingProvisioningEventListener {

        private final EntityManager entityManager;

        private final AdminAccountActivationTokenRepository
                activationTokenRepository;

        private String observedToken;

        FailingProvisioningEventListener(
                EntityManager entityManager,
                AdminAccountActivationTokenRepository
                        activationTokenRepository
        ) {
            this.entityManager = entityManager;
            this.activationTokenRepository =
                    activationTokenRepository;
        }

        @EventListener
        public void failAfterActivationTokenPersistence(
                AdminAccountCreatedEvent event
        ) {

            /*
             * Fuerza INSERT/UPDATE reales antes del fallo.
             *
             * El flush incluye Store, Settings, Subscription,
             * AdminUser y ActivationToken.
             */
            entityManager.flush();

            /*
             * No basta con confiar en que createToken() fue llamado:
             * verificamos que el token es consultable dentro de la
             * misma transacción antes de provocar el rollback.
             */
            assertThat(
                    activationTokenRepository
                            .findByToken(
                                    event.activationToken()
                            )
            ).isPresent();

            observedToken =
                    event.activationToken();

            throw new IllegalStateException(
                    "forced post-token event failure"
            );
        }

        String getObservedToken() {
            return observedToken;
        }
    }
}
