package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminPermission;
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

                /*
                 * Administración del tenant.
                 *
                 * STORE_OWNER y STORE_ADMIN pueden acceder a usuarios
                 * y configuración general de la tienda.
                 */
                /*
                 * Billing del tenant.
                 *
                 * Ver la suscripción y modificarla son permisos
                 * diferentes.
                 *
                 * STORE_ADMIN:
                 *   SUBSCRIPTION_VIEW
                 *
                 * STORE_OWNER:
                 *   SUBSCRIPTION_VIEW
                 *   SUBSCRIPTION_MANAGE
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/billing/checkout",
                    "/admin/billing/portal"
                )
                .hasAuthority(
                    AdminPermission
                        .SUBSCRIPTION_MANAGE
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/billing",
                    "/admin/billing/**"
                )
                .hasAuthority(
                    AdminPermission
                        .SUBSCRIPTION_VIEW
                        .authority()
                )

                /*
                 * Fail closed:
                 * cualquier método futuro de /admin/billing/**
                 * debe declararse explícitamente.
                 */
                .requestMatchers(
                    "/admin/billing",
                    "/admin/billing/**"
                )
                .denyAll()

                /*
                 * Gestión de equipo.
                 *
                 * Cada acción se protege con su permiso real.
                 */

                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/users/nuevo"
                )
                .hasAuthority(
                    AdminPermission
                        .TEAM_INVITE
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/users/guardar"
                )
                .hasAuthority(
                    AdminPermission
                        .TEAM_INVITE
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/users/estado/**"
                )
                .hasAuthority(
                    AdminPermission
                        .TEAM_EDIT
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/users",
                    "/admin/users/"
                )
                .hasAuthority(
                    AdminPermission
                        .TEAM_VIEW
                        .authority()
                )

                /*
                 * Fail closed:
                 * cualquier nueva ruta de equipo deberá
                 * declararse explícitamente.
                 */
                .requestMatchers(
                    "/admin/users/**"
                )
                .denyAll()

                .requestMatchers(
                    "/admin/store/settings/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER",
                    "STORE_ADMIN"
                )

                /*
                 * Stripe Connect modifica la relación financiera entre
                 * la plataforma y la tienda, por lo que queda reservado
                 * al propietario del tenant.
                 */
                .requestMatchers(
                    "/admin/stripe/connect/**",
                    "/api/admin/stripe/connect/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER"
                )

                /*
                 * Todos los perfiles administrativos pueden entrar
                 * al dashboard principal.
                 *
                 * SALES queda deliberadamente fuera del anyRequest()
                 * general para que no obtenga acceso accidental a
                 * módulos operativos no clasificados.
                 */
                .requestMatchers(
                    "/admin",
                    "/admin/dashboard"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER",
                    "STORE_ADMIN",
                    "STORE_STAFF",
                    "SALES"
                )

                .requestMatchers("/api/knowledge/**")
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )

                /*
                 * Acceso administrativo operacional general.
                 *
                 * SALES no entra aquí: sus módulos deben declararse
                 * explícitamente.
                 */
                .anyRequest()
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER",
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
                            "/health",
                            "/privacy",
                            "/billing/success",
                            "/gracias",
                            "/checkout-cancel",
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
                    "STORE_OWNER",
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
                    "STORE_OWNER",
                    "STORE_ADMIN",
                    "STORE_STAFF"
                )

                /*
                 * CRM administration routes.
                 *
                 * CRM views and APIs contain tenant commercial data and
                 * administrative operations, so authenticated storefront
                 * customers must not reach them.
                 */
                .requestMatchers(
                    "/crm/**",
                    "/api/crm/**",
                    "/admin/leads",
                    "/admin/leads/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER",
                    "STORE_ADMIN",
                    "SALES"
                )

                /*
                 * Legacy review administration routes.
                 */
                .requestMatchers(
                    "/resenas",
                    "/resenas/**"
                )
                .hasAnyRole(
                    "SUPER_ADMIN",
                    "STORE_OWNER",
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