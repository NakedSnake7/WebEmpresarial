package com.webempresarial.store.controller.admin;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.FeatureViewAdvice;
import com.webempresarial.store.config.SecurityViewAdvice;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.service.AdminAccountActivationService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.theme.ThemeModelAdvice;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(
        controllers =
                AdminAccountActivationController.class,
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
@AutoConfigureMockMvc(addFilters = false)
class AdminAccountActivationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAccountActivationService activationService;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService
            inventoryPersistentAlertService;
    @MockitoBean
    private AdminTenantAccessInterceptor
            adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor
            subscriptionInterceptor;

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
    void shouldShowActivationFormForValidToken()
            throws Exception {

        AdminUser admin =
                new AdminUser();

        admin.setEmail(
                "owner@stride.test"
        );

        AdminAccountActivationToken activation =
                new AdminAccountActivationToken();

        activation.setAdminUser(admin);
        activation.setToken("valid-token");

        when(
                activationService.validate(
                        "valid-token"
                )
        ).thenReturn(activation);

        mockMvc.perform(
                get("/admin/activate")
                        .param(
                                "token",
                                "valid-token"
                        )
        )
        .andDo(print())
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/activate"
                )
        )
        .andExpect(
                model().attribute(
                        "token",
                        "valid-token"
                )
        )
        .andExpect(
                model().attribute(
                        "email",
                        "owner@stride.test"
                )
        )
        .andExpect(
                model().attributeDoesNotExist(
                        "activationError"
                )
        );

        verify(activationService)
                .validate(
                        "valid-token"
                );
    }

    @Test
    void shouldShowErrorForInvalidToken()
            throws Exception {

        when(
                activationService.validate(
                        "invalid-token"
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Token de activación inválido"
                )
        );

        mockMvc.perform(
                get("/admin/activate")
                        .param(
                                "token",
                                "invalid-token"
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/activate"
                )
        )
        .andExpect(
                model().attribute(
                        "activationError",
                        "Token de activación inválido"
                )
        );

        verify(activationService)
                .validate(
                        "invalid-token"
                );
    }

    @Test
    void shouldRejectDifferentPasswords()
            throws Exception {

        mockMvc.perform(
                post("/admin/activate")
                        .param(
                                "token",
                                "valid-token"
                        )
                        .param(
                                "password",
                                "StrongPassword123!"
                        )
                        .param(
                                "confirmPassword",
                                "DifferentPassword123!"
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/activate"
                )
        )
        .andExpect(
                model().attribute(
                        "token",
                        "valid-token"
                )
        )
        .andExpect(
                model().attribute(
                        "activationError",
                        "Las contraseñas no coinciden"
                )
        );

        verify(
                activationService,
                never()
        ).activate(
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldActivateAccountAndRedirectToLogin()
            throws Exception {

        mockMvc.perform(
                post("/admin/activate")
                        .param(
                                "token",
                                "valid-token"
                        )
                        .param(
                                "password",
                                "StrongPassword123!"
                        )
                        .param(
                                "confirmPassword",
                                "StrongPassword123!"
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        )
        .andExpect(
                redirectedUrl(
                        "/admin/login?activated"
                )
        );

        verify(activationService)
                .activate(
                        "valid-token",
                        "StrongPassword123!"
                );
    }

    @Test
    void shouldShowErrorWhenActivationFails()
            throws Exception {

        doThrow(
                new IllegalArgumentException(
                        "Token de activación expirado o utilizado"
                )
        )
        .when(activationService)
        .activate(
                "expired-token",
                "StrongPassword123!"
        );

        mockMvc.perform(
                post("/admin/activate")
                        .param(
                                "token",
                                "expired-token"
                        )
                        .param(
                                "password",
                                "StrongPassword123!"
                        )
                        .param(
                                "confirmPassword",
                                "StrongPassword123!"
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/activate"
                )
        )
        .andExpect(
                model().attribute(
                        "token",
                        "expired-token"
                )
        )
        .andExpect(
                model().attribute(
                        "activationError",
                        "Token de activación expirado o utilizado"
                )
        );

        verify(activationService)
                .activate(
                        "expired-token",
                        "StrongPassword123!"
                );
    }
}