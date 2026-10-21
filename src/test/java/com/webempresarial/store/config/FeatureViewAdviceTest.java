package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureViewAdviceTest {

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private SidebarRegistry sidebarRegistry;

    @Mock
    private InventoryPersistentAlertService inventoryAlertService;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private ObjectProvider<FeatureAccessService>
            featureAccessServiceProvider;

    @Mock
    private HttpServletRequest request;

    private FeatureViewAdvice advice;
    private Store store;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        advice = new FeatureViewAdvice(
                storeContextService,
                sidebarRegistry,
                inventoryAlertService,
                featureAccessServiceProvider
        );

        store = new Store();
        store.setId(3L);

        when(storeContextService.getCurrentStore(request))
                .thenReturn(store);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveStoreOwnerRoleForSidebar() {

        authenticate(
                "owner@stride.test",
                "ROLE_STORE_OWNER"
        );

        when(
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_OWNER
                )
        ).thenReturn(List.of());

        List<SidebarSectionDTO> result =
                advice.sidebarSections(request);

        assertThat(result).isEmpty();

        verify(sidebarRegistry).sections(
                store,
                AdminRole.STORE_OWNER
        );
    }

    @Test
    void shouldResolveSalesRoleForSidebar() {

        authenticate(
                "sales@stride.test",
                "ROLE_SALES"
        );

        when(
                sidebarRegistry.sections(
                        store,
                        AdminRole.SALES
                )
        ).thenReturn(List.of());

        List<SidebarSectionDTO> result =
                advice.sidebarSections(request);

        assertThat(result).isEmpty();

        verify(sidebarRegistry).sections(
                store,
                AdminRole.SALES
        );
    }

    @Test
    void shouldResolveStoreAdminRoleForSidebar() {

        authenticate(
                "admin@stride.test",
                "ROLE_STORE_ADMIN"
        );

        when(
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_ADMIN
                )
        ).thenReturn(List.of());

        List<SidebarSectionDTO> result =
                advice.sidebarSections(request);

        assertThat(result).isEmpty();

        verify(sidebarRegistry).sections(
                store,
                AdminRole.STORE_ADMIN
        );
    }

    @Test
    void shouldResolveStoreStaffRoleForSidebar() {

        authenticate(
                "staff@stride.test",
                "ROLE_STORE_STAFF"
        );

        when(
                sidebarRegistry.sections(
                        store,
                        AdminRole.STORE_STAFF
                )
        ).thenReturn(List.of());

        advice.sidebarSections(request);

        verify(sidebarRegistry).sections(
                store,
                AdminRole.STORE_STAFF
        );
    }

    @Test
    void shouldResolveSuperAdminRoleForSidebar() {

        authenticate(
                "superadmin@webempresarial.test",
                "ROLE_SUPER_ADMIN"
        );

        when(
                sidebarRegistry.sections(
                        store,
                        AdminRole.SUPER_ADMIN
                )
        ).thenReturn(List.of());

        advice.sidebarSections(request);

        verify(sidebarRegistry).sections(
                store,
                AdminRole.SUPER_ADMIN
        );
    }

    @Test
    void shouldReturnEmptySidebarWhenAuthenticationMissing() {

        List<SidebarSectionDTO> result =
                advice.sidebarSections(request);

        assertThat(result).isEmpty();

        verifyNoInteractions(sidebarRegistry);
    }

    @Test
    void shouldReturnEmptySidebarWhenStoreCannotBeResolved() {

        authenticate(
                "staff@stride.test",
                "ROLE_STORE_STAFF"
        );

        when(storeContextService.getCurrentStore(request))
                .thenThrow(
                        new RuntimeException(
                                "Store not found"
                        )
                );

        List<SidebarSectionDTO> result =
                advice.sidebarSections(request);

        assertThat(result).isEmpty();

        verifyNoInteractions(sidebarRegistry);
    }

    @Test
    void sidebarBadgesShouldNotQueryInventoryWithoutPermission() {

        when(request.getRequestURI())
                .thenReturn("/admin/dashboard");

        authenticate(
                "sales@stride.test",
                "ROLE_SALES"
        );

        Map<String, Long> result =
                advice.sidebarBadges(request);

        assertThat(result).isEmpty();

        verifyNoInteractions(featureAccessService);
        verifyNoInteractions(inventoryAlertService);
    }

    @Test
    void sidebarBadgesShouldFailClosedWhenFeatureServiceIsUnavailable() {

        when(request.getRequestURI())
                .thenReturn("/admin/dashboard");

        authenticate(
                "staff@stride.test",
                "ROLE_STORE_STAFF",
                AdminPermission.INVENTORY_VIEW.authority()
        );

        when(
                featureAccessServiceProvider
                        .getIfAvailable()
        ).thenReturn(null);

        Map<String, Long> result =
                advice.sidebarBadges(request);

        assertThat(result).isEmpty();

        verify(featureAccessServiceProvider)
                .getIfAvailable();

        verifyNoInteractions(
                featureAccessService,
                inventoryAlertService
        );
    }

    @Test
    void sidebarBadgesShouldNotQueryInventoryWhenFeatureLocked() {

        when(request.getRequestURI())
                .thenReturn("/admin/dashboard");

        authenticate(
                "staff@stride.test",
                "ROLE_STORE_STAFF",
                AdminPermission.INVENTORY_VIEW.authority()
        );

        when(
                featureAccessServiceProvider
                        .getIfAvailable()
        ).thenReturn(featureAccessService);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.INVENTORY
                )
        ).thenReturn(false);

        Map<String, Long> result =
                advice.sidebarBadges(request);

        assertThat(result).isEmpty();

        verify(featureAccessService).canUse(
                store,
                Feature.INVENTORY
        );

        verifyNoInteractions(inventoryAlertService);
    }

    @Test
    void sidebarBadgesShouldReturnInventoryAlertsWhenAllowed() {

        when(request.getRequestURI())
                .thenReturn("/admin/dashboard");

        authenticate(
                "staff@stride.test",
                "ROLE_STORE_STAFF",
                AdminPermission.INVENTORY_VIEW.authority()
        );

        when(
                featureAccessServiceProvider
                        .getIfAvailable()
        ).thenReturn(featureAccessService);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.INVENTORY
                )
        ).thenReturn(true);

        when(
                inventoryAlertService.countActive(store)
        ).thenReturn(3L);

        Map<String, Long> result =
                advice.sidebarBadges(request);

        assertThat(result)
                .containsExactly(
                        Map.entry(
                                "/admin/inventory/alerts",
                                3L
                        )
                );

        verify(featureAccessService).canUse(
                store,
                Feature.INVENTORY
        );

        verify(inventoryAlertService)
                .countActive(store);
    }

    private void authenticate(
            String username,
            String... authorities
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        Arrays.stream(authorities)
                                .map(
                                        SimpleGrantedAuthority::new
                                )
                                .toList()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}