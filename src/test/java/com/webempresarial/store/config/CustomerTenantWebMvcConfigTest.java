package com.webempresarial.store.config;

import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.CustomerTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.AuthUserRepository;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.SubscriptionAccessService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.test.context.junit.jupiter
        .SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup
        .MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(
        CustomerTenantWebMvcConfigTest.TestConfig.class
)
@WebAppConfiguration
class CustomerTenantWebMvcConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private StoreContextService storeContextService;

    @Autowired
    private TestController testController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                webAppContextSetup(context)
                        .build();

        reset(
                authUserRepository,
                storeContextService
        );

        testController.reset();

        Store credentialStore = store(10L);
        Store requestStore = store(20L);

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setEmail("cliente@example.com");
        cliente.setStore(credentialStore);

        AuthUser authUser = new AuthUser();
        authUser.setId(200L);
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(true);
        authUser.setCliente(cliente);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                requestStore
                        )
        ).thenReturn(Optional.empty());

        when(
                storeContextService.getCurrentStore(
                        any(HttpServletRequest.class)
                )
        ).thenReturn(requestStore);

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "cliente@example.com",
                                "N/A",
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENTE"
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
    void customerAccountPathShouldRejectDifferentTenant()
            throws Exception {

        mockMvc.perform(
                get("/cuenta/test")
        )
        .andExpect(status().isForbidden());

        assertThat(
                testController.invocations()
        ).isZero();
    }

    @Test
    void customerOrdersPathShouldRejectDifferentTenant()
            throws Exception {

        mockMvc.perform(
                get("/pedidos/test")
        )
        .andExpect(status().isForbidden());

        assertThat(
                testController.invocations()
        ).isZero();
    }

    @Test
    void customerApiUserPathShouldRejectDifferentTenant()
            throws Exception {

        mockMvc.perform(
                get("/api/user/me")
        )
        .andExpect(status().isForbidden());

        assertThat(
                testController.invocations()
        ).isZero();
    }

    private static Store store(Long id) {
        Store store = new Store();
        store.setId(id);
        return store;
    }

    @RestController
    static class TestController {

        private final AtomicInteger invocations =
                new AtomicInteger();

        @GetMapping("/cuenta/test")
        String account() {
            invocations.incrementAndGet();
            return "OK";
        }

        @GetMapping("/pedidos/test")
        String orders() {
            invocations.incrementAndGet();
            return "OK";
        }

        @GetMapping("/api/user/me")
        String userMe() {
            invocations.incrementAndGet();
            return "OK";
        }

        int invocations() {
            return invocations.get();
        }

        void reset() {
            invocations.set(0);
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
        AuthUserRepository authUserRepository() {
            return mock(AuthUserRepository.class);
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
        CustomerTenantAccessInterceptor customerTenantAccessInterceptor(
                AuthUserRepository authUserRepository,
                StoreContextService storeContextService
        ) {
            return new CustomerTenantAccessInterceptor(
                    authUserRepository,
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
