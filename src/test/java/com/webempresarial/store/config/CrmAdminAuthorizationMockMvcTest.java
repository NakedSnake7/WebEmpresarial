package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                CrmAdminAuthorizationMockMvcTest.TestController.class
)
@Import(SecurityConfig.class)
class CrmAdminAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService inventoryPersistentAlertService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver storeThemeResolver;

    @MockitoBean
    private FeatureAccessService featureAccessService;

    @Test
    void clientShouldNotAccessCrmViews()
            throws Exception {

        mockMvc.perform(
                get("/crm/dashboard")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/crm/leads/1")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void clientShouldNotAccessCrmApi()
            throws Exception {

        mockMvc.perform(
                get("/api/crm/leads")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                patch("/api/crm/leads/1/status")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeAdminShouldAccessCrm()
            throws Exception {

        mockMvc.perform(
                get("/crm/dashboard")
                        .with(user("admin@stride.test")
                                .roles("STORE_ADMIN"))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/leads")
                        .with(user("admin@stride.test")
                                .roles("STORE_ADMIN"))
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeStaffShouldAccessCrm()
            throws Exception {

        mockMvc.perform(
                get("/crm/dashboard")
                        .with(user("staff@stride.test")
                                .roles("STORE_STAFF"))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/leads")
                        .with(user("staff@stride.test")
                                .roles("STORE_STAFF"))
        )
        .andExpect(status().isOk());
    }

    @RestController
    static class TestController {

        @GetMapping("/crm/dashboard")
        String dashboard() {
            return "OK";
        }

        @GetMapping("/crm/leads/{id}")
        String leadDetail() {
            return "OK";
        }

        @GetMapping("/api/crm/leads")
        String leads() {
            return "OK";
        }

        @PatchMapping("/api/crm/leads/{id}/status")
        String updateLeadStatus() {
            return "OK";
        }
    }
}
