package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                StoreCrmSettingsAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class StoreCrmSettingsAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

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
    void storeAdminRoleAloneShouldNotConfigureCrm()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/crm")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void crmEditShouldNotGrantStoreConfiguration()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/crm")
                        .with(
                                adminWith(
                                        AdminPermission.CRM_EDIT
                                )
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void salesWithCrmEditShouldNotConfigureCrm()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/crm")
                        .with(
                                user("sales@stride.test")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_SALES"
                                                ),
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .CRM_EDIT
                                                                .authority()
                                                )
                                        )
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void storeEditShouldAllowCrmSettings()
            throws Exception {

        RequestPostProcessor admin =
                adminWith(
                        AdminPermission.STORE_EDIT
                );

        mockMvc.perform(
                get("/admin/store/crm")
                        .with(admin)
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/store/crm/budget-ranges")
                        .with(admin)
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/store/crm/pipeline")
                        .with(admin)
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/store/crm/automation")
                        .with(admin)
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void storeEditShouldAllowBudgetWrites()
            throws Exception {

        mockMvc.perform(
                post("/admin/store/crm/budget-ranges")
                        .with(
                                adminWith(
                                        AdminPermission.STORE_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );
    }

    private RequestPostProcessor adminWith(
            AdminPermission... permissions
    ) {

        SimpleGrantedAuthority[] authorities =
                Stream.concat(
                        Stream.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_STORE_ADMIN"
                                )
                        ),
                        Arrays.stream(permissions)
                                .map(permission ->
                                        new SimpleGrantedAuthority(
                                                permission.authority()
                                        )
                                )
                )
                .toArray(
                        SimpleGrantedAuthority[]::new
                );

        return user(
                "admin@stride.test"
        ).authorities(
                authorities
        );
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/store/crm")
        String settings() {
            return "OK";
        }

        @GetMapping("/admin/store/crm/budget-ranges")
        String budgetRanges() {
            return "OK";
        }

        @PostMapping("/admin/store/crm/budget-ranges")
        String createBudgetRange() {
            return "OK";
        }

        @GetMapping("/admin/store/crm/pipeline")
        String pipeline() {
            return "OK";
        }

        @GetMapping("/admin/store/crm/automation")
        String automation() {
            return "OK";
        }
    }
}
