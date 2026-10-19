package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminPermission;

import org.junit.jupiter.api.Test;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority
        .SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityViewAdviceTest {

    private final SecurityViewAdvice advice =
            new SecurityViewAdvice();

    @Test
    void ownerShouldBeDetectedAndSeeBilling() {

        Authentication auth = authentication(
                "ROLE_STORE_OWNER",
                AdminPermission
                        .SUBSCRIPTION_VIEW
                        .authority(),
                AdminPermission
                        .SUBSCRIPTION_MANAGE
                        .authority(),
                AdminPermission
                        .STORE_EDIT
                        .authority(),
                AdminPermission
                        .STRIPE_CONNECT_MANAGE
                        .authority()
        );

        assertThat(
                advice.isStoreOwner(auth)
        ).isTrue();

        assertThat(
                advice.canViewBilling(auth)
        ).isTrue();

        assertThat(
                advice.canManageBilling(auth)
        ).isTrue();

        assertThat(
                advice.canManageStoreSettings(auth)
        ).isTrue();

        assertThat(
                advice.canManageStripeConnect(auth)
        ).isTrue();
    }

    @Test
    void storeAdminShouldViewBillingWithoutManagingSubscription() {

        Authentication auth = authentication(
                "ROLE_STORE_ADMIN",
                AdminPermission
                        .SUBSCRIPTION_VIEW
                        .authority()
        );

        assertThat(
                advice.canViewBilling(auth)
        ).isTrue();

        assertThat(
                advice.canManageBilling(auth)
        ).isFalse();
    }

    @Test
    void analyticsPermissionShouldControlReportsVisibility() {

        Authentication allowed = authentication(
                "ROLE_STORE_ADMIN",
                AdminPermission.ANALYTICS_VIEW.authority()
        );

        Authentication denied = authentication(
                "ROLE_STORE_ADMIN"
        );

        assertThat(
                advice.canViewAnalytics(allowed)
        ).isTrue();

        assertThat(
                advice.canViewAnalytics(denied)
        ).isFalse();
    }

    @Test
    void salesShouldBeDetectedWithoutBillingOrSettingsAccess() {

        Authentication auth = authentication(
                "ROLE_SALES",
                AdminPermission.CRM_VIEW.authority()
        );

        assertThat(
                advice.isSales(auth)
        ).isTrue();

        assertThat(
                advice.canViewBilling(auth)
        ).isFalse();

        assertThat(
                advice.canManageBilling(auth)
        ).isFalse();

        assertThat(
                advice.canManageStoreSettings(auth)
        ).isFalse();

        assertThat(
                advice.canManageStripeConnect(auth)
        ).isFalse();
    }

    @Test
    void brandingPermissionShouldControlLogoManagement() {

        Authentication branding = authentication(
                AdminPermission
                        .STORE_BRANDING_EDIT
                        .authority()
        );

        Authentication storeEdit = authentication(
                AdminPermission
                        .STORE_EDIT
                        .authority()
        );

        assertThat(
                advice.canManageStoreBranding(branding)
        ).isTrue();

        assertThat(
                advice.canManageStoreSettings(branding)
        ).isFalse();

        assertThat(
                advice.canManageStoreBranding(storeEdit)
        ).isFalse();

        assertThat(
                advice.canManageStoreSettings(storeEdit)
        ).isTrue();
    }

    @Test
    void commercialInfoPermissionShouldControlCommercialEditing() {

        Authentication commercial = authentication(
                AdminPermission
                        .STORE_COMMERCIAL_INFO_EDIT
                        .authority()
        );

        Authentication storeEdit = authentication(
                AdminPermission
                        .STORE_EDIT
                        .authority()
        );

        assertThat(
                advice.canManageCommercialInfo(commercial)
        ).isTrue();

        assertThat(
                advice.canManageStoreSettings(commercial)
        ).isFalse();

        assertThat(
                advice.canManageCommercialInfo(storeEdit)
        ).isFalse();

        assertThat(
                advice.canManageStoreSettings(storeEdit)
        ).isTrue();
    }

    @Test
    void paymentMethodPermissionsShouldControlViewAndManageIndependently() {

        Authentication viewOnly = authentication(
                AdminPermission
                        .PAYMENT_METHOD_VIEW
                        .authority()
        );

        Authentication manageOnly = authentication(
                AdminPermission
                        .PAYMENT_METHOD_MANAGE
                        .authority()
        );

        assertThat(
                advice.canViewPaymentMethods(viewOnly)
        ).isTrue();

        assertThat(
                advice.canManagePaymentMethods(viewOnly)
        ).isFalse();

        assertThat(
                advice.canViewPaymentMethods(manageOnly)
        ).isFalse();

        assertThat(
                advice.canManagePaymentMethods(manageOnly)
        ).isTrue();
    }

    @Test
    void categoryManagePermissionShouldBeIndependent() {

        Authentication viewOnly = authentication(
                AdminPermission.CATEGORY_VIEW.authority()
        );

        Authentication manageOnly = authentication(
                AdminPermission.CATEGORY_MANAGE.authority()
        );

        assertThat(
                advice.canManageCategories(viewOnly)
        ).isFalse();

        assertThat(
                advice.canManageCategories(manageOnly)
        ).isTrue();
    }

    @Test
    void brandManagePermissionShouldBeIndependent() {

        Authentication viewOnly = authentication(
                AdminPermission.BRAND_VIEW.authority()
        );

        Authentication manageOnly = authentication(
                AdminPermission.BRAND_MANAGE.authority()
        );

        assertThat(
                advice.canManageBrands(viewOnly)
        ).isFalse();

        assertThat(
                advice.canManageBrands(manageOnly)
        ).isTrue();

        assertThat(
                advice.canManageBrands(null)
        ).isFalse();
    }

    @Test
    void productPermissionsShouldControlActionsIndependently() {

        Authentication viewOnly = authentication(
                AdminPermission.PRODUCT_VIEW.authority()
        );

        Authentication createOnly = authentication(
                AdminPermission.PRODUCT_CREATE.authority()
        );

        Authentication editOnly = authentication(
                AdminPermission.PRODUCT_EDIT.authority()
        );

        Authentication deleteOnly = authentication(
                AdminPermission.PRODUCT_DELETE.authority()
        );

        assertThat(
                advice.canViewProducts(viewOnly)
        ).isTrue();

        assertThat(
                advice.canCreateProducts(viewOnly)
        ).isFalse();

        assertThat(
                advice.canCreateProducts(createOnly)
        ).isTrue();

        assertThat(
                advice.canEditProducts(createOnly)
        ).isFalse();

        assertThat(
                advice.canEditProducts(editOnly)
        ).isTrue();

        assertThat(
                advice.canDeleteProducts(editOnly)
        ).isFalse();

        assertThat(
                advice.canDeleteProducts(deleteOnly)
        ).isTrue();

        assertThat(
                advice.canViewProducts(deleteOnly)
        ).isFalse();
    }

    @Test
    void inventoryPermissionsShouldControlViewAndEditIndependently() {

        Authentication viewOnly = authentication(
                AdminPermission.INVENTORY_VIEW.authority()
        );

        Authentication editOnly = authentication(
                AdminPermission.INVENTORY_EDIT.authority()
        );

        assertThat(
                advice.canViewInventory(viewOnly)
        ).isTrue();

        assertThat(
                advice.canEditInventory(viewOnly)
        ).isFalse();

        assertThat(
                advice.canEditInventory(editOnly)
        ).isTrue();

        assertThat(
                advice.canViewInventory(editOnly)
        ).isFalse();
    }

    @Test
    void reviewPermissionsShouldControlActionsIndependently() {

        Authentication viewOnly = authentication(
                AdminPermission.REVIEW_VIEW.authority()
        );

        Authentication createOnly = authentication(
                AdminPermission.REVIEW_CREATE.authority()
        );

        Authentication editOnly = authentication(
                AdminPermission.REVIEW_EDIT.authority()
        );

        Authentication deleteOnly = authentication(
                AdminPermission.REVIEW_DELETE.authority()
        );

        assertThat(advice.canViewReviews(viewOnly)).isTrue();
        assertThat(advice.canCreateReviews(viewOnly)).isFalse();
        assertThat(advice.canEditReviews(viewOnly)).isFalse();
        assertThat(advice.canDeleteReviews(viewOnly)).isFalse();

        assertThat(advice.canCreateReviews(createOnly)).isTrue();
        assertThat(advice.canViewReviews(createOnly)).isFalse();
        assertThat(advice.canEditReviews(createOnly)).isFalse();
        assertThat(advice.canDeleteReviews(createOnly)).isFalse();

        assertThat(advice.canEditReviews(editOnly)).isTrue();
        assertThat(advice.canViewReviews(editOnly)).isFalse();
        assertThat(advice.canCreateReviews(editOnly)).isFalse();
        assertThat(advice.canDeleteReviews(editOnly)).isFalse();

        assertThat(advice.canDeleteReviews(deleteOnly)).isTrue();
        assertThat(advice.canViewReviews(deleteOnly)).isFalse();
        assertThat(advice.canCreateReviews(deleteOnly)).isFalse();
        assertThat(advice.canEditReviews(deleteOnly)).isFalse();
    }

    @Test
    void orderPermissionsShouldControlActionsIndependently() {

        Authentication viewOnly = authentication(
                AdminPermission.ORDER_VIEW.authority()
        );

        Authentication updateOnly = authentication(
                AdminPermission.ORDER_UPDATE.authority()
        );

        Authentication cancelOnly = authentication(
                AdminPermission.ORDER_CANCEL.authority()
        );

        assertThat(advice.canViewOrders(viewOnly)).isTrue();
        assertThat(advice.canUpdateOrders(viewOnly)).isFalse();
        assertThat(advice.canCancelOrders(viewOnly)).isFalse();

        assertThat(advice.canUpdateOrders(updateOnly)).isTrue();
        assertThat(advice.canViewOrders(updateOnly)).isFalse();
        assertThat(advice.canCancelOrders(updateOnly)).isFalse();

        assertThat(advice.canCancelOrders(cancelOnly)).isTrue();
        assertThat(advice.canViewOrders(cancelOnly)).isFalse();
        assertThat(advice.canUpdateOrders(cancelOnly)).isFalse();
    }

    @Test
    void crmPermissionsShouldControlActionsIndependently() {

        Authentication crmView = authentication(
                AdminPermission.CRM_VIEW.authority()
        );

        Authentication crmEdit = authentication(
                AdminPermission.CRM_EDIT.authority()
        );

        Authentication pipelineView = authentication(
                AdminPermission.PIPELINE_VIEW.authority()
        );

        Authentication pipelineManage = authentication(
                AdminPermission.PIPELINE_MANAGE.authority()
        );

        Authentication proposalView = authentication(
                AdminPermission.PROPOSAL_VIEW.authority()
        );

        Authentication proposalCreate = authentication(
                AdminPermission.PROPOSAL_CREATE.authority()
        );

        Authentication proposalEdit = authentication(
                AdminPermission.PROPOSAL_EDIT.authority()
        );

        assertThat(advice.canViewCrm(crmView)).isTrue();
        assertThat(advice.canEditCrm(crmView)).isFalse();

        assertThat(advice.canEditCrm(crmEdit)).isTrue();
        assertThat(advice.canViewCrm(crmEdit)).isFalse();

        assertThat(
                advice.canViewPipeline(pipelineView)
        ).isTrue();

        assertThat(
                advice.canManagePipeline(pipelineView)
        ).isFalse();

        assertThat(
                advice.canManagePipeline(pipelineManage)
        ).isTrue();

        assertThat(
                advice.canViewPipeline(pipelineManage)
        ).isFalse();

        assertThat(
                advice.canViewProposals(proposalView)
        ).isTrue();

        assertThat(
                advice.canCreateProposals(proposalView)
        ).isFalse();

        assertThat(
                advice.canEditProposals(proposalView)
        ).isFalse();

        assertThat(
                advice.canCreateProposals(proposalCreate)
        ).isTrue();

        assertThat(
                advice.canViewProposals(proposalCreate)
        ).isFalse();

        assertThat(
                advice.canEditProposals(proposalCreate)
        ).isFalse();

        assertThat(
                advice.canEditProposals(proposalEdit)
        ).isTrue();

        assertThat(
                advice.canViewProposals(proposalEdit)
        ).isFalse();

        assertThat(
                advice.canCreateProposals(proposalEdit)
        ).isFalse();
    }

    @Test
    void missingAuthenticationShouldNotGrantCrmPermissions() {

        assertThat(advice.canViewCrm(null)).isFalse();
        assertThat(advice.canEditCrm(null)).isFalse();

        assertThat(
                advice.canViewPipeline(null)
        ).isFalse();

        assertThat(
                advice.canManagePipeline(null)
        ).isFalse();

        assertThat(
                advice.canViewProposals(null)
        ).isFalse();

        assertThat(
                advice.canCreateProposals(null)
        ).isFalse();

        assertThat(
                advice.canEditProposals(null)
        ).isFalse();
    }

    @Test
    void missingAuthenticationShouldNotGrantAnything() {

        assertThat(
                advice.canViewReviews(null)
        ).isFalse();

        assertThat(
                advice.canCreateReviews(null)
        ).isFalse();

        assertThat(
                advice.canEditReviews(null)
        ).isFalse();

        assertThat(
                advice.canDeleteReviews(null)
        ).isFalse();

        assertThat(
                advice.canViewOrders(null)
        ).isFalse();

        assertThat(
                advice.canUpdateOrders(null)
        ).isFalse();

        assertThat(
                advice.canCancelOrders(null)
        ).isFalse();

        assertThat(
                advice.canViewInventory(null)
        ).isFalse();

        assertThat(
                advice.canEditInventory(null)
        ).isFalse();

        assertThat(
                advice.canManageCategories(null)
        ).isFalse();

        assertThat(
                advice.canViewProducts(null)
        ).isFalse();

        assertThat(
                advice.canCreateProducts(null)
        ).isFalse();

        assertThat(
                advice.canEditProducts(null)
        ).isFalse();

        assertThat(
                advice.canDeleteProducts(null)
        ).isFalse();

        assertThat(
                advice.canViewBilling(null)
        ).isFalse();

        assertThat(
                advice.canManageBilling(null)
        ).isFalse();

        assertThat(
                advice.canManageCommercialInfo(null)
        ).isFalse();

        assertThat(
                advice.canManageStoreBranding(null)
        ).isFalse();

        assertThat(
                advice.canViewAnalytics(null)
        ).isFalse();

        assertThat(
                advice.canViewTeam(null)
        ).isFalse();

        assertThat(
                advice.canManageStoreSettings(null)
        ).isFalse();

        assertThat(
                advice.canViewPaymentMethods(null)
        ).isFalse();

        assertThat(
                advice.canManagePaymentMethods(null)
        ).isFalse();

        assertThat(
                advice.canManageStripeConnect(null)
        ).isFalse();
    }

    private Authentication authentication(
            String... authorities
    ) {

        return new UsernamePasswordAuthenticationToken(
                "test@webempresarial.test",
                "password",
                List.of(authorities)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}
