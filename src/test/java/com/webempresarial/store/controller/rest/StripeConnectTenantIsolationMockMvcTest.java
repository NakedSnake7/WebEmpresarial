package com.webempresarial.store.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeConnectService;
import com.webempresarial.store.theme.ThemeModelAdvice;

@WebMvcTest(
        controllers = StripeConnectController.class,
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
class StripeConnectTenantIsolationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StripeConnectService stripeConnectService;

    @MockitoBean
    private StoreContextService storeContextService;



    @MockitoBean
    private AdminUserRepository adminUserRepository;



    @MockitoBean
    private SubscriptionInterceptor
            subscriptionInterceptor;

    private Store stride;
    private Store barleyPunch;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        stride = new Store();
        stride.setId(3L);

        barleyPunch = new Store();
        barleyPunch.setId(4L);
    }


    @BeforeEach
    void clearSecurityContextBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContextAfter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void storeAdmin_shouldAccessOwnTenant()
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
        )).thenReturn(Optional.of(admin));

        when(storeContextService.getCurrentStore(any()))
                .thenReturn(stride);

        when(stripeConnectService.createOnboardingLink(
                eq(stride),
                any(String.class)
        )).thenReturn(
                "https://connect.stripe.test/onboarding"
        );

        mockMvc.perform(
                post(
                        "/api/admin/stripe/connect/onboarding"
                )
                        .header(
                                "Host",
                                "stride.local"
                        )
        )
                .andExpect(status().isOk());

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );

        verify(stripeConnectService)
                .createOnboardingLink(
                        eq(stride),
                        any(String.class)
                );
    }

    @Test
    void storeAdmin_shouldBeForbiddenForDifferentTenant()
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
        )).thenReturn(Optional.of(admin));

        when(storeContextService.getCurrentStore(any()))
                .thenReturn(barleyPunch);

        mockMvc.perform(
                post(
                        "/api/admin/stripe/connect/onboarding"
                )
                        .header(
                                "Host",
                                "barleypunch.local"
                        )
        )
                .andExpect(status().isForbidden());

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );

        verify(
                stripeConnectService,
                never()
        ).createOnboardingLink(
                any(),
                any()
        );
    }

    @Test
    void superAdmin_shouldAccessDifferentTenant()
            throws Exception {

        authenticate(
                "superadmin@webempresarial.test",
                "SUPER_ADMIN"
        );

        when(storeContextService.getCurrentStore(any()))
                .thenReturn(barleyPunch);

        when(stripeConnectService.createOnboardingLink(
                eq(barleyPunch),
                any(String.class)
        )).thenReturn(
                "https://connect.stripe.test/onboarding"
        );

        mockMvc.perform(
                post(
                        "/api/admin/stripe/connect/onboarding"
                )
                        .header(
                                "Host",
                                "barleypunch.local"
                        )
        )
                .andExpect(status().isOk());

        /*
         * SUPER_ADMIN salta la comprobación
         * AdminUser -> Store.
         */
        verify(
                adminUserRepository,
                never()
        ).findByEmail(any());

        verify(stripeConnectService)
                .createOnboardingLink(
                        eq(barleyPunch),
                        any(String.class)
                );
    }

    private AdminUser storeAdmin(
            String email,
            Store store
    ) {

        AdminUser admin = new AdminUser();

        admin.setEmail(email);
        admin.setRole(AdminRole.STORE_ADMIN);
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
                .setAuthentication(authentication);
    }
}