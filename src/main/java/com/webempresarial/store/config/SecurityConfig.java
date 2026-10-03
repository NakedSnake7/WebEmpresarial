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
                    "/api/checkout"
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