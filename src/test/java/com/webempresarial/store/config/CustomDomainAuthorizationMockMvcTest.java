package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory
        .InventoryPersistentAlertService;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.request;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                CustomDomainAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class CustomDomainAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerTenantAccessInterceptor
            customerTenantAccessInterceptor;

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
    private SidebarRegistry sidebarRegistry;

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

    @BeforeEach
    void allowMvcInterceptors() throws Exception {

        when(
                customerTenantAccessInterceptor.preHandle(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(true);

        when(
                adminTenantAccessInterceptor.preHandle(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(true);

        when(
                subscriptionInterceptor.preHandle(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(true);
    }

    @Test
    void domainViewerCanReadDomainPage()
            throws Exception {

        mockMvc.perform(
                get("/admin/domains")
                        .with(
                                tenantUser(
                                        AdminPermission.DOMAIN_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void domainManagerWithoutViewCannotReadDomainPage()
            throws Exception {

        mockMvc.perform(
                get("/admin/domains")
                        .with(
                                tenantUser(
                                        AdminPermission.DOMAIN_MANAGE
                                )
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void domainManagerCanRequestDomain()
            throws Exception {

        mockMvc.perform(
                post("/admin/domains")
                        .with(
                                tenantUser(
                                        AdminPermission.DOMAIN_MANAGE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void domainViewerCannotRequestDomain()
            throws Exception {

        mockMvc.perform(
                post("/admin/domains")
                        .with(
                                tenantUser(
                                        AdminPermission.DOMAIN_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void domainManagerCanDisconnectDomain()
            throws Exception {

        mockMvc.perform(
                post("/admin/domains/disconnect")
                        .with(
                                tenantUser(
                                        AdminPermission.DOMAIN_MANAGE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void tenantAdministratorCannotVerifyDomain()
            throws Exception {

        mockMvc.perform(
                post("/admin/domains/verify")
                        .with(
                                tenantUser(
                                        AdminPermission.DOMAIN_MANAGE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanVerifyDomain()
            throws Exception {

        mockMvc.perform(
                post("/admin/domains/verify")
                        .with(
                                user("root@webempresarial.com")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_SUPER_ADMIN"
                                                )
                                        )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void unsupportedDomainMethodMustFailClosed()
            throws Exception {

        mockMvc.perform(
                request(
                        HttpMethod.PUT,
                        "/admin/domains"
                )
                .with(
                        tenantUser(
                                AdminPermission.DOMAIN_VIEW,
                                AdminPermission.DOMAIN_MANAGE
                        )
                )
                .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    private org.springframework.security.test.web.servlet
            .request.SecurityMockMvcRequestPostProcessors
            .UserRequestPostProcessor tenantUser(
                    AdminPermission... permissions
            ) {

        java.util.List<SimpleGrantedAuthority> authorities =
                new java.util.ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_STORE_ADMIN"
                )
        );

        for (AdminPermission permission : permissions) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            permission.authority()
                    )
            );
        }

        return user("admin@stride.test")
                .authorities(
                        authorities.toArray(
                                SimpleGrantedAuthority[]::new
                        )
                );
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/domains")
        String index() {
            return "OK";
        }

        @PostMapping("/admin/domains")
        String requestDomain() {
            return "OK";
        }

        @PostMapping("/admin/domains/disconnect")
        String disconnect() {
            return "OK";
        }

        @PostMapping("/admin/domains/verify")
        String verify() {
            return "OK";
        }
    }
}
