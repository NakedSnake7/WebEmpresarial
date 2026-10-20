package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPermissionServiceTest {

    private final AdminPermissionService service =
            new AdminPermissionService();

    @Test
    void superAdminHasEveryPermission() {

        assertThat(
                service.permissionsFor(AdminRole.SUPER_ADMIN)
        ).containsExactlyInAnyOrder(
                AdminPermission.values()
        );
    }

    @Test
    void ownerCanManageSensitiveStoreOperations() {

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_OWNER,
                        AdminPermission.SUBSCRIPTION_MANAGE
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_OWNER,
                        AdminPermission.STRIPE_CONNECT_MANAGE
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_OWNER,
                        AdminPermission.ROLE_MANAGE
                )
        ).isTrue();

        assertThat(
                service.permissionsFor(
                        AdminRole.STORE_OWNER
                )
        ).contains(
                AdminPermission.REVIEW_VIEW,
                AdminPermission.REVIEW_CREATE,
                AdminPermission.REVIEW_EDIT,
                AdminPermission.REVIEW_DELETE
        );
    }

    @Test
    void storeAdminCanOperateStoreButCannotManageOwnerOnlyActions() {

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_ADMIN,
                        AdminPermission.PRODUCT_EDIT
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_ADMIN,
                        AdminPermission.SUBSCRIPTION_VIEW
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_ADMIN,
                        AdminPermission.SUBSCRIPTION_MANAGE
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_ADMIN,
                        AdminPermission.STRIPE_CONNECT_MANAGE
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_ADMIN,
                        AdminPermission.ROLE_MANAGE
                )
        ).isFalse();

        assertThat(
                service.permissionsFor(
                        AdminRole.STORE_ADMIN
                )
        ).contains(
                AdminPermission.REVIEW_VIEW,
                AdminPermission.REVIEW_CREATE,
                AdminPermission.REVIEW_EDIT,
                AdminPermission.REVIEW_DELETE
        );
    }

    @Test
    void staffCanOperateCatalogInventoryAndOrdersButNotDestructiveActions() {

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_STAFF,
                        AdminPermission.PRODUCT_EDIT
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_STAFF,
                        AdminPermission.INVENTORY_EDIT
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_STAFF,
                        AdminPermission.ORDER_UPDATE
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_STAFF,
                        AdminPermission.PRODUCT_DELETE
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_STAFF,
                        AdminPermission.ORDER_CANCEL
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_STAFF,
                        AdminPermission.CRM_VIEW
                )
        ).isFalse();

        assertThat(
                service.permissionsFor(
                        AdminRole.STORE_STAFF
                )
        ).contains(
                AdminPermission.REVIEW_VIEW,
                AdminPermission.REVIEW_CREATE,
                AdminPermission.REVIEW_EDIT
        )
        .doesNotContain(
                AdminPermission.REVIEW_DELETE
        );
    }

    @Test
    void salesHasCommercialPermissionsWithoutOperationalWriteAccess() {

        assertThat(
                service.hasPermission(
                        AdminRole.SALES,
                        AdminPermission.CRM_EDIT
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.SALES,
                        AdminPermission.PIPELINE_MANAGE
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.SALES,
                        AdminPermission.PROPOSAL_CREATE
                )
        ).isTrue();

        assertThat(
                service.hasPermission(
                        AdminRole.SALES,
                        AdminPermission.PRODUCT_EDIT
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.SALES,
                        AdminPermission.ORDER_UPDATE
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.SALES,
                        AdminPermission.SUBSCRIPTION_VIEW
                )
        ).isFalse();

        assertThat(
                service.permissionsFor(
                        AdminRole.SALES
                )
        ).doesNotContain(
                AdminPermission.REVIEW_VIEW,
                AdminPermission.REVIEW_CREATE,
                AdminPermission.REVIEW_EDIT,
                AdminPermission.REVIEW_DELETE
        );
    }

    @Test
    void tenantIntegrationPermissionsShouldBeLimitedToOwnerAndAdmin() {

        for (AdminPermission permission : new AdminPermission[] {
                AdminPermission.INTEGRATION_VIEW,
                AdminPermission.INTEGRATION_MANAGE
        }) {

            assertThat(
                    service.hasPermission(
                            AdminRole.STORE_OWNER,
                            permission
                    )
            ).isTrue();

            assertThat(
                    service.hasPermission(
                            AdminRole.STORE_ADMIN,
                            permission
                    )
            ).isTrue();

            assertThat(
                    service.hasPermission(
                            AdminRole.STORE_STAFF,
                            permission
                    )
            ).isFalse();

            assertThat(
                    service.hasPermission(
                            AdminRole.SALES,
                            permission
                    )
            ).isFalse();
        }
    }

    @Test
    void onlySuperAdminGetsPlatformPermissions() {

        Arrays.stream(AdminRole.values())
                .filter(role -> role != AdminRole.SUPER_ADMIN)
                .forEach(role ->
                        assertThat(
                                service.hasPermission(
                                        role,
                                        AdminPermission.PLATFORM_STORE_MANAGE
                                )
                        ).isFalse()
                );
    }

    @Test
    void nullRoleOrPermissionNeverGrantsAccess() {

        assertThat(
                service.hasPermission(
                        null,
                        AdminPermission.DASHBOARD_VIEW
                )
        ).isFalse();

        assertThat(
                service.hasPermission(
                        AdminRole.STORE_OWNER,
                        null
                )
        ).isFalse();

        assertThat(
                service.permissionsFor(null)
        ).isEmpty();
    }
}
