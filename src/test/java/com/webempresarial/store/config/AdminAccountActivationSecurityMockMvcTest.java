package com.webempresarial.store.config;

import com.webempresarial.store.controller.admin.AdminAccountActivationController;
import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.service.AdminAccountActivationService;
import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.webempresarial.store.config.FeatureViewAdvice;
import com.webempresarial.store.config.SecurityViewAdvice;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.theme.ThemeModelAdvice;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
        controllers = AdminAccountActivationController.class,
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
@Import(SecurityConfig.class)
class AdminAccountActivationSecurityMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAccountActivationService activationService;

    @MockitoBean
    private AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @BeforeEach
    void allowMvcInterceptors() throws Exception {

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
    void anonymousUserShouldAccessActivationForm()
            throws Exception {

        AdminUser admin = new AdminUser();
        admin.setEmail("owner@stride.test");

        AdminAccountActivationToken activation =
                new AdminAccountActivationToken();

        activation.setToken("valid-token");
        activation.setAdminUser(admin);

        when(
                activationService.validate("valid-token")
        ).thenReturn(activation);

        mockMvc.perform(
                get("/admin/activate")
                        .param("token", "valid-token")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/activate"))
        .andExpect(
                model().attribute(
                        "email",
                        "owner@stride.test"
                )
        );
    }

    @Test
    void anonymousUserShouldActivateAccountWithCsrf()
            throws Exception {

        mockMvc.perform(
                post("/admin/activate")
                        .with(csrf())
                        .param("token", "valid-token")
                        .param(
                                "password",
                                "StrongPassword123!"
                        )
                        .param(
                                "confirmPassword",
                                "StrongPassword123!"
                        )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrl(
                        "/admin/login?activated"
                )
        );
    }

    @Test
    void activationShouldRejectPostWithoutCsrf()
            throws Exception {

        mockMvc.perform(
                post("/admin/activate")
                        .param("token", "valid-token")
                        .param(
                                "password",
                                "StrongPassword123!"
                        )
                        .param(
                                "confirmPassword",
                                "StrongPassword123!"
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserShouldNotAccessAdminDashboard()
            throws Exception {

        mockMvc.perform(
                get("/admin/dashboard")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                redirectedUrlPattern("**/admin/login")
        );
    }

    @Test
    @WithMockUser(
            username = "staff@stride.test",
            roles = "STORE_STAFF"
    )
    void storeStaffShouldNotGainAccessToPlatformAdministration()
            throws Exception {

        mockMvc.perform(
                get("/admin/stores")
        )
        .andExpect(status().isForbidden());
    }
}
