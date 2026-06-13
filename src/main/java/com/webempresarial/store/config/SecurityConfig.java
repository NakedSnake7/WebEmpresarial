package com.webempresarial.store.config;

import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final AuthUserDetailsService authUserDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;

    public SecurityConfig(
            AuthUserDetailsService authUserDetailsService,
            AdminUserDetailsService adminUserDetailsService
    ) {
        this.authUserDetailsService = authUserDetailsService;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {

        http
            .securityMatcher("/admin/**", "/api/admin/**")
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()

                .requestMatchers(
                    "/admin/stores/**",
                    "/admin/subscriptions/**",
                    "/admin/billing/**",
                    "/admin/saas/**"
                ).hasRole("SUPER_ADMIN")

                .requestMatchers(
                    "/admin/store/settings/**",
                    "/api/admin/stripe/connect/**"
                ).hasAnyRole("SUPER_ADMIN", "STORE_ADMIN")

                .anyRequest().hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )
            )

            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
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
            HttpSecurity http,
            PasswordEncoder passwordEncoder
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index", "/inicio", "/privacy",
                    "/productos/**",
                    "/products/**",
                    "/producto-detalle/**",
                    "/fragmento-menu",
                    "/fragmento-resenas",
                    "/login",
                    "/api/checkout",
                    "/api/stripe/**",
                    "/api/user/me",
                    "/api/leads",
                    "/api/leads/**",
                    "/themes/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico"
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