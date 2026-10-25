package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.sidebar.SidebarItemDTO;
import com.webempresarial.store.feature.catalog.PlatformFeatures;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomDomainSidebarContractTest {

    private FeatureAccessService featureAccessService;

    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    private SidebarRegistry registry;

    private Store store;

    @BeforeEach
    void setUp() {

        featureAccessService =
                mock(FeatureAccessService.class);

        currentAdminAuthorizationService =
                mock(CurrentAdminAuthorizationService.class);

        registry =
                new SidebarRegistry(
                        featureAccessService,
                        currentAdminAuthorizationService
                );

        registry.register(
                new PlatformFeatures()
                        .descriptor()
        );

        store = new Store();

        when(
                currentAdminAuthorizationService
                        .hasPermission(
                                AdminPermission.DOMAIN_VIEW
                        )
        ).thenReturn(true);
    }

    @Test
    void basicTenantMustSeeLockedUpgradeItem() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.CUSTOM_DOMAIN
                )
        ).thenReturn(false);

        SidebarItemDTO item =
                customDomainItem();

        assertThat(item.locked())
                .isTrue();

        assertThat(item.url())
                .isEqualTo(
                        "/admin/upgrade?feature=CUSTOM_DOMAIN"
                );
    }

    @Test
    void entitledTenantMustNavigateToDomainPage() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.CUSTOM_DOMAIN
                )
        ).thenReturn(true);

        SidebarItemDTO item =
                customDomainItem();

        assertThat(item.locked())
                .isFalse();

        assertThat(item.url())
                .isEqualTo(
                        "/admin/domains"
                );
    }

    @Test
    void userWithoutDomainViewMustNotSeeItem() {

        when(
                currentAdminAuthorizationService
                        .hasPermission(
                                AdminPermission.DOMAIN_VIEW
                        )
        ).thenReturn(false);

        boolean present =
                registry
                        .sections(
                                store,
                                AdminRole.STORE_ADMIN
                        )
                        .stream()
                        .flatMap(section ->
                                section
                                        .items()
                                        .stream()
                        )
                        .anyMatch(item ->
                                item.feature()
                                        == Feature.CUSTOM_DOMAIN
                        );

        assertThat(present)
                .isFalse();
    }

    private SidebarItemDTO customDomainItem() {

        return registry
                .sections(
                        store,
                        AdminRole.STORE_ADMIN
                )
                .stream()
                .flatMap(section ->
                        section
                                .items()
                                .stream()
                )
                .filter(item ->
                        item.feature()
                                == Feature.CUSTOM_DOMAIN
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "CUSTOM_DOMAIN no apareció "
                                + "en el sidebar"
                        )
                );
    }
}
