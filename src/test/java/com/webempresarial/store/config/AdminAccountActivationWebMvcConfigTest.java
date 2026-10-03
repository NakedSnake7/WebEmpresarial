package com.webempresarial.store.config;

import com.webempresarial.store.controller.admin.AdminAccountActivationController;
import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.service.AdminAccountActivationService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.SubscriptionAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(
        AdminAccountActivationWebMvcConfigTest.TestConfig.class
)
@WebAppConfiguration
class AdminAccountActivationWebMvcConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminAccountActivationService
            activationService;

    @Autowired
    private AdminUserRepository
            adminUserRepository;

    @Autowired
    private StoreContextService
            storeContextService;

    @Autowired
    private SubscriptionAccessService
            subscriptionAccessService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                webAppContextSetup(context)
                        .build();

        reset(
                activationService,
                adminUserRepository,
                storeContextService,
                subscriptionAccessService
        );
    }

    @Test
    void shouldAllowAnonymousActivationWithoutTenantOrSubscriptionInterceptors()
            throws Exception {

        AdminUser admin =
                new AdminUser();

        admin.setId(101L);
        admin.setEmail("owner@acme.test");
        admin.setFullName("Alice Owner");
        admin.setEnabled(false);

        AdminAccountActivationToken token =
                new AdminAccountActivationToken();

        token.setAdminUser(admin);
        token.setToken(
                "activation-token-123"
        );

        when(
                activationService.validate(
                        "activation-token-123"
                )
        ).thenReturn(token);

        mockMvc.perform(
                        get("/admin/activate")
                                .param(
                                        "token",
                                        "activation-token-123"
                                )
                                .header(
                                        "Host",
                                        "acme.web-empresarial.com"
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
                                "activation-token-123"
                        )
                )
                .andExpect(
                        model().attribute(
                                "email",
                                "owner@acme.test"
                        )
                );

        /*
         * Si AdminTenantAccessInterceptor entra,
         * podría consultar AdminUserRepository
         * o StoreContextService.
         */
        verifyNoInteractions(
                adminUserRepository
        );

        /*
         * Si SubscriptionInterceptor entra,
         * intentará resolver la tienda.
         */
        verifyNoInteractions(
                storeContextService
        );

        verifyNoInteractions(
                subscriptionAccessService
        );

        verify(activationService)
                .validate(
                        "activation-token-123"
                );
    }

    @Configuration
    @EnableWebMvc
    @Import(WebMvcConfig.class)
    static class TestConfig {

        @Bean
        AdminAccountActivationService
        activationService() {

            return mock(
                    AdminAccountActivationService.class
            );
        }

        @Bean
        AdminUserRepository
        adminUserRepository() {

            return mock(
                    AdminUserRepository.class
            );
        }

        @Bean
        StoreContextService
        storeContextService() {

            return mock(
                    StoreContextService.class
            );
        }

        @Bean
        SubscriptionAccessService
        subscriptionAccessService() {

            return mock(
                    SubscriptionAccessService.class
            );
        }

        @Bean
        AdminTenantAccessInterceptor
        adminTenantAccessInterceptor(
                AdminUserRepository adminUserRepository,
                StoreContextService storeContextService
        ) {

            return new AdminTenantAccessInterceptor(
                    adminUserRepository,
                    storeContextService
            );
        }

        @Bean
        SubscriptionInterceptor
        subscriptionInterceptor(
                StoreContextService storeContextService,
                SubscriptionAccessService subscriptionAccessService
        ) {

            return new SubscriptionInterceptor(
                    storeContextService,
                    subscriptionAccessService
            );
        }

        @Bean
        AdminAccountActivationController
        adminAccountActivationController(
                AdminAccountActivationService activationService
        ) {

            return new AdminAccountActivationController(
                    activationService
            );
        }
    }
}