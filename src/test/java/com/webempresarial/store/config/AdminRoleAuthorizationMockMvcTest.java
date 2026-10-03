package com.webempresarial.store.config;

import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;

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
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                AdminRoleAuthorizationMockMvcTest.TestController.class
)
@Import(SecurityConfig.class)
class AdminRoleAuthorizationMockMvcTest {

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

    /*
     * ---------------------------------------------------------
     * STORE_STAFF
     * ---------------------------------------------------------
     */

    @Test
    void storeStaff_shouldAccessOperationalPanel()
            throws Exception {

        mockMvc.perform(
                get("/admin/test-operational")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeStaff_shouldNotAccessUsers()
            throws Exception {

        mockMvc.perform(
                get("/admin/users")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeStaff_shouldNotAccessBilling()
            throws Exception {

        mockMvc.perform(
                get("/admin/billing")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeStaff_shouldNotAccessStoreSettings()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/settings")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeStaff_shouldNotAccessStores()
            throws Exception {

        mockMvc.perform(
                get("/admin/stores")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeStaff_shouldNotAccessPlatform()
            throws Exception {

        mockMvc.perform(
                get("/admin/platform")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());
    }

    /*
     * ---------------------------------------------------------
     * STORE_ADMIN
     * ---------------------------------------------------------
     */

    @Test
    void storeAdmin_shouldAccessUsers()
            throws Exception {

        mockMvc.perform(
                get("/admin/users")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeAdmin_shouldAccessBilling()
            throws Exception {

        mockMvc.perform(
                get("/admin/billing")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeAdmin_shouldAccessStoreSettings()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/settings")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeAdmin_shouldNotAccessStores()
            throws Exception {

        mockMvc.perform(
                get("/admin/stores")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeAdmin_shouldNotAccessPlatform()
            throws Exception {

        mockMvc.perform(
                get("/admin/platform")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());
    }

    /*
     * ---------------------------------------------------------
     * SUPER_ADMIN
     * ---------------------------------------------------------
     */

    @Test
    void superAdmin_shouldAccessStores()
            throws Exception {

        mockMvc.perform(
                get("/admin/stores")
                        .with(
                                user("superadmin@webempresarial.test")
                                        .roles("SUPER_ADMIN")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void superAdmin_shouldAccessPlatform()
            throws Exception {

        mockMvc.perform(
                get("/admin/platform")
                        .with(
                                user("superadmin@webempresarial.test")
                                        .roles("SUPER_ADMIN")
                        )
        )
        .andExpect(status().isOk());
    }

    /*
     * ---------------------------------------------------------
     * Authentication
     * ---------------------------------------------------------
     */

    @Test
    void anonymous_shouldNotAccessAdminPanel()
            throws Exception {

        mockMvc.perform(
                get("/admin/test-operational")
        )
        .andExpect(status().is3xxRedirection());
    }

    /*
     * Controller mínimo utilizado únicamente para comprobar
     * las reglas URL -> ROLE definidas en SecurityConfig.
     */
    @RestController
    static class TestController {

        @GetMapping("/admin/test-operational")
        String operational() {
            return "OK";
        }

        @GetMapping("/admin/users")
        String users() {
            return "OK";
        }

        @GetMapping("/admin/billing")
        String billing() {
            return "OK";
        }

        @GetMapping("/admin/store/settings")
        String settings() {
            return "OK";
        }

        @GetMapping("/admin/stores")
        String stores() {
            return "OK";
        }

        @GetMapping("/admin/platform")
        String platform() {
            return "OK";
        }
    }
}
