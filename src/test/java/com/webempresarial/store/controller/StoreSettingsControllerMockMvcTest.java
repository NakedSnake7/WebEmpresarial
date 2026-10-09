package com.webempresarial.store.controller;

import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreThemeResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.view;

@WebMvcTest(StoreSettingsController.class)
@Import(SecurityConfig.class)
class StoreSettingsControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @MockitoBean
    private FeatureAccessService features;

    /*
     * Dependencias requeridas por SecurityConfig / infraestructura MVC.
     */
    @MockitoBean
    private AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService inventoryPersistentAlertService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver storeThemeResolver;

    private Store store;
    private StoreSettings settings;

    @BeforeEach
    void setUp() throws Exception {

        store = new Store();
        store.setId(100L);
        store.setNombre("ACME");
        store.setDominio("acme.web-empresarial.com");

        settings = new StoreSettings();
        settings.setStore(store);

        settings.setCompanyEmail("old@acme.test");
        settings.setPrimaryColor("#111827");
        settings.setSecondaryColor("#6B7280");
        settings.setAccentColor("#2563EB");
        settings.setFontFamily("Inter");
        settings.setCurrency("MXN");

        settings.setCustomCss(
                "body { color: red; }"
        );

        settings.setCustomJs(
                "console.log('old');"
        );

        /*
         * En @WebMvcTest los interceptores mockeados siguen formando
         * parte de la cadena MVC.
         *
         * Mockito devuelve false para boolean por defecto, por lo que
         * sin estos stubs la request sería abortada silenciosamente.
         */
        when(adminTenantAccessInterceptor.preHandle(
                any(),
                any(),
                any()
        )).thenReturn(true);

        when(subscriptionInterceptor.preHandle(
                any(),
                any(),
                any()
        )).thenReturn(true);
    }

    private void mockCurrentTenant() {

        when(storeContextService.getCurrentStore(
                any(HttpServletRequest.class)
        )).thenReturn(store);

        when(storeSettingsService.getOrCreate(store))
                .thenReturn(settings);
    }

    /*
     * =========================================================
     * AUTHENTICATION / AUTHORIZATION
     * =========================================================
     */

    @Test
    void anonymousShouldBeRedirectedToLogin()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/settings")
        )
        .andExpect(status().is3xxRedirection());

        verifyNoInteractions(storeContextService);
        verifyNoInteractions(storeSettingsService);
    }

    @Test
    void storeStaffShouldNotAccessSettings()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/settings")
                        .with(
                                user("staff@acme.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(storeContextService);
        verifyNoInteractions(storeSettingsService);
    }

    @Test
    void storeAdminShouldAccessSettings()
            throws Exception {

        mockCurrentTenant();

        mockMvc.perform(
                get("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "admin/store/settings"
        ))
        .andExpect(model().attribute(
                "store",
                store
        ))
        .andExpect(model().attribute(
                "settings",
                settings
        ))
        .andExpect(model().attribute(
                "features",
                features
        ));

        verify(storeContextService, atLeastOnce())
        .getCurrentStore(
                any(HttpServletRequest.class)
        );

verify(storeSettingsService, atLeastOnce())
        .getOrCreate(store);


    }

    /*
     * =========================================================
     * CSRF
     * =========================================================
     */

    @Test
    void postSettingsWithoutCsrfShouldBeForbidden()
            throws Exception {

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
                        .param(
                                "companyEmail",
                                "ventas@acme.test"
                        )
        )
        .andExpect(status().isForbidden());

        /*
         * CSRF debe detener la petición antes de llegar
         * al controlador.
         */
        verifyNoInteractions(storeContextService);
        verifyNoInteractions(storeSettingsService);
    }

    /*
     * =========================================================
     * BASIC SETTINGS
     * =========================================================
     */

    @Test
    void storeAdminShouldSaveSettingsWithCsrf()
            throws Exception {

        mockCurrentTenant();

        when(features.canUse(
                store,
                "WHITE_LABEL_FULL"
        )).thenReturn(false);

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
                        .with(csrf())

                        .param(
                                "companyEmail",
                                "ventas@acme.test"
                        )
                        .param(
                                "companyPhone",
                                "+52 222 123 4567"
                        )
                        .param(
                                "companyAddress",
                                "Puebla"
                        )
                        .param(
                                "companyWebsite",
                                "https://acme.test"
                        )
                        .param(
                                "contactName",
                                "ACME Owner"
                        )
                        .param(
                                "currency",
                                "MXN"
                        )
                        .param(
                                "proposalFooter",
                                "50% anticipo"
                        )

                        .param(
                                "faviconUrl",
                                "https://cdn.test/favicon.png"
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
                                "Montserrat"
                        )
                        .param(
                                "heroImageUrl",
                                "https://cdn.test/hero.jpg"
                        )
                        .param(
                                "slogan",
                                "Construimos el futuro"
                        )

                        .param(
                                "googleAnalyticsId",
                                "G-123456"
                        )
                        .param(
                                "metaPixelId",
                                "123456789"
                        )
                        .param(
                                "tiktokPixelId",
                                "TIKTOK-123"
                        )
                        .param(
                                "hotjarId",
                                "987654"
                        )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrl(
                        "/admin/store/settings?success"
                )
        );

        assertThat(settings.getCompanyEmail())
                .isEqualTo("ventas@acme.test");

        assertThat(settings.getCompanyPhone())
                .isEqualTo("+52 222 123 4567");

        assertThat(settings.getCompanyAddress())
                .isEqualTo("Puebla");

        assertThat(settings.getCompanyWebsite())
                .isEqualTo("https://acme.test");

        assertThat(settings.getContactName())
                .isEqualTo("ACME Owner");

        assertThat(settings.getCurrency())
                .isEqualTo("MXN");

        assertThat(settings.getProposalFooter())
                .isEqualTo("50% anticipo");

        assertThat(settings.getFaviconUrl())
                .isEqualTo(
                        "https://cdn.test/favicon.png"
                );

        assertThat(settings.getPrimaryColor())
                .isEqualTo("#123456");

        assertThat(settings.getSecondaryColor())
                .isEqualTo("#654321");

        assertThat(settings.getAccentColor())
                .isEqualTo("#ABCDEF");

        assertThat(settings.getFontFamily())
                .isEqualTo("Montserrat");

        assertThat(settings.getHeroImageUrl())
                .isEqualTo(
                        "https://cdn.test/hero.jpg"
                );

        assertThat(settings.getSlogan())
                .isEqualTo(
                        "Construimos el futuro"
                );

        assertThat(settings.getGoogleAnalyticsId())
                .isEqualTo("G-123456");

        assertThat(settings.getMetaPixelId())
                .isEqualTo("123456789");

        assertThat(settings.getTiktokPixelId())
                .isEqualTo("TIKTOK-123");

        assertThat(settings.getHotjarId())
                .isEqualTo("987654");

        verify(storeSettingsService)
                .save(settings);
    }

    /*
     * =========================================================
     * WHITE LABEL SECURITY
     * =========================================================
     */

    @Test
    void nonWhiteLabelTenantShouldNotModifyCustomCssOrJs()
            throws Exception {

        mockCurrentTenant();

        when(features.canUse(
                store,
                "WHITE_LABEL_FULL"
        )).thenReturn(false);

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
                        .with(csrf())

                        /*
                         * Simulamos manipulación manual del POST.
                         * Estos campos no deberían estar disponibles
                         * para este plan.
                         */
                        .param(
                                "customCss",
                                "body { display:none; }"
                        )
                        .param(
                                "customJs",
                                "alert('hacked');"
                        )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrl(
                        "/admin/store/settings?success"
                )
        );

        assertThat(settings.getCustomCss())
                .isEqualTo(
                        "body { color: red; }"
                );

        assertThat(settings.getCustomJs())
                .isEqualTo(
                        "console.log('old');"
                );

        verify(features)
                .canUse(
                        store,
                        "WHITE_LABEL_FULL"
                );

        verify(storeSettingsService)
                .save(settings);
    }

    @Test
    void whiteLabelTenantShouldModifyCustomCssAndJs()
            throws Exception {

        mockCurrentTenant();

        when(features.canUse(
                store,
                "WHITE_LABEL_FULL"
        )).thenReturn(true);

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
                        .with(csrf())

                        .param(
                                "customCss",
                                "body { background:black; }"
                        )
                        .param(
                                "customJs",
                                "console.log('premium');"
                        )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrl(
                        "/admin/store/settings?success"
                )
        );

        assertThat(settings.getCustomCss())
                .isEqualTo(
                        "body { background:black; }"
                );

        assertThat(settings.getCustomJs())
                .isEqualTo(
                        "console.log('premium');"
                );

        verify(features)
                .canUse(
                        store,
                        "WHITE_LABEL_FULL"
                );

        verify(storeSettingsService)
                .save(settings);
    }

    /*
     * =========================================================
     * LOGO ENDPOINTS / CSRF
     * =========================================================
     */

    @Test
    void deleteLogoWithoutCsrfShouldBeForbidden()
            throws Exception {

        mockMvc.perform(
                post(
                        "/admin/store/settings/logo/delete"
                )
                .with(
                        user("admin@acme.test")
                                .roles("STORE_ADMIN")
                )
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(storeContextService);
        verifyNoInteractions(storeSettingsService);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    void storeStaffShouldNotSaveSettings()
            throws Exception {

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(
                                user("staff@acme.test")
                                        .roles("STORE_STAFF")
                        )
                        .with(csrf())
                        .param(
                                "companyEmail",
                                "attack@acme.test"
                        )
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(storeContextService);
        verifyNoInteractions(storeSettingsService);
    }
}
