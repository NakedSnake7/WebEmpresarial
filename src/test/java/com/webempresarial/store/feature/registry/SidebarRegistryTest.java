package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.sidebar.SidebarItemDTO;
import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SidebarRegistryTest {

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    private SidebarRegistry sidebarRegistry;
    private Store store;

    @BeforeEach
    void setUp() {

        sidebarRegistry =
                new SidebarRegistry(
                        featureAccessService,
                        currentAdminAuthorizationService
                );

        store = new Store();
        store.setId(3L);
    }

    @Test
    void shouldShowMultiUserForStoreAdminWhenFeatureAvailable() {

        registerPlatformSection();

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.TEAM_VIEW
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.MULTI_USER
                )
        ).thenReturn(true);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_ADMIN
                );

        assertThat(sections)
                .hasSize(1);

        assertThat(sections.get(0).items())
                .extracting(SidebarItemDTO::title)
                .containsExactly("Usuarios");

        SidebarItemDTO item =
                sections.get(0).items().get(0);

        assertThat(item.locked())
                .isFalse();

        assertThat(item.url())
                .isEqualTo("/admin/users");
    }

    @Test
    void shouldHideMultiUserWithoutTeamViewPermissionEvenWhenFeatureAvailable() {

        registerPlatformSection();

        /*
         * MULTI_USER está disponible por plan,
         * pero STORE_STAFF no tiene permiso
         * para administrarlo.
         *
         * El registry debe filtrar por TEAM_VIEW antes
         * de convertir el item.
         */
        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_STAFF
                );

        assertThat(sections)
                .isEmpty();
    }

    @Test
    void shouldShowLockedMultiUserForStoreAdminWhenFeatureUnavailable() {

        registerPlatformSection();

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.TEAM_VIEW
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.MULTI_USER
                )
        ).thenReturn(false);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_ADMIN
                );

        assertThat(sections)
                .hasSize(1);

        SidebarItemDTO item =
                sections.get(0).items().get(0);

        assertThat(item.title())
                .isEqualTo("Usuarios");

        assertThat(item.locked())
                .isTrue();

        assertThat(item.icon())
                .isEqualTo("🔒");

        assertThat(item.url())
                .isEqualTo(
                        "/admin/upgrade?feature=MULTI_USER"
                );

        assertThat(item.badge())
                .isEqualTo("Upgrade");
    }

    @Test
    void shouldShowOperationalFeatureForStoreStaff() {

        registerOperationalSection();

        when(
                featureAccessService.canUse(
                        store,
                        Feature.PRODUCTS
                )
        ).thenReturn(true);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_STAFF
                );

        assertThat(sections)
                .hasSize(1);

        SidebarItemDTO item =
                sections.get(0).items().get(0);

        assertThat(item.title())
                .isEqualTo("Productos");

        assertThat(item.locked())
                .isFalse();

        assertThat(item.url())
                .isEqualTo("/admin/productos");
    }

    @Test
    void shouldRemoveSectionWhenRoleCannotSeeAnyItems() {

        registerPlatformSection();

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_STAFF
                );

        assertThat(sections)
                .isEmpty();
    }

    @Test
    void salesShouldSeeAnalyticsWhenPermissionAndFeatureAvailable() {

        registerAnalyticsSection();

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ANALYTICS
                )
        ).thenReturn(true);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.SALES
                );

        assertThat(sections)
                .hasSize(1);

        SidebarItemDTO item =
                sections.get(0).items().get(0);

        assertThat(item.title())
                .isEqualTo("Analytics");

        assertThat(item.locked())
                .isFalse();

        assertThat(item.url())
                .isEqualTo("/crm/reports");
    }

    @Test
    void salesShouldSeeLockedAnalyticsWhenPermissionButFeatureUnavailable() {

        registerAnalyticsSection();

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ANALYTICS
                )
        ).thenReturn(false);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.SALES
                );

        assertThat(sections)
                .hasSize(1);

        SidebarItemDTO item =
                sections.get(0).items().get(0);

        assertThat(item.locked())
                .isTrue();

        assertThat(item.url())
                .isEqualTo(
                        "/admin/upgrade?feature=ANALYTICS"
                );
    }

    @Test
    void salesShouldNotSeeAnalyticsWithoutPermission() {

        registerAnalyticsSection();

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(false);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.SALES
                );

        assertThat(sections)
                .isEmpty();
    }

    @Test
    void storeStaffShouldNotSeeAnalyticsWithoutPermission() {

        registerAnalyticsSection();

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(false);

        List<SidebarSectionDTO> sections =
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_STAFF
                );

        assertThat(sections)
                .isEmpty();
    }

    private void registerAnalyticsSection() {

        PlatformModuleDescriptor module =
                PlatformModuleDescriptor
                        .builder("Platform")
                        .sidebarSection(
                                SidebarSectionDefinition
                                        .builder(
                                                "Plataforma",
                                                "🧩"
                                        )
                                        .item(
                                                "Analytics",
                                                "📈",
                                                "/crm/reports",
                                                Feature.ANALYTICS,
                                                AdminPermission.ANALYTICS_VIEW
                                        )
                                        .build()
                        )
                        .build();

        sidebarRegistry.register(module);
    }

    private void registerPlatformSection() {

        PlatformModuleDescriptor module =
                PlatformModuleDescriptor
                        .builder("Platform")
                        .sidebarSection(
                                SidebarSectionDefinition
                                        .builder(
                                                "Plataforma",
                                                "🧩"
                                        )
                                        .item(
                                                "Usuarios",
                                                "👥",
                                                "/admin/users",
                                                Feature.MULTI_USER,
                                                AdminPermission.TEAM_VIEW
                                        )
                                        .build()
                        )
                        .build();

        sidebarRegistry.register(module);
    }

    private void registerOperationalSection() {

        PlatformModuleDescriptor module =
                PlatformModuleDescriptor
                        .builder("Commerce")
                        .sidebarSection(
                                SidebarSectionDefinition
                                        .builder(
                                                "Commerce",
                                                "🛒"
                                        )
                                        .item(
                                                "Productos",
                                                "📦",
                                                "/admin/productos",
                                                Feature.PRODUCTS
                                        )
                                        .build()
                        )
                        .build();

        sidebarRegistry.register(module);
    }
}