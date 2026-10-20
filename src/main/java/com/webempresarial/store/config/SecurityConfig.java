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
                    "/api/admin/saas",
                    "/api/admin/saas/**",
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

                /*
                 * CRM lead administration.
                 *
                 * /admin/leads/** belongs to this SecurityFilterChain
                 * because adminSecurity matches /admin/**.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/leads",
                    "/admin/leads/**"
                )
                .hasAuthority(
                    AdminPermission
                        .CRM_VIEW
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/leads/**"
                )
                .hasAuthority(
                    AdminPermission
                        .CRM_EDIT
                        .authority()
                )

                /*
                 * Fail closed:
                 * cualquier método futuro bajo /admin/leads/**
                 * requiere clasificación explícita.
                 */
                .requestMatchers(
                    "/admin/leads",
                    "/admin/leads/**"
                )
                .denyAll()

                /*
                 * CRM store configuration.
                 *
                 * These routes configure tenant-level CRM behavior,
                 * so commercial users such as SALES must not inherit
                 * access only because they have CRM_EDIT.
                 */
                .requestMatchers(
                    "/admin/store/crm",
                    "/admin/store/crm/**"
                )
                .hasAuthority(
                    AdminPermission
                        .STORE_EDIT
                        .authority()
                )

                /*
                 * Store settings.
                 *
                 * La pantalla reúne varias responsabilidades, pero las
                 * operaciones independientes se protegen con su permiso
                 * específico.
                 */

                /*
                 * Branding: logo de la tienda.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/store/settings/logo",
                    "/admin/store/settings/logo/delete"
                )
                .hasAuthority(
                    AdminPermission
                        .STORE_BRANDING_EDIT
                        .authority()
                )

                /*
                 * Métodos de pago: transferencia SPEI.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/store/settings/transfer"
                )
                .hasAuthority(
                    AdminPermission
                        .PAYMENT_METHOD_MANAGE
                        .authority()
                )

                /*
                 * Configuración general e información comercial.
                 *
                 * El controlador aplica granularidad por campo:
                 * STORE_EDIT modifica configuración general;
                 * STORE_BRANDING_EDIT modifica identidad visual;
                 * STORE_COMMERCIAL_INFO_EDIT modifica datos comerciales;
                 * WHITE_LABEL_MANAGE modifica CSS, JS y tracking Premium.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/store/settings"
                )
                .hasAnyAuthority(
                    AdminPermission
                        .STORE_EDIT
                        .authority(),
                    AdminPermission
                        .STORE_BRANDING_EDIT
                        .authority(),
                    AdminPermission
                        .STORE_COMMERCIAL_INFO_EDIT
                        .authority(),
                    AdminPermission
                        .WHITE_LABEL_MANAGE
                        .authority()
                )

                /*
                 * Configuración operacional de tienda.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/store/settings/shipping"
                )
                .hasAuthority(
                    AdminPermission
                        .STORE_EDIT
                        .authority()
                )

                /*
                 * Lectura de configuración.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/store/settings",
                    "/admin/store/settings/"
                )
                .hasAuthority(
                    AdminPermission
                        .STORE_VIEW
                        .authority()
                )

                /*
                 * Fail closed:
                 * cualquier nueva operación de Store Settings deberá
                 * recibir un permiso explícito.
                 */
                .requestMatchers(
                    "/admin/store/settings",
                    "/admin/store/settings/**"
                )
                .denyAll()

                /*
                 * Stripe Connect.
                 *
                 * Modifica la relación financiera entre la plataforma
                 * y la tienda, por lo que requiere un permiso explícito.
                 *
                 * El plan se valida independientemente dentro de los
                 * controllers mediante Feature.STRIPE_CONNECT.
                 */

                /*
                 * Inicio de onboarding.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/admin/stripe/connect/onboarding"
                )
                .hasAuthority(
                    AdminPermission
                        .STRIPE_CONNECT_MANAGE
                        .authority()
                )

                /*
                 * Callbacks del onboarding.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/stripe/connect/return",
                    "/admin/stripe/connect/refresh"
                )
                .hasAuthority(
                    AdminPermission
                        .STRIPE_CONNECT_MANAGE
                        .authority()
                )

                /*
                 * Fail closed:
                 * cualquier futura operación de Stripe Connect debe
                 * recibir autorización explícita.
                 */
                .requestMatchers(
                    "/admin/stripe/connect",
                    "/admin/stripe/connect/**",
                    "/api/admin/stripe/connect",
                    "/api/admin/stripe/connect/**"
                )
                .denyAll()

                /*
                 * Catalog product listing.
                 *
                 * /admin/productos belongs to adminSecurity because it
                 * already lives under /admin/**.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/productos"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_VIEW
                        .authority()
                )

                /*
                 * Fail closed for unsupported methods on the product list.
                 */
                .requestMatchers(
                    "/admin/productos"
                )
                .denyAll()

                /*
                 * Inventory administration.
                 *
                 * All these URLs belong to adminSecurity because they live
                 * under /admin/**.
                 */

                /*
                 * Inventory mutations.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/admin/productos/*/inventory/adjust",
                    "/admin/inventory/alerts/*/acknowledge"
                )
                .hasAuthority(
                    AdminPermission
                        .INVENTORY_EDIT
                        .authority()
                )

                /*
                 * Inventory reads: dashboard, product stock detail,
                 * alerts and Kardex views.
                 *
                 * FeatureAccessService independently distinguishes
                 * INVENTORY from INVENTORY_KARDEX.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/inventory",
                    "/admin/inventory/**",
                    "/admin/productos/*/inventory"
                )
                .hasAuthority(
                    AdminPermission
                        .INVENTORY_VIEW
                        .authority()
                )

                /*
                 * Fail closed for future inventory operations.
                 */
                .requestMatchers(
                    "/admin/inventory",
                    "/admin/inventory/**",
                    "/admin/productos/*/inventory",
                    "/admin/productos/*/inventory/**"
                )
                .denyAll()

                /*
                 * Order exports.
                 *
                 * These URLs belong to adminSecurity because they are under
                 * /admin/**. Exporting is considered an alternate view of
                 * order data, not a separate analytics entitlement.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/orders/excel",
                    "/admin/orders/pdf",
                    "/admin/reports/products/excel",
                    "/admin/reports/products/pdf"
                )
                .hasAuthority(
                    AdminPermission
                        .ORDER_VIEW
                        .authority()
                )

                /*
                 * Platform automation diagnostics.
                 *
                 * Kernel registry and execution history are global
                 * platform diagnostics. They are not tenant automation
                 * features and AutomationExecution is not tenant-scoped.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/admin/automations/kernel",
                    "/admin/automations/history",
                    "/admin/automations/history/**"
                )
                .hasRole("SUPER_ADMIN")

                /*
                 * Fail closed:
                 * the tenant-facing /admin/automations module is not
                 * implemented yet and any future operation must receive
                 * an explicit authorization rule.
                 */
                .requestMatchers(
                    "/admin/automations",
                    "/admin/automations/**"
                )
                .denyAll()

                /*
                 * Custom domains.
                 *
                 * El módulo tenant-facing todavía no está implementado.
                 * DOMAIN_VIEW y DOMAIN_MANAGE permanecen reservados para
                 * cuando existan operaciones reales de dominio.
                 */
                .requestMatchers(
                    "/admin/domains",
                    "/admin/domains/**"
                )
                .denyAll()

                /*
                 * Integrations / API Access.
                 *
                 * El módulo tenant-facing todavía no está implementado.
                 * INTEGRATION_VIEW e INTEGRATION_MANAGE permanecen reservados
                 * hasta que existan operaciones reales de integraciones/API.
                 *
                 * Fail closed:
                 * cualquier futura ruta bajo /admin/api debe recibir una
                 * autorización explícita antes de ser accesible.
                 */
                .requestMatchers(
                    "/admin/api",
                    "/admin/api/**"
                )
                .denyAll()

                /*
                 * Coupons.
                 *
                 * El módulo administrativo todavía no está implementado.
                 * COUPONS permanece registrado como feature PRO para
                 * Billing/Upgrade, pero ninguna operación administrativa
                 * debe quedar accesible hasta existir un contrato real.
                 */
                .requestMatchers(
                    "/admin/coupons",
                    "/admin/coupons/**"
                )
                .denyAll()

                /*
                 * Email Marketing.
                 *
                 * El módulo tenant-facing todavía no está implementado.
                 * EMAIL_MARKETING permanece como feature PREMIUM para
                 * Billing/Upgrade, pero cualquier futura operación debe
                 * recibir autorización explícita antes de ser accesible.
                 */
                .requestMatchers(
                    "/admin/email-marketing",
                    "/admin/email-marketing/**"
                )
                .denyAll()

                /*
                 * WhatsApp Automation.
                 *
                 * El módulo tenant-facing todavía no está implementado.
                 * WHATSAPP_AUTOMATION permanece como feature PREMIUM para
                 * Billing/Upgrade, pero cualquier futura operación debe
                 * recibir autorización explícita antes de ser accesible.
                 *
                 * Los enlaces wa.me, datos de leads y configuración manual
                 * de WhatsApp no forman parte de esta feature automática.
                 */
                .requestMatchers(
                    "/admin/whatsapp",
                    "/admin/whatsapp/**"
                )
                .denyAll()

                /*
                 * Dashboard principal.
                 *
                 * El acceso depende de DASHBOARD_VIEW, no del nombre
                 * concreto del rol. SALES sigue pudiendo acceder porque
                 * su rol recibe explícitamente este permiso.
                 */
                .requestMatchers(
                    "/admin",
                    "/admin/dashboard"
                )
                .hasAuthority(
                    AdminPermission
                        .DASHBOARD_VIEW
                        .authority()
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
                 * Legacy catalog administration routes.
                 *
                 * These endpoints still live outside /admin/**, so their
                 * permissions belong to this SecurityFilterChain.
                 */

                /*
                 * Product creation.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/nuevo"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_CREATE
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/nuevo"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_CREATE
                        .authority()
                )

                /*
                 * Product editing views.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/editar/*",
                    "/modificar-precios"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_EDIT
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/editar/*"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_EDIT
                        .authority()
                )

                /*
                 * Variant stock belongs to inventory administration,
                 * not to commercial product editing.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/variantes/*/stock"
                )
                .hasAuthority(
                    AdminPermission
                        .INVENTORY_EDIT
                        .authority()
                )

                /*
                 * Product API reads.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/productos",
                    "/api/productos/*"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_VIEW
                        .authority()
                )

                /*
                 * Editing a product image is an edit operation.
                 */
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/productos/*/eliminar-imagen/*"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_EDIT
                        .authority()
                )

                /*
                 * Deleting the product itself is deliberately more
                 * privileged than editing it.
                 */
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/productos/*"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_DELETE
                        .authority()
                )

                /*
                 * Product and price mutations.
                 *
                 * toggleCategoria changes product visibility by category;
                 * it does not administer the category entity itself.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/productos/editar/*",
                    "/api/productos/togglePromocion",
                    "/api/productos/toggleVisibility",
                    "/api/productos/toggleCategoria",
                    "/api/productos/actualizarPrecio",
                    "/api/productos/ajustarPrecioCategoria",
                    "/api/variantes/actualizarPrecio"
                )
                .hasAuthority(
                    AdminPermission
                        .PRODUCT_EDIT
                        .authority()
                )

                /*
                 * Fail closed:
                 * future catalog operations require explicit classification.
                 */
                .requestMatchers(
                    "/subirProducto",
                    "/nuevo",
                    "/editar/**",
                    "/modificar-precios",
                    "/variantes/**",
                    "/api/productos",
                    "/api/productos/**",
                    "/api/variantes",
                    "/api/variantes/**"
                )
                .denyAll()

                /*
                 * Order administration.
                 *
                 * /orders/** still lives outside /admin/**, therefore
                 * permissions must be enforced in this filter chain.
                 */

                /*
                 * Cancellation is deliberately more privileged than normal
                 * order processing because it changes order lifecycle and
                 * restores inventory.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/orders/*/cancel"
                )
                .hasAuthority(
                    AdminPermission
                        .ORDER_CANCEL
                        .authority()
                )

                /*
                 * Day-to-day order processing.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/orders/*/confirm-payment",
                    "/orders/update-shipping",
                    "/orders/*/status-ajax"
                )
                .hasAuthority(
                    AdminPermission
                        .ORDER_UPDATE
                        .authority()
                )

                /*
                 * Order listing and detail.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/orders",
                    "/orders/**"
                )
                .hasAuthority(
                    AdminPermission
                        .ORDER_VIEW
                        .authority()
                )

                /*
                 * Fail closed:
                 * new order administration operations must receive an
                 * explicit permission before becoming accessible.
                 */
                .requestMatchers(
                    "/orders",
                    "/orders/**"
                )
                .denyAll()

                /*
                 * CRM administration routes.
                 *
                 * Authorization is permission based.
                 *
                 * Role -> AdminPermission is resolved when the
                 * authenticated AdminUser is loaded.
                 *
                 * Plan access remains independently enforced by
                 * FeatureAccessService inside the CRM controllers.
                 */

                /*
                 * Proposals.
                 *
                 * These patterns MUST appear before the generic
                 * /api/crm/leads/** rules below.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/crm/leads/*/proposals",
                    "/api/crm/proposals/*/pdf"
                )
                .hasAuthority("PERM_PROPOSAL_VIEW")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/crm/leads/*/proposals"
                )
                .hasAuthority("PERM_PROPOSAL_CREATE")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/crm/proposals/**"
                )
                .hasAuthority("PERM_PROPOSAL_EDIT")

                /*
                 * Pipeline.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/crm/pipeline",
                    "/api/crm/pipeline",
                    "/api/crm/pipeline/**"
                )
                .hasAuthority("PERM_PIPELINE_VIEW")

                /*
                 * CRM analytics / reports.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/crm/reports"
                )
                .hasAuthority("PERM_ANALYTICS_VIEW")

                /*
                 * Shared lead reads used by the CRM and Pipeline.
                 *
                 * Pipeline cards and drawer consume these two endpoints,
                 * so PIPELINE_VIEW must be sufficient for read-only use.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/crm/leads",
                    "/api/crm/leads/*"
                )
                .hasAnyAuthority(
                    "PERM_CRM_VIEW",
                    "PERM_PIPELINE_VIEW"
                )

                /*
                 * CRM read operations.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/crm/dashboard",
                    "/crm/leads/**",
                    "/crm/tasks",
                    "/api/crm/dashboard",
                    "/api/crm/dashboard/**",
                    "/api/crm/leads",
                    "/api/crm/leads/**",
                    "/api/crm/tasks",
                    "/api/crm/tasks/**"
                )
                .hasAuthority("PERM_CRM_VIEW")

                /*
                 * CRM write operations.
                 *
                 * TASKS currently belong to CRM_EDIT because there
                 * is no independent TASK permission family.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/crm/leads/**"
                )
                .hasAuthority("PERM_CRM_EDIT")

                /*
                 * Lead stage/status is shared by general CRM editing
                 * and Pipeline management.
                 *
                 * This rule MUST remain before the generic
                 * /api/crm/leads/** PATCH matcher.
                 */
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/crm/leads/*/status"
                )
                .hasAnyAuthority(
                    "PERM_CRM_EDIT",
                    "PERM_PIPELINE_MANAGE"
                )

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/crm/leads/**",
                    "/api/crm/tasks/**"
                )
                .hasAuthority("PERM_CRM_EDIT")

                /*
                 * Fail closed.
                 *
                 * A newly introduced CRM endpoint must receive an
                 * explicit permission before becoming accessible.
                 */
                .requestMatchers(
                    "/crm/**",
                    "/api/crm/**"
                )
                .denyAll()

                /*
                 * Review administration.
                 */
                .requestMatchers(
                    HttpMethod.GET,
                    "/resenas"
                )
                .hasAuthority(
                    AdminPermission
                        .REVIEW_VIEW
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.GET,
                    "/resenas/nueva"
                )
                .hasAuthority(
                    AdminPermission
                        .REVIEW_CREATE
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/resenas/nueva"
                )
                .hasAuthority(
                    AdminPermission
                        .REVIEW_CREATE
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.PUT,
                    "/resenas/*",
                    "/resenas/*/imagen"
                )
                .hasAuthority(
                    AdminPermission
                        .REVIEW_EDIT
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/resenas/*/imagen"
                )
                .hasAuthority(
                    AdminPermission
                        .REVIEW_EDIT
                        .authority()
                )

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/resenas/eliminar/*"
                )
                .hasAuthority(
                    AdminPermission
                        .REVIEW_DELETE
                        .authority()
                )

                /*
                 * Fail closed.
                 *
                 * Any new review administration endpoint must receive
                 * an explicit permission before becoming accessible.
                 */
                .requestMatchers(
                    "/resenas",
                    "/resenas/**"
                )
                .denyAll()

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