package com.webempresarial.store.controller.admin;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.status;

import java.util.List;

import com.webempresarial.store.commerce.application.inventory
        .InventoryPersistentAlertService;
import com.webempresarial.store.config
        .AdminAuthenticationSuccessHandler;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.feature.registry
        .SidebarRegistry;
import com.webempresarial.store.interceptor
        .AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor
        .CustomerTenantAccessInterceptor;
import com.webempresarial.store.interceptor
        .SubscriptionInterceptor;
import com.webempresarial.store.service
        .AdminAccountInvitationResendService;
import com.webempresarial.store.service
        .AdminUserDetailsService;
import com.webempresarial.store.service
        .AuthUserDetailsService;
import com.webempresarial.store.service
        .FeatureAccessService;
import com.webempresarial.store.service
        .PlatformOperationsService;
import com.webempresarial.store.service
        .StoreContextService;
import com.webempresarial.store.service
        .StoreSettingsService;
import com.webempresarial.store.theme
        .StoreThemeResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet
        .WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito
        .MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers =
                PlatformOperationsController.class
)
@Import(SecurityConfig.class)
class PlatformOperationsRecoveryAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlatformOperationsService
            platformOperationsService;

    @MockitoBean
    private AdminAccountInvitationResendService
            invitationResendService;

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

        org.mockito.Mockito.when(
                customerTenantAccessInterceptor.preHandle(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(true);

        org.mockito.Mockito.when(
                adminTenantAccessInterceptor.preHandle(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(true);

        org.mockito.Mockito.when(
                subscriptionInterceptor.preHandle(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(true);
    }

    @Test
    void superAdminCanResendActivation()
            throws Exception {

        mockMvc.perform(
                post(
                        "/admin/platform/operations/"
                        + "resend-activation"
                )
                .param(
                        "adminUserId",
                        "42"
                )
                .with(
                        user("root@test")
                                .roles("SUPER_ADMIN")
                )
                .with(csrf())
        )
        .andExpect(
                status().is3xxRedirection()
        )
        .andExpect(
                redirectedUrl(
                        "/admin/platform/operations"
                )
        )
        .andExpect(
                flash().attribute(
                        "platformOperationSuccess",
                        "Invitación de activación reenviada."
                )
        );

        verify(invitationResendService)
                .resend(42L);
    }

    @Test
    void tenantRolesCannotResendActivation()
            throws Exception {

        for (String role : List.of(
                "STORE_OWNER",
                "STORE_ADMIN",
                "STORE_STAFF",
                "SALES"
        )) {

            mockMvc.perform(
                    post(
                            "/admin/platform/operations/"
                            + "resend-activation"
                    )
                    .param(
                            "adminUserId",
                            "42"
                    )
                    .with(
                            user(role + "@test")
                                    .roles(role)
                    )
                    .with(csrf())
            )
            .andExpect(
                    status().isForbidden()
            );
        }

        verifyNoInteractions(
                invitationResendService
        );
    }

    @Test
    void anonymousCannotResendActivation()
            throws Exception {

        mockMvc.perform(
                post(
                        "/admin/platform/operations/"
                        + "resend-activation"
                )
                .param(
                        "adminUserId",
                        "42"
                )
                .with(csrf())
        )
        .andExpect(
                status().is3xxRedirection()
        );

        verifyNoInteractions(
                invitationResendService
        );
    }
}
