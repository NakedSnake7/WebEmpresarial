package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.CustomerTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                WhatsAppAutomationAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class WhatsAppAutomationAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerTenantAccessInterceptor customerTenantAccessInterceptor;

    @MockitoBean
    private AdminAuthenticationSuccessHandler
            adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService
            adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService
            authUserDetailsService;

    @MockitoBean
    private StoreContextService
            storeContextService;

    @MockitoBean
    private SidebarRegistry
            sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService
            inventoryPersistentAlertService;

    @MockitoBean
    private StoreSettingsService
            storeSettingsService;

    @MockitoBean
    private AdminTenantAccessInterceptor
            adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor
            subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver
            storeThemeResolver;

    @MockitoBean
    private FeatureAccessService
            featureAccessService;

    @Test
    void storeAdminShouldNotAccessUnimplementedWhatsAppAutomation()
            throws Exception {

        mockMvc.perform(
                get("/admin/whatsapp")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/admin/whatsapp/templates")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/whatsapp")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void superAdminShouldNotBypassWhatsAppAutomationFailClosed()
            throws Exception {

        mockMvc.perform(
                get("/admin/whatsapp")
                        .with(
                                user("root@webempresarial.com")
                                        .roles("SUPER_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/whatsapp")
        String index() {
            return "OK";
        }

        @GetMapping("/admin/whatsapp/templates")
        String templates() {
            return "OK";
        }

        @PostMapping("/admin/whatsapp")
        String create() {
            return "OK";
        }
    }
}
