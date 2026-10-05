package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.StoreSettingsRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SaaSTenantLifecycleIntegrationTest {

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private AdminAccountActivationService activationService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAccountActivationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldProvisionAndActivateNewSaaSTenant() {

        String domain =
                "e2e-lifecycle";

        String finalDomain =
                domain + ".web-empresarial.com";

        String email =
                "owner-e2e-lifecycle@webempresarial.test";

        Store provisionedStore =
                provisioningService.provisionStoreFromCheckout(
                        "E2E Lifecycle Store",
                        domain,
                        "E2E Owner",
                        email,
                        StorePlan.PRO,
                        "cus_e2e_lifecycle",
                        "sub_e2e_lifecycle",
                        "price_e2e_pro"
                );

        assertThat(provisionedStore.getId())
                .isNotNull();

        Store persistedStore =
                storeRepository
                        .findByDominio(finalDomain)
                        .orElseThrow();

        assertThat(persistedStore.getId())
                .isEqualTo(provisionedStore.getId());

        assertThat(persistedStore.isActiva())
                .isTrue();

        assertThat(persistedStore.getPlan())
                .isEqualTo(StorePlan.PRO);

        assertThat(
                storeSettingsRepository.findByStoreId(
                        persistedStore.getId()
                )
        ).isPresent();

        Subscription subscription =
                subscriptionRepository
                        .findByStoreId(
                                persistedStore.getId()
                        )
                        .orElseThrow();

        assertThat(subscription.getStatus())
                .isEqualTo(
                        SubscriptionStatus.ACTIVE
                );

        assertThat(subscription.getPlan())
                .isEqualTo(StorePlan.PRO);

        assertThat(
                subscription.getStripeCustomerId()
        ).isEqualTo(
                "cus_e2e_lifecycle"
        );

        assertThat(
                subscription.getStripeSubscriptionId()
        ).isEqualTo(
                "sub_e2e_lifecycle"
        );

        assertThat(
                subscription.getStripePriceId()
        ).isEqualTo(
                "price_e2e_pro"
        );

        AdminUser admin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertThat(admin.getRole())
                .isEqualTo(
                        AdminRole.STORE_ADMIN
                );

        assertThat(admin.getStore().getId())
                .isEqualTo(
                        persistedStore.getId()
                );

        assertThat(admin.isEnabled())
                .isFalse();

        AdminAccountActivationToken token =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                admin.getId()
                        )
                        .orElseThrow();

        assertThat(token.getToken())
                .isNotBlank();

        assertThat(token.isUsed())
                .isFalse();

        String newPassword =
                "StrongE2EPassword123!";

        activationService.activate(
                token.getToken(),
                newPassword
        );

        AdminUser activatedAdmin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertThat(activatedAdmin.isEnabled())
                .isTrue();

        assertThat(
                passwordEncoder.matches(
                        newPassword,
                        activatedAdmin.getPassword()
                )
        ).isTrue();

        AdminAccountActivationToken usedToken =
                tokenRepository
                        .findByToken(
                                token.getToken()
                        )
                        .orElseThrow();

        assertThat(usedToken.isUsed())
                .isTrue();
    }
}
