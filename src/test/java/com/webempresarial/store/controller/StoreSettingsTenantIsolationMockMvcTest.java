package com.webempresarial.store.controller;

import com.webempresarial.store.config.FeatureViewAdvice;
import com.webempresarial.store.config.SecurityViewAdvice;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.config.WebMvcConfig;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.ThemeModelAdvice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StoreSettingsController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                FeatureViewAdvice.class,
                                StoreViewAdvice.class,
                                SecurityViewAdvice.class,
                                ThemeModelAdvice.class
                        }
                )
        }
)
@Import({
        WebMvcConfig.class,
        AdminTenantAccessInterceptor.class
})
@AutoConfigureMockMvc(addFilters = false)
class StoreSettingsTenantIsolationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private FeatureAccessService featureAccessService;

    @MockitoBean
    private AdminUserRepository adminUserRepository;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    private Store stride;
    private Store barleyPunch;

    @BeforeEach
    void setUp() throws Exception {

        SecurityContextHolder.clearContext();

        stride = new Store();
        stride.setId(3L);
        stride.setNombre("Stride");
        stride.setDominio("stride.local");

        barleyPunch = new Store();
        barleyPunch.setId(4L);
        barleyPunch.setNombre("Barley Punch");
        barleyPunch.setDominio("barleypunch.local");

        /*
         * Este test se concentra exclusivamente en aislamiento tenant.
         * SubscriptionInterceptor no es el objeto bajo prueba.
         */
        when(subscriptionInterceptor.preHandle(
                any(),
                any(),
                any()
        )).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /*
     * =========================================================
     * STORE ADMIN -> OWN TENANT
     * =========================================================
     */

    @Test
    void storeAdminShouldSaveSettingsForOwnTenant()
            throws Exception {

        authenticate(
                "admin@stride.test",
                "STORE_ADMIN"
        );

        AdminUser admin =
                storeAdmin(
                        "admin@stride.test",
                        stride
                );

        when(adminUserRepository.findByEmail(
                "admin@stride.test"
        )).thenReturn(
                Optional.of(admin)
        );

        /*
         * El host stride.local resuelve Stride.
         */
        when(storeContextService.getCurrentStore(any()))
                .thenReturn(stride);

        com.webempresarial.store.entity.StoreSettings settings =
                new com.webempresarial.store.entity.StoreSettings();

        settings.setStore(stride);
        settings.setCompanyEmail(
                "old@stride.test"
        );

        when(storeSettingsService.getOrCreate(stride))
                .thenReturn(settings);

        when(featureAccessService.canUse(
                stride,
                "WHITE_LABEL"
        )).thenReturn(false);

        mockMvc.perform(
                post("/admin/store/settings")
                        .header(
                                "Host",
                                "stride.local"
                        )
                        .param(
                                "companyEmail",
                                "ventas@stride.test"
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
                                "heroTitle",
                                "STRIDE-HERO-TITLE"
                        )
                        .param(
                                "aboutTitle",
                                "STRIDE-ABOUT-TITLE"
                        )
                        .param(
                                "ctaTitle",
                                "STRIDE-CTA-TITLE"
                        )
                        .param(
                                "whatsappMessage",
                                "STRIDE-WHATSAPP-MESSAGE"
                        )
                        .param(
                                "instagramUrl",
                                "https://instagram.com/stride"
                        )
                        .param(
                                "footerText",
                                "STRIDE-FOOTER-TEXT"
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
        assertThat(settings.getCompanyEmail())
        .isEqualTo("ventas@stride.test");

assertThat(settings.getHeroTitle())
        .isEqualTo("STRIDE-HERO-TITLE");

assertThat(settings.getAboutTitle())
        .isEqualTo("STRIDE-ABOUT-TITLE");

assertThat(settings.getCtaTitle())
        .isEqualTo("STRIDE-CTA-TITLE");

assertThat(settings.getWhatsappMessage())
        .isEqualTo("STRIDE-WHATSAPP-MESSAGE");

assertThat(settings.getInstagramUrl())
        .isEqualTo("https://instagram.com/stride");

assertThat(settings.getFooterText())
        .isEqualTo("STRIDE-FOOTER-TEXT");


        /*
         * El controlador sí llegó a ejecutar la escritura
         * porque identidad y tenant coinciden.
         */
        verify(storeSettingsService)
                .save(settings);

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );
    }

    /*
     * =========================================================
     * STORE ADMIN -> FOREIGN TENANT
     * =========================================================
     */

    @Test
    void storeAdminShouldBeForbiddenFromSavingDifferentTenant()
            throws Exception {

        authenticate(
                "admin@stride.test",
                "STORE_ADMIN"
        );

        AdminUser admin =
                storeAdmin(
                        "admin@stride.test",
                        stride
                );

        when(adminUserRepository.findByEmail(
                "admin@stride.test"
        )).thenReturn(
                Optional.of(admin)
        );

        /*
         * La petición llega mediante el host de Barley Punch.
         *
         * El usuario, sin embargo, pertenece a Stride.
         */
        when(storeContextService.getCurrentStore(any()))
                .thenReturn(barleyPunch);

        mockMvc.perform(
                post("/admin/store/settings")
                        .header(
                                "Host",
                                "barleypunch.local"
                        )
                        .param(
                                "companyEmail",
                                "attack@barleypunch.test"
                        )
                        .param(
                                "primaryColor",
                                "#000000"
                        )
                        .param(
                                "customCss",
                                "body { display:none; }"
                        )
                        .param(
                                "heroTitle",
                                "ATTACKED-HERO"
                        )
                        .param(
                                "aboutText",
                                "ATTACKED-ABOUT"
                        )
                        .param(
                                "instagramUrl",
                                "https://instagram.com/attacker"
                        )
                        .param(
                                "footerText",
                                "ATTACKED-FOOTER"
                        )
        )
        .andExpect(
                status().isForbidden()
        );

        /*
         * Esta es la garantía importante:
         *
         * AdminTenantAccessInterceptor debe detener la request
         * ANTES de que StoreSettingsController pueda obtener
         * o modificar la configuración del tenant B.
         */
        verify(
                storeSettingsService,
                never()
        ).getOrCreate(any());

        verify(
                storeSettingsService,
                never()
        ).save(any());

        verifyNoInteractions(
                featureAccessService
        );

        verifyNoInteractions(
                cloudinaryService
        );

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );
    }

    /*
     * =========================================================
     * SECOND TENANT -> OWN TENANT
     * =========================================================
     */

    @Test
    void secondStoreAdminShouldSaveSettingsForOwnTenant()
            throws Exception {

        authenticate(
                "admin@barleypunch.test",
                "STORE_ADMIN"
        );

        AdminUser admin =
                storeAdmin(
                        "admin@barleypunch.test",
                        barleyPunch
                );

        when(adminUserRepository.findByEmail(
                "admin@barleypunch.test"
        )).thenReturn(
                Optional.of(admin)
        );

        when(storeContextService.getCurrentStore(any()))
                .thenReturn(barleyPunch);

        com.webempresarial.store.entity.StoreSettings settings =
                new com.webempresarial.store.entity.StoreSettings();

        settings.setStore(barleyPunch);
        settings.setCompanyEmail(
                "old@barleypunch.test"
        );

        when(storeSettingsService.getOrCreate(
                barleyPunch
        )).thenReturn(settings);

        when(featureAccessService.canUse(
                barleyPunch,
                "WHITE_LABEL"
        )).thenReturn(false);

        mockMvc.perform(
                post("/admin/store/settings")
                        .header(
                                "Host",
                                "barleypunch.local"
                        )
                        .param(
                                "companyEmail",
                                "ventas@barleypunch.test"
                        )
                        .param(
                                "currency",
                                "MXN"
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

        verify(storeSettingsService)
                .save(settings);

        verify(adminUserRepository)
                .findByEmail(
                        "admin@barleypunch.test"
                );
    }

    /*
     * =========================================================
     * SUPER ADMIN
     * =========================================================
     */

    @Test
    void superAdminShouldAccessTenantSettings()
            throws Exception {

        authenticate(
                "superadmin@webempresarial.test",
                "SUPER_ADMIN"
        );

        when(storeContextService.getCurrentStore(any()))
                .thenReturn(barleyPunch);

        com.webempresarial.store.entity.StoreSettings settings =
                new com.webempresarial.store.entity.StoreSettings();

        settings.setStore(barleyPunch);

        when(storeSettingsService.getOrCreate(
                barleyPunch
        )).thenReturn(settings);

        when(featureAccessService.canUse(
                barleyPunch,
                "WHITE_LABEL"
        )).thenReturn(false);

        mockMvc.perform(
                post("/admin/store/settings")
                        .header(
                                "Host",
                                "barleypunch.local"
                        )
                        .param(
                                "companyEmail",
                                "platform-update@test.com"
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
         * La política existente establece que SUPER_ADMIN
         * puede operar transversalmente y el interceptor
         * no consulta AdminUserRepository.
         */
        verify(
                adminUserRepository,
                never()
        ).findByEmail(any());

        verify(storeSettingsService)
                .save(settings);
    }

    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private AdminUser storeAdmin(
            String email,
            Store store
    ) {

        AdminUser admin =
                new AdminUser();

        admin.setEmail(email);
        admin.setRole(
                AdminRole.STORE_ADMIN
        );
        admin.setStore(store);
        admin.setEnabled(true);

        return admin;
    }

    private void authenticate(
            String username,
            String role
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        java.util.List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}