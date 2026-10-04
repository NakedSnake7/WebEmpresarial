package com.webempresarial.store.config;

import com.webempresarial.store.service.AdminUserDetailsService; 
import com.webempresarial.store.service.AuthUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final AuthUserDetailsService authUserDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;
    private final AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler;

    public SecurityConfig(
            AuthUserDetailsService authUserDetailsService,
            AdminUserDetailsService adminUserDetailsService,
            AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler
    ) {
        this.authUserDetailsService = authUserDetailsService;
        this.adminUserDetailsService = adminUserDetailsService;
        this.adminAuthenticationSuccessHandler = adminAuthenticationSuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurity(
            HttpSecurity http
    ) throws Exception {

        http
            .securityMatcher(
                "/admin/**",
                "/api/admin/**",
                "/api/knowledge/**"
            )

            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/admin/stripe/connect/**"
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/admin/login",
                    "/admin/activate",
                    "/admin/activate/**"
                )
                .permitAll()

                .requestMatchers(
                    "/admin/stores/**",
                    "/admin/subscriptions/**",
                    "/admin/saas/**",
                    "/admin/platform/**",
                    "/api/admin/platform/**"
                )
                .hasRole("SUPER_ADMIN")

                .requestMatchers(
                    "/admin/users/**",
                    "/admin/billing/**",
                    "/admin/store/settings/**",
                    "/admin/stripe/connect/**",
                    "/api/admin/stripe/connect/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_ADMIN"
                )

                .requestMatchers("/api/knowledge/**")
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )

                .anyRequest()
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )
            )

            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler(
                    adminAuthenticationSuccessHandler
                )
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login")
            )

            .userDetailsService(adminUserDetailsService);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain storeSecurity(
            HttpSecurity http
            
    ) throws Exception {

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/checkout",
                    "/api/billing/checkout",
                    "/api/stripe/**",
                    "/api/leads",
                    "/api/leads/**"
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/index",
                    "/inicio",
                    "/privacy",
                    "/billing/success",
                    "/productos/**",
                    "/products/**",
                    "/producto-detalle/**",
                    "/fragmento-menu",
                    "/fragmento-resenas",
                    "/login",
                    "/themes/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/checkout",
                    "/api/billing/checkout"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/user/me"
                ).permitAll()

                .requestMatchers(
                    "/api/stripe/**",
                    "/api/leads",
                    "/api/leads/**"
                ).permitAll()

                /*
                 * Legacy Commerce administration routes.
                 *
                 * These endpoints still live outside /admin/** and
                 * /api/admin/**, so they must not fall through to the
                 * generic authenticated-user rule.
                 */
                .requestMatchers(
                    "/nuevo",
                    "/editar/**",
                    "/modificar-precios",
                    "/variantes/**",
                    "/api/productos/**",
                    "/api/variantes/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )

                /*
                 * Legacy order administration routes.
                 *
                 * The admin order UI still lives under /orders rather than
                 * /admin/orders, so it must not fall through to the generic
                 * authenticated-user rule.
                 */
                .requestMatchers(
                    "/orders",
                    "/orders/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )

                .requestMatchers(
                    "/cuenta/**",
                    "/pedidos/**"
                ).hasRole("CLIENTE")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/inicio", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
            )

            .userDetailsService(authUserDetailsService);

        return http.build();
    }
}