package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@Import(
        SubscriptionEnforcementIntegrationTest
                .BillingRecoveryProbeController.class
)
class SubscriptionEnforcementIntegrationTest {

    private static final String DOMAIN =
            "g08-subscription-enforcement";

    private static final String FINAL_DOMAIN =
            DOMAIN + ".web-empresarial.com";

    private static final String OWNER_EMAIL =
            "owner-g08-subscription@webempresarial.test";

    private static final String OWNER_PASSWORD =
            "StrongG08Password123!";

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private AdminAccountActivationService activationService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAccountActivationTokenRepository tokenRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession ownerSession;

    @BeforeEach
    void provisionActivateAndLoginTenant()
            throws Exception {

        provisioningService.provisionStoreFromCheckout(
                "G08 Subscription Store",
                DOMAIN,
                "G08 Owner",
                OWNER_EMAIL,
                StorePlan.PRO,
                "cus_g08_subscription",
                "sub_g08_subscription",
                "price_g08_subscription"
        );

        AdminUser owner =
                adminUserRepository
                        .findByEmail(OWNER_EMAIL)
                        .orElseThrow();

        AdminAccountActivationToken token =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                owner.getId()
                        )
                        .orElseThrow();

        /*
         * Utilizamos exactamente el mismo flujo de activación
         * que utilizaría el STORE_OWNER real.
         */
        activationService.activate(
                token.getToken(),
                OWNER_PASSWORD
        );

        /*
         * Simula la frontera entre provisioning/activation
         * y una petición HTTP posterior.
         */
        entityManager.flush();
        entityManager.clear();

        /*
         * Login real de Spring Security.
         *
         * Esto garantiza que authorities, enabled y principal
         * provengan de AdminUserDetailsService y no de un
         * principal sintético creado por el test.
         */
        MvcResult loginResult =
                mockMvc.perform(
                        post("/admin/login")
                                .with(csrf())
                                .param(
                                        "username",
                                        OWNER_EMAIL
                                )
                                .param(
                                        "password",
                                        OWNER_PASSWORD
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andReturn();

        ownerSession =
                (MockHttpSession)
                        loginResult
                                .getRequest()
                                .getSession(false);

        assertThat(ownerSession)
                .isNotNull();
    }

    @Test
    void shouldEnforceSubscriptionStatusesAndKeepBillingRecoveryAccessible()
            throws Exception {

        /*
         * ACTIVE:
         * tenant operativo.
         */
        setStatus(SubscriptionStatus.ACTIVE);

        assertDashboardAllowed();

        /*
         * TRIAL:
         * tenant operativo durante el periodo permitido.
         */
        setStatus(SubscriptionStatus.TRIAL);

        assertDashboardAllowed();

        /*
         * PAST_DUE:
         * la operación normal queda suspendida.
         */
        setStatus(SubscriptionStatus.PAST_DUE);

        assertDashboardBlocked();

        /*
         * Sin embargo, billing permanece accesible para
         * permitir recuperación de la suscripción.
         */
        mockMvc.perform(
                get(
                        "/admin/billing/"
                        + "subscription-enforcement-probe"
                )
                        .session(ownerSession)
                        .header(
                                "X-Forwarded-Host",
                                FINAL_DOMAIN
                        )
        )
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));

        /*
         * CANCELLED:
         * acceso operativo suspendido.
         */
        setStatus(SubscriptionStatus.CANCELLED);

        assertDashboardBlocked();

        /*
         * EXPIRED:
         * acceso operativo suspendido.
         */
        setStatus(SubscriptionStatus.EXPIRED);

        assertDashboardBlocked();
    }

    @Test
    void superAdminShouldBypassSuspendedTenantSubscription()
            throws Exception {

        setStatus(SubscriptionStatus.PAST_DUE);

        mockMvc.perform(
                get("/admin/dashboard")
                        .with(superAdmin())
                        .header(
                                "X-Forwarded-Host",
                                FINAL_DOMAIN
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                view().name(
                        "admin/dashboard"
                )
        );
    }

    private void assertDashboardAllowed()
            throws Exception {

        mockMvc.perform(
                get("/admin/dashboard")
                        .session(ownerSession)
                        .header(
                                "X-Forwarded-Host",
                                FINAL_DOMAIN
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                view().name(
                        "admin/dashboard"
                )
        );
    }

    private void assertDashboardBlocked()
            throws Exception {

        mockMvc.perform(
                get("/admin/dashboard")
                        .session(ownerSession)
                        .header(
                                "X-Forwarded-Host",
                                FINAL_DOMAIN
                        )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrl(
                        "/admin/billing?expired"
                )
        );
    }

    private void setStatus(
            SubscriptionStatus status
    ) {

        Store store =
                storeRepository
                        .findByDominio(FINAL_DOMAIN)
                        .orElseThrow();

        Subscription subscription =
                subscriptionRepository
                        .findByStoreId(
                                store.getId()
                        )
                        .orElseThrow();

        subscription.setStatus(status);

        subscriptionRepository.save(subscription);

        /*
         * La petición siguiente debe observar el estado
         * persistido, no una entidad cacheada previamente.
         */
        entityManager.flush();
        entityManager.clear();
    }

    private RequestPostProcessor superAdmin() {

        return user(
                "superadmin-g08@webempresarial.test"
        )
        .authorities(
                new SimpleGrantedAuthority(
                        "ROLE_SUPER_ADMIN"
                ),
                new SimpleGrantedAuthority(
                        AdminPermission
                                .DASHBOARD_VIEW
                                .authority()
                )
        );
    }

    @RestController
    static class BillingRecoveryProbeController {

        @GetMapping(
                "/admin/billing/"
                + "subscription-enforcement-probe"
        )
        String probe() {
            return "OK";
        }
    }
}
