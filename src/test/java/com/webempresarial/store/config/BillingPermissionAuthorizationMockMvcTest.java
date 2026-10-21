package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.CustomerTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminPermission;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
                BillingPermissionAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class BillingPermissionAuthorizationMockMvcTest {

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

    /*
     * STORE_ADMIN tiene SUBSCRIPTION_VIEW.
     */
    @Test
    void subscriptionViewPermissionShouldAllowBillingPage()
            throws Exception {

        mockMvc.perform(
                get("/admin/billing")
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_STORE_ADMIN"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .SUBSCRIPTION_VIEW
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void subscriptionViewPermissionShouldAllowBillingSuccess()
            throws Exception {

        mockMvc.perform(
                get("/admin/billing/success")
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_STORE_ADMIN"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .SUBSCRIPTION_VIEW
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    /*
     * SUBSCRIPTION_VIEW no permite modificar la suscripción.
     */
    @Test
    void subscriptionViewPermissionShouldNotAllowPlanChange()
            throws Exception {

        mockMvc.perform(
                post("/admin/billing/checkout")
                        .with(csrf())
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_STORE_ADMIN"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .SUBSCRIPTION_VIEW
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void subscriptionViewPermissionShouldNotAllowBillingPortal()
            throws Exception {

        mockMvc.perform(
                post("/admin/billing/portal")
                        .with(csrf())
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_STORE_ADMIN"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .SUBSCRIPTION_VIEW
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(status().isForbidden());
    }

    /*
     * STORE_OWNER recibe SUBSCRIPTION_MANAGE.
     */
    @Test
    void subscriptionManagePermissionShouldAllowPlanChange()
            throws Exception {

        mockMvc.perform(
                post("/admin/billing/checkout")
                        .with(csrf())
                        .with(
                                user("owner@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_STORE_OWNER"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .SUBSCRIPTION_MANAGE
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void subscriptionManagePermissionShouldAllowBillingPortal()
            throws Exception {

        mockMvc.perform(
                post("/admin/billing/portal")
                        .with(csrf())
                        .with(
                                user("owner@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_STORE_OWNER"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .SUBSCRIPTION_MANAGE
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    /*
     * Un rol sin PERM_* no obtiene acceso implícito.
     */
    @Test
    void ownerRoleAloneShouldNotAllowBillingView()
            throws Exception {

        mockMvc.perform(
                get("/admin/billing")
                        .with(
                                user("owner@stride.test")
                                        .roles("STORE_OWNER")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void salesShouldNotAccessBilling()
            throws Exception {

        mockMvc.perform(
                get("/admin/billing")
                        .with(
                                user("sales@stride.test")
                                        .roles("SALES")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/billing")
        String billing() {
            return "OK";
        }

        @GetMapping("/admin/billing/success")
        String billingSuccess() {
            return "OK";
        }

        @PostMapping("/admin/billing/checkout")
        String checkout() {
            return "OK";
        }

        @PostMapping("/admin/billing/portal")
        String portal() {
            return "OK";
        }
    }
}
