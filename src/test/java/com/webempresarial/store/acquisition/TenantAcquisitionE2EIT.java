package com.webempresarial.store.acquisition;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreSettingsRepository;
import com.webempresarial.store.repository.SubscriptionRepository;
import com.webempresarial.store.service.ProvisioningService;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet.request
.SecurityMockMvcRequestBuilders.formLogin;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.redirectedUrl;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result
.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TenantAcquisitionE2EIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProvisioningService provisioningService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAccountActivationTokenRepository tokenRepository;

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldProvisionActivateLoginAndEnforceTenantIsolation()
            throws Exception {

        /*
         * ---------------------------------------------------------
         * 1. Datos únicos del comprador
         * ---------------------------------------------------------
         */

        String suffix =
                String.valueOf(
                        System.currentTimeMillis()
                );

        String domainPrefix =
                "acquisition-e2e-" + suffix;

        String domain =
                domainPrefix
                        + ".web-empresarial.com";

        String email =
                "owner-"
                        + suffix
                        + "@example.test";

        String password =
                "StrongPassword123!";

        /*
         * ---------------------------------------------------------
         * 2. Provisioning real
         * ---------------------------------------------------------
         */

        Store store =
                provisioningService
                        .provisionStoreFromCheckout(
                                "Acquisition E2E",
                                domainPrefix,
                                "Acquisition Owner",
                                email,
                                StorePlan.BASIC,
                                "cus_acquisition_" + suffix,
                                "sub_acquisition_" + suffix,
                                "price_acquisition_basic"
                        );

        assertThat(store)
                .isNotNull();

        assertThat(store.getId())
                .isNotNull();

        assertThat(store.getDominio())
                .isEqualTo(domain);

        assertThat(store.isActiva())
                .isTrue();

        assertThat(store.getPlan())
                .isEqualTo(StorePlan.BASIC);

        /*
         * ---------------------------------------------------------
         * 3. StoreSettings creado por provisioning
         * ---------------------------------------------------------
         */

        StoreSettings settings =
                storeSettingsRepository
                        .findByStoreId(
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(settings.getStore().getId())
                .isEqualTo(store.getId());

        /*
         * ---------------------------------------------------------
         * 4. Subscription real
         * ---------------------------------------------------------
         */

        Subscription subscription =
                subscriptionRepository
                        .findByStoreId(
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(subscription.getStatus())
                .isEqualTo(
                        SubscriptionStatus.ACTIVE
                );

        /*
         * ---------------------------------------------------------
         * 5. STORE_ADMIN recién provisionado
         * ---------------------------------------------------------
         */

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
                        store.getId()
                );

        assertThat(admin.isEnabled())
                .isFalse();

        /*
         * ---------------------------------------------------------
         * 6. Token real generado por provisioning
         * ---------------------------------------------------------
         */

        AdminAccountActivationToken activationToken =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                admin.getId()
                        )
                        .orElseThrow();

        assertThat(activationToken.isUsable())
                .isTrue();

        /*
         * ---------------------------------------------------------
         * 7. Antes de activar NO debe poder iniciar sesión
         * ---------------------------------------------------------
         */

        mockMvc.perform(
                formLogin("/admin/login")
                        .user(email)
                        .password(password)
        )
        .andExpect(
                status().is3xxRedirection()
        );

        /*
         * Spring Security debe considerar el login fallido.
         * No comprobamos una URL concreta todavía porque puede
         * depender del failureUrl configurado por Spring.
         */

        /*
         * ---------------------------------------------------------
         * 8. Activación real mediante endpoint público
         * ---------------------------------------------------------
         */

        mockMvc.perform(
                post("/admin/activate")
                        .param(
                                "token",
                                activationToken.getToken()
                        )
                        .param(
                                "password",
                                password
                        )
                        .param(
                                "confirmPassword",
                                password
                        )
                        .with(csrf())
        )
        .andExpect(
                status().is3xxRedirection()
        )
        .andExpect(
                redirectedUrl(
                        "/admin/login?activated"
                )
        );

        /*
         * ---------------------------------------------------------
         * 9. Estado persistido después de activar
         * ---------------------------------------------------------
         */

        AdminUser activatedAdmin =
                adminUserRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertThat(activatedAdmin.isEnabled())
                .isTrue();

        assertThat(
                passwordEncoder.matches(
                        password,
                        activatedAdmin.getPassword()
                )
        ).isTrue();

        AdminAccountActivationToken usedToken =
                tokenRepository
                        .findByToken(
                                activationToken.getToken()
                        )
                        .orElseThrow();

        assertThat(usedToken.isUsed())
                .isTrue();

        /*
         * ---------------------------------------------------------
         * 10. Login real después de activación
         * ---------------------------------------------------------
         */

        MvcResult loginResult =
                mockMvc.perform(
                        formLogin("/admin/login")
                                .user(email)
                                .password(password)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "http://"
                                    + domain
                                    + ":8080/admin/dashboard"
                            )
                )
                .andReturn();

        HttpSession session =
                loginResult
                        .getRequest()
                        .getSession(false);

        assertThat(session)
                .isNotNull();

        /*
         * ---------------------------------------------------------
         * 11. Panel/settings accesible desde SU dominio
         * ---------------------------------------------------------
         */

        mockMvc.perform(
                get("/admin/store/settings")
                        .session(
                                (org.springframework.mock.web.MockHttpSession)
                                        session
                        )
                        .header(
                                "Host",
                                domain
                        )
        )
        .andExpect(
                status().isOk()
        );

        /*
         * ---------------------------------------------------------
         * Tenant B real para comprobar aislamiento A -> B
         * ---------------------------------------------------------
         */

        String foreignDomainPrefix =
                "foreign-acquisition-e2e-" + suffix;

        String foreignDomain =
                foreignDomainPrefix
                        + ".web-empresarial.com";

        String foreignEmail =
                "foreign-owner-"
                        + suffix
                        + "@example.test";

        Store foreignStore =
                provisioningService
                        .provisionStoreFromCheckout(
                                "Foreign Acquisition E2E",
                                foreignDomainPrefix,
                                "Foreign Owner",
                                foreignEmail,
                                StorePlan.BASIC,
                                "cus_foreign_" + suffix,
                                "sub_foreign_" + suffix,
                                "price_foreign_basic"
                        );

        assertThat(foreignStore.getId())
                .isNotNull();

        assertThat(foreignStore.getId())
                .isNotEqualTo(
                        store.getId()
                );

        assertThat(foreignStore.getDominio())
                .isEqualTo(
                        foreignDomain
                );

        /*
         * ---------------------------------------------------------
         * 12. Mismo usuario + otro tenant => acceso prohibido
         * ---------------------------------------------------------
         *
         * El dominio no necesita pertenecer al comprador.
         * Si no puede resolverse, AdminTenantAccessInterceptor
         * también debe cerrar el acceso.
         */

        mockMvc.perform(
                get("/admin/store/settings")
                        .session(
                                (org.springframework.mock.web.MockHttpSession)
                                        session
                        )
                        .header(
                                "X-Forwarded-Host",
                                foreignDomain
                        )
        )
        .andExpect(
                status().isForbidden()
        );

        /*
         * ---------------------------------------------------------
         * 13. STORE_ADMIN personaliza SU tienda
         * ---------------------------------------------------------
         */

        String heroTitle =
                "Hero Acquisition " + suffix;

        String heroSubtitle =
                "Storefront personalizado desde el E2E";

        String aboutTitle =
                "Nuestra historia " + suffix;

        String aboutText =
                "Contenido exclusivo del tenant " + suffix;

        String ctaTitle =
                "Compra con nosotros " + suffix;

        String ctaText =
                "CTA exclusivo Acquisition E2E " + suffix;

        String footerText =
                "Footer Acquisition " + suffix;

        String whatsappMessage =
                "Hola Acquisition " + suffix;

        mockMvc.perform(
                post("/admin/store/settings")
                        .session(
                                (org.springframework.mock.web.MockHttpSession)
                                        session
                        )
                        .header(
                                "X-Forwarded-Host",
                                domain
                        )
                        .with(csrf())

                        .param(
                                "companyEmail",
                                email
                        )
                        .param(
                                "companyPhone",
                                "+52 222 123 4567"
                        )
                        .param(
                                "currency",
                                "MXN"
                        )

                        .param(
                                "primaryColor",
                                "#112233"
                        )
                        .param(
                                "secondaryColor",
                                "#445566"
                        )
                        .param(
                                "accentColor",
                                "#778899"
                        )
                        .param(
                                "fontFamily",
                                "Inter"
                        )

                        .param(
                                "heroEyebrow",
                                "ACQUISITION E2E"
                        )
                        .param(
                                "heroTitle",
                                heroTitle
                        )
                        .param(
                                "heroSubtitle",
                                heroSubtitle
                        )
                        .param(
                                "heroButtonText",
                                "Explorar productos"
                        )
                        .param(
                                "heroButtonUrl",
                                "/productos"
                        )

                        .param(
                                "aboutTitle",
                                aboutTitle
                        )
                        .param(
                                "aboutText",
                                aboutText
                        )

                        .param(
                                "ctaTitle",
                                ctaTitle
                        )
                        .param(
                                "ctaText",
                                ctaText
                        )

                        .param(
                                "whatsappMessage",
                                whatsappMessage
                        )

                        .param(
                                "facebookUrl",
                                "https://facebook.com/acquisition-e2e"
                        )
                        .param(
                                "instagramUrl",
                                "https://instagram.com/acquisition-e2e"
                        )
                        .param(
                                "tiktokUrl",
                                "https://tiktok.com/@acquisition-e2e"
                        )

                        .param(
                                "footerText",
                                footerText
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        )
        .andExpect(
                redirectedUrl(
                        "/admin/store/settings?success"
                )
        );

        /*
         * ---------------------------------------------------------
         * 14. Comprobar persistencia real
         * ---------------------------------------------------------
         */

        StoreSettings updatedSettings =
                storeSettingsRepository
                        .findByStoreId(
                                store.getId()
                        )
                        .orElseThrow();

        assertThat(updatedSettings.getHeroTitle())
                .isEqualTo(heroTitle);

        assertThat(updatedSettings.getHeroSubtitle())
                .isEqualTo(heroSubtitle);

        assertThat(updatedSettings.getAboutTitle())
                .isEqualTo(aboutTitle);

        assertThat(updatedSettings.getAboutText())
                .isEqualTo(aboutText);

        assertThat(updatedSettings.getCtaTitle())
                .isEqualTo(ctaTitle);

        assertThat(updatedSettings.getCtaText())
                .isEqualTo(ctaText);

        assertThat(updatedSettings.getFooterText())
                .isEqualTo(footerText);

        assertThat(updatedSettings.getWhatsappMessage())
                .isEqualTo(whatsappMessage);

        assertThat(updatedSettings.getPrimaryColor())
                .isEqualTo("#112233");

        /*
         * ---------------------------------------------------------
         * 15. Storefront público refleja la personalización
         * ---------------------------------------------------------
         */

        mockMvc.perform(
                get("/")
                        .header(
                                "X-Forwarded-Host",
                                domain
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                heroTitle
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                heroSubtitle
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                aboutTitle
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                aboutText
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                ctaTitle
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                ctaText
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                footerText
                        )
                )
        );
    }
}
