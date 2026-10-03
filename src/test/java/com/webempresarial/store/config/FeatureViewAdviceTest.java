package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.dto.sidebar.SidebarSectionDTO;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

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
    private HttpServletRequest request;

    private FeatureViewAdvice advice;
    private Store store;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        advice = new FeatureViewAdvice(
                storeContextService,
                sidebarRegistry,
                inventoryAlertService
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

    private void authenticate(
            String username,
            String authority
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        List.of(
                                new SimpleGrantedAuthority(
                                        authority
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}