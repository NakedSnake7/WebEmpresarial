package com.webempresarial.store.config;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.SubscriptionAccessService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(
        CatalogAdminWebMvcConfigTest.TestConfig.class
)
@WebAppConfiguration
class CatalogAdminWebMvcConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private StoreContextService storeContextService;

    @Autowired
    private SubscriptionAccessService subscriptionAccessService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                webAppContextSetup(context)
                        .build();

        reset(
                adminUserRepository,
                storeContextService,
                subscriptionAccessService
        );

        Store store = mock(Store.class);
        Subscription subscription = mock(Subscription.class);
        AdminUser admin = mock(AdminUser.class);

        when(
                adminUserRepository.findByEmail(
                        "admin@stride.test"
                )
        ).thenReturn(Optional.of(admin));

        when(admin.isEnabled())
                .thenReturn(true);

        when(admin.getStore())
                .thenReturn(store);

        when(store.getId())
                .thenReturn(3L);

        when(store.getSubscription())
                .thenReturn(subscription);

        when(
                storeContextService.getCurrentStore(
                        any(HttpServletRequest.class)
                )
        ).thenReturn(store);

        when(
                subscriptionAccessService.canAccessPlatform(
                        subscription
                )
        ).thenReturn(true);

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin@stride.test",
                                "N/A",
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_STORE_ADMIN"
                                        )
                                )
                        )
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void newProductRouteShouldUseTenantAndSubscriptionInterceptors()
            throws Exception {

        mockMvc.perform(
                get("/nuevo")
        )
        .andExpect(status().isOk());

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );

        verify(
                storeContextService,
                atLeast(2)
        ).getCurrentStore(
                any(HttpServletRequest.class)
        );

        verify(subscriptionAccessService)
                .canAccessPlatform(
                        any(Subscription.class)
                );
    }

    @Test
    void productApiRouteShouldUseTenantAndSubscriptionInterceptors()
            throws Exception {

        mockMvc.perform(
                post("/api/productos/editar/1")
        )
        .andExpect(status().isOk());

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );

        verify(
                storeContextService,
                atLeast(2)
        ).getCurrentStore(
                any(HttpServletRequest.class)
        );

        verify(subscriptionAccessService)
                .canAccessPlatform(
                        any(Subscription.class)
                );
    }

    @RestController
    static class TestController {

        @GetMapping("/nuevo")
        String newProduct() {
            return "OK";
        }

        @PostMapping("/api/productos/editar/{id}")
        String updateProduct() {
            return "OK";
        }
    }

    @Configuration
    @EnableWebMvc
    @Import(WebMvcConfig.class)
    static class TestConfig {

        @Bean
        AdminUserRepository adminUserRepository() {
            return mock(AdminUserRepository.class);
        }

        @Bean
        StoreContextService storeContextService() {
            return mock(StoreContextService.class);
        }

        @Bean
        SubscriptionAccessService subscriptionAccessService() {
            return mock(SubscriptionAccessService.class);
        }

        @Bean
        AdminTenantAccessInterceptor adminTenantAccessInterceptor(
                AdminUserRepository adminUserRepository,
                StoreContextService storeContextService
        ) {
            return new AdminTenantAccessInterceptor(
                    adminUserRepository,
                    storeContextService
            );
        }

        @Bean
        SubscriptionInterceptor subscriptionInterceptor(
                StoreContextService storeContextService,
                SubscriptionAccessService subscriptionAccessService
        ) {
            return new SubscriptionInterceptor(
                    storeContextService,
                    subscriptionAccessService
            );
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }
}
