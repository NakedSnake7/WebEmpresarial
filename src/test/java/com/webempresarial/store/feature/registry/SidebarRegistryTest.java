package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.sidebar.SidebarItemDTO;
import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
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

    private SidebarRegistry sidebarRegistry;
    private Store store;

    @BeforeEach
    void setUp() {

        sidebarRegistry =
                new SidebarRegistry(
                        featureAccessService
                );

        store = new Store();
        store.setId(3L);
    }

    @Test
    void shouldShowMultiUserForStoreAdminWhenFeatureAvailable() {

        registerPlatformSection();

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
    void shouldHideMultiUserForStoreStaffEvenWhenFeatureAvailable() {

        registerPlatformSection();

        /*
         * MULTI_USER está disponible por plan,
         * pero STORE_STAFF no tiene permiso
         * para administrarlo.
         *
         * El registry debe filtrar por rol antes
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
                                                AdminRole.SUPER_ADMIN,
                                                AdminRole.STORE_ADMIN
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