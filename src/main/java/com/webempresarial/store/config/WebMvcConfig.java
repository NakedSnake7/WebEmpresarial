package com.webempresarial.store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;

@Configuration
public class WebMvcConfig
        implements WebMvcConfigurer {

    private final AdminTenantAccessInterceptor
            adminTenantAccessInterceptor;

    private final SubscriptionInterceptor
            subscriptionInterceptor;

    public WebMvcConfig(
            AdminTenantAccessInterceptor adminTenantAccessInterceptor,
            SubscriptionInterceptor subscriptionInterceptor
    ) {
        this.adminTenantAccessInterceptor =
                adminTenantAccessInterceptor;

        this.subscriptionInterceptor =
                subscriptionInterceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {

        /*
         * =================================================
         * ADMIN TENANT ISOLATION
         * =================================================
         *
         * STORE_ADMIN solo puede operar sobre la tienda
         * asociada a su AdminUser.
         *
         * SUPER_ADMIN conserva acceso cross-tenant.
         */
        registry.addInterceptor(
                        adminTenantAccessInterceptor
                )
                .order(0)
                .addPathPatterns(
                        "/admin/**",
                        "/api/admin/**",
                        "/nuevo",
                        "/editar/**",
                        "/modificar-precios",
                        "/variantes/**",
                        "/api/productos/**",
                        "/api/variantes/**"
                )
                .excludePathPatterns(
                        "/admin/login",
                        "/admin/logout",
                        "/assets/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/admin/activate",
                        "/admin/activate/**",
                        "/favicon.ico"
                );

        /*
         * =================================================
         * SUBSCRIPTION ACCESS
         * =================================================
         */
        registry.addInterceptor(
                subscriptionInterceptor
        )
        .order(10)
        .addPathPatterns(
                "/admin/**",
                "/crm/**",
                "/nuevo",
                "/editar/**",
                "/modificar-precios",
                "/variantes/**",
                "/api/productos/**",
                "/api/variantes/**"
        )
        .excludePathPatterns(
                "/admin/login",
                "/admin/logout",

                "/admin/activate",
                "/admin/activate/**",

                "/admin/billing",
                "/admin/billing/**",
                "/admin/upgrade",
                "/admin/upgrade/**",
                "/api/admin/**",
                "/assets/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**",
                "/favicon.ico"
        );
    }
}