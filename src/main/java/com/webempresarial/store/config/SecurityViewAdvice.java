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

    @ModelAttribute("canViewReviews")
    public boolean canViewReviews(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.REVIEW_VIEW.authority()
        );
    }

    @ModelAttribute("canCreateReviews")
    public boolean canCreateReviews(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.REVIEW_CREATE.authority()
        );
    }

    @ModelAttribute("canEditReviews")
    public boolean canEditReviews(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.REVIEW_EDIT.authority()
        );
    }

    @ModelAttribute("canDeleteReviews")
    public boolean canDeleteReviews(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.REVIEW_DELETE.authority()
        );
    }

    @ModelAttribute("canViewOrders")
    public boolean canViewOrders(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.ORDER_VIEW.authority()
        );
    }

    @ModelAttribute("canUpdateOrders")
    public boolean canUpdateOrders(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.ORDER_UPDATE.authority()
        );
    }

    @ModelAttribute("canCancelOrders")
    public boolean canCancelOrders(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.ORDER_CANCEL.authority()
        );
    }

    @ModelAttribute("canViewInventory")
    public boolean canViewInventory(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.INVENTORY_VIEW.authority()
        );
    }

    @ModelAttribute("canEditInventory")
    public boolean canEditInventory(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.INVENTORY_EDIT.authority()
        );
    }

    @ModelAttribute("canManageCategories")
    public boolean canManageCategories(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.CATEGORY_MANAGE.authority()
        );
    }

    @ModelAttribute("canManageBrands")
    public boolean canManageBrands(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.BRAND_MANAGE.authority()
        );
    }

    @ModelAttribute("canViewProducts")
    public boolean canViewProducts(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PRODUCT_VIEW.authority()
        );
    }

    @ModelAttribute("canCreateProducts")
    public boolean canCreateProducts(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PRODUCT_CREATE.authority()
        );
    }

    @ModelAttribute("canEditProducts")
    public boolean canEditProducts(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PRODUCT_EDIT.authority()
        );
    }

    @ModelAttribute("canDeleteProducts")
    public boolean canDeleteProducts(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PRODUCT_DELETE.authority()
        );
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

    @ModelAttribute("canViewCrm")
    public boolean canViewCrm(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.CRM_VIEW.authority()
        );
    }

    @ModelAttribute("canEditCrm")
    public boolean canEditCrm(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.CRM_EDIT.authority()
        );
    }

    @ModelAttribute("canViewPipeline")
    public boolean canViewPipeline(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PIPELINE_VIEW.authority()
        );
    }

    @ModelAttribute("canManagePipeline")
    public boolean canManagePipeline(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PIPELINE_MANAGE.authority()
        );
    }

    @ModelAttribute("canViewProposals")
    public boolean canViewProposals(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PROPOSAL_VIEW.authority()
        );
    }

    @ModelAttribute("canCreateProposals")
    public boolean canCreateProposals(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PROPOSAL_CREATE.authority()
        );
    }

    @ModelAttribute("canEditProposals")
    public boolean canEditProposals(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.PROPOSAL_EDIT.authority()
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

    @ModelAttribute("canManageStoreBranding")
    public boolean canManageStoreBranding(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.STORE_BRANDING_EDIT.authority()
        );
    }

    @ModelAttribute("canManageCommercialInfo")
    public boolean canManageCommercialInfo(Authentication auth) {
        return hasAuthority(
                auth,
                AdminPermission.STORE_COMMERCIAL_INFO_EDIT.authority()
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
