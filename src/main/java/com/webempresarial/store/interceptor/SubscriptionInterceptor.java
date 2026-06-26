package com.webempresarial.store.interceptor;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.SubscriptionAccessService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final StoreContextService storeContextService;
    private final SubscriptionAccessService subscriptionAccessService;

    public SubscriptionInterceptor(
            StoreContextService storeContextService,
            SubscriptionAccessService subscriptionAccessService
    ) {
        this.storeContextService = storeContextService;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {

            boolean isSuperAdmin = auth.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (isSuperAdmin) {
                return true;
            }
        }

        Store store = storeContextService.getCurrentStore(request);

        if (store == null) {
            return true;
        }

        Subscription subscription = store.getSubscription();

        if (subscriptionAccessService.canAccessPlatform(subscription)) {
            return true;
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/billing?expired"
        );

        return false;
    }
}