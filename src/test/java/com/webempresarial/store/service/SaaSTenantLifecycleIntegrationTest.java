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

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

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
    @Test
    void activatedTenantAdminShouldLoginAndAccessOwnSettings()
            throws Exception {

        String domain =
                "e2e-admin-login";

        String finalDomain =
                domain + ".web-empresarial.com";

        String email =
                "owner-e2e-admin-login@webempresarial.test";

        provisioningService.provisionStoreFromCheckout(
                "E2E Admin Login Store",
                domain,
                "E2E Admin Owner",
                email,
                StorePlan.PRO,
                "cus_e2e_admin_login",
                "sub_e2e_admin_login",
                "price_e2e_admin_login"
        );

        AdminUser admin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        AdminAccountActivationToken token =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                admin.getId()
                        )
                        .orElseThrow();

        String password =
                "StrongAdminLogin123!";

        activationService.activate(
                token.getToken(),
                password
        );

        /*
         * Simula la frontera real entre:
         *
         * webhook/provisioning -> request posterior de login/admin.
         *
         * El provisioning y esta prueba comparten una transacción de test;
         * sin limpiar el persistence context, Store puede conservar en
         * memoria el lado inverso subscription == null aunque la relación
         * ya esté persistida correctamente.
         */
        entityManager.flush();
        entityManager.clear();

        MvcResult loginResult =
                mockMvc.perform(
                        post("/admin/login")
                                .with(csrf())
                                .param(
                                        "username",
                                        email
                                )
                                .param(
                                        "password",
                                        password
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andReturn();

        String redirect =
                loginResult
                        .getResponse()
                        .getRedirectedUrl();

        assertThat(redirect)
                .isNotNull()
                .contains(finalDomain)
                .endsWith("/admin/dashboard");

        MockHttpSession session =
                (MockHttpSession)
                        loginResult
                                .getRequest()
                                .getSession(false);

        assertThat(session)
                .isNotNull();

        mockMvc.perform(
                get("/admin/store/settings")
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/store/settings"
                )
        );

        /*
         * El propietario personaliza su tenant utilizando
         * la misma sesión autenticada.
         */
        mockMvc.perform(
                post("/admin/store/settings")
                        .session(session)
                        .header(
                                "X-Forwarded-Host",
                                finalDomain
                        )
                        .with(csrf())
                        .param(
                                "companyEmail",
                                "ventas-e2e@webempresarial.test"
                        )
                        .param(
                                "contactName",
                                "E2E Admin Owner"
                        )
                        .param(
                                "currency",
                                "MXN"
                        )
                        .param(
                                "primaryColor",
                                "#123456"
                        )
                        .param(
                                "secondaryColor",
                                "#654321"
                        )
                        .param(
                                "accentColor",
                                "#ABCDEF"
                        )
                        .param(
                                "fontFamily",
                                "Inter"
                        )
                        .param(
                                "slogan",
                                "E2E-STOREFRONT-SLOGAN"
                        )
                        .param(
                                "heroTitle",
                                "E2E-STOREFRONT-HERO"
                        )
                        .param(
                                "heroSubtitle",
                                "E2E-STOREFRONT-SUBTITLE"
                        )
                        .param(
                                "footerText",
                                "E2E-STOREFRONT-FOOTER"
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        );

        /*
         * Nueva frontera de request/contexto:
         * comprobamos lo realmente persistido.
         */
        entityManager.flush();
        entityManager.clear();

        var persistedSettings =
                storeSettingsRepository
                        .findByStoreId(
                                provisionedStoreId(finalDomain)
                        )
                        .orElseThrow();

        assertThat(persistedSettings.getPrimaryColor())
                .isEqualTo("#123456");

        assertThat(persistedSettings.getSlogan())
                .isEqualTo(
                        "E2E-STOREFRONT-SLOGAN"
                );

        assertThat(persistedSettings.getHeroTitle())
                .isEqualTo(
                        "E2E-STOREFRONT-HERO"
                );

        /*
         * Finalmente se solicita el storefront real del tenant.
         */
        MvcResult storefrontResult =
                mockMvc.perform(
                        get("/")
                                .header(
                                        "X-Forwarded-Host",
                                        finalDomain
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn();

        String storefrontHtml =
                storefrontResult
                        .getResponse()
                        .getContentAsString();

        assertThat(storefrontHtml)
                .contains(
                        "E2E-STOREFRONT-SLOGAN",
                        "E2E-STOREFRONT-HERO",
                        "#123456"
                );
    }

    private Long provisionedStoreId(
            String domain
    ) {

        return storeRepository
                .findByDominio(domain)
                .orElseThrow()
                .getId();
    }

}
