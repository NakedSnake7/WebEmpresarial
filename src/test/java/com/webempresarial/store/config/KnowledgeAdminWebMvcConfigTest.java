package com.webempresarial.store.config;

import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup
        .MockMvcBuilders.webAppContextSetup;

@SpringJUnitConfig(
        KnowledgeAdminWebMvcConfigTest.TestConfig.class
)
@WebAppConfiguration
class KnowledgeAdminWebMvcConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private StoreContextService storeContextService;

    @Autowired
    private SubscriptionAccessService subscriptionAccessService;

    @Autowired
    private TestController testController;

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

        testController.reset();

        Store adminStore = mock(Store.class);
        Store requestStore = mock(Store.class);
        AdminUser admin = mock(AdminUser.class);

        when(
                adminUserRepository.findByEmail(
                        "admin@stride.test"
                )
        ).thenReturn(Optional.of(admin));

        when(admin.isEnabled())
                .thenReturn(true);

        when(admin.getRole())
                .thenReturn(AdminRole.STORE_ADMIN);

        when(admin.getStore())
                .thenReturn(adminStore);

        when(adminStore.getId())
                .thenReturn(3L);

        when(requestStore.getId())
                .thenReturn(4L);

        when(
                storeContextService.getCurrentStore(
                        any(HttpServletRequest.class)
                )
        ).thenReturn(requestStore);

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
    void knowledgeApiShouldRejectAdminFromDifferentTenant()
            throws Exception {

        mockMvc.perform(
                get("/api/knowledge/test")
        )
        .andExpect(status().isForbidden());

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );

        assertThat(
                testController.invocations()
        ).isZero();
    }

    @RestController
    static class TestController {

        private final AtomicInteger invocations =
                new AtomicInteger();

        @GetMapping("/api/knowledge/test")
        String knowledge() {
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
