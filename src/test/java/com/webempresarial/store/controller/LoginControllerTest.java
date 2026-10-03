package com.webempresarial.store.controller;

import com.webempresarial.store.config.FeatureViewAdvice;
import com.webempresarial.store.config.SecurityViewAdvice;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.theme.StoreThemeResolver;
import com.webempresarial.store.theme.ThemeModelAdvice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        controllers = LoginController.class,
        excludeFilters = {
                @Filter(
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
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreThemeResolver themeResolver;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @Test
    void shouldShowActivationConfirmationAfterAccountActivation() throws Exception {

        mockMvc.perform(
                get("/admin/login")
                        .param("activated", "")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/login"))
        .andExpect(content().string(
                org.hamcrest.Matchers.containsString(
                        "Tu cuenta fue activada correctamente."
                )
        ))
        .andExpect(content().string(
                org.hamcrest.Matchers.containsString(
                        "Ya puedes iniciar sesión."
                )
        ));
    }

    @Test
    void shouldNotShowActivationConfirmationOnRegularLogin() throws Exception {

        mockMvc.perform(
                get("/admin/login")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/login"))
        .andExpect(content().string(
                org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(
                                "Tu cuenta fue activada correctamente."
                        )
                )
        ));
    }
}
