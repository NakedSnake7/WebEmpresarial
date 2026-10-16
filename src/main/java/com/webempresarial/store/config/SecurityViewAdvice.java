package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminPermission;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityViewAdvice {

    @ModelAttribute("isSuperAdmin")
    public boolean isSuperAdmin(Authentication auth) {
        return hasRole(auth, "ROLE_SUPER_ADMIN");
    }

    @ModelAttribute("isStoreOwner")
    public boolean isStoreOwner(Authentication auth) {
        return hasRole(auth, "ROLE_STORE_OWNER");
    }

    @ModelAttribute("isStoreAdmin")
    public boolean isStoreAdmin(Authentication auth) {
        return hasRole(auth, "ROLE_STORE_ADMIN");
    }

    @ModelAttribute("isStoreStaff")
    public boolean isStoreStaff(Authentication auth) {
        return hasRole(auth, "ROLE_STORE_STAFF");
    }

    @ModelAttribute("isSales")
    public boolean isSales(Authentication auth) {
        return hasRole(auth, "ROLE_SALES");
    }

    @ModelAttribute("canViewBilling")
    public boolean canViewBilling(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.SUBSCRIPTION_VIEW.authority()
        );
    }

    @ModelAttribute("canManageBilling")
    public boolean canManageBilling(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.SUBSCRIPTION_MANAGE.authority()
        );
    }

    @ModelAttribute("canViewAnalytics")
    public boolean canViewAnalytics(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.ANALYTICS_VIEW.authority()
        );
    }

    @ModelAttribute("canViewTeam")
    public boolean canViewTeam(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.TEAM_VIEW.authority()
        );
    }

    @ModelAttribute("canInviteTeam")
    public boolean canInviteTeam(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.TEAM_INVITE.authority()
        );
    }

    @ModelAttribute("canEditTeam")
    public boolean canEditTeam(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.TEAM_EDIT.authority()
        );
    }

    @ModelAttribute("canManageStoreSettings")
    public boolean canManageStoreSettings(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.STORE_EDIT.authority()
        );
    }

    @ModelAttribute("canViewPaymentMethods")
    public boolean canViewPaymentMethods(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PAYMENT_METHOD_VIEW.authority()
        );
    }

    @ModelAttribute("canManagePaymentMethods")
    public boolean canManagePaymentMethods(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PAYMENT_METHOD_MANAGE.authority()
        );
    }

    @ModelAttribute("canManageStripeConnect")
    public boolean canManageStripeConnect(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.STRIPE_CONNECT_MANAGE.authority()
        );
    }

    private boolean hasRole(
            Authentication auth,
            String role
    ) {
        return hasAuthority(auth, role);
    }

    private boolean hasAuthority(
            Authentication auth,
            String authority
    ) {

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities()
                .stream()
                .anyMatch(granted ->
                        authority.equals(
                                granted.getAuthority()
                        )
                );
    }
}
