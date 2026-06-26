package com.webempresarial.store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.webempresarial.store.interceptor.SubscriptionInterceptor;

@Configuration
public class WebMvcConfig
        implements WebMvcConfigurer {

    private final SubscriptionInterceptor subscriptionInterceptor;

    public WebMvcConfig(
            SubscriptionInterceptor subscriptionInterceptor
    ) {
        this.subscriptionInterceptor =
                subscriptionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(subscriptionInterceptor)
                .addPathPatterns(
                        "/admin/**",
                        "/crm/**"
                )
                .excludePathPatterns(
                        "/admin/login",
                        "/admin/logout",
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