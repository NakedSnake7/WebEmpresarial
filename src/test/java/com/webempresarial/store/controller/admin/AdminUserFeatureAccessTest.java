package com.webempresarial.store.controller.admin;

import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserFeatureAccessTest {

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private HttpServletRequest request;

    private AdminUserController controller;
    private Store store;

    @BeforeEach
    void setUp() {

        controller = new AdminUserController(
                adminUserService,
                storeContextService,
                featureAccessService
        );

        store = new Store();
        store.setId(10L);

        when(
                storeContextService.getCurrentStore(
                        request
                )
        ).thenReturn(store);
    }

    @Test
    void userListShouldRequireMultiUserFeature() {

        when(
                adminUserService.listarPorTienda(
                        store
                )
        ).thenReturn(List.of());

        Model model =
                new ExtendedModelMap();

        String view =
                controller.listar(
                        request,
                        model
                );

        verify(
                featureAccessService
        ).requireFeature(
                store,
                Feature.MULTI_USER
        );

        verify(
                adminUserService
        ).listarPorTienda(
                store
        );

        assertThat(view)
                .isEqualTo(
                        "admin/users/list"
                );
    }

    @Test
    void lockedMultiUserFeatureShouldStopUserList() {

        doThrow(
                new FeatureLockedException(
                        Feature.MULTI_USER
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.MULTI_USER
        );

        Model model =
                new ExtendedModelMap();

        assertThatThrownBy(() ->
                controller.listar(
                        request,
                        model
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(
                adminUserService,
                never()
        ).listarPorTienda(any(Store.class));
    }

    @Test
    void inviteFormShouldRequireMultiUserBeforeBusinessLogic() {

        Authentication authentication =
                ownerAuthentication();

        AdminUser newUser =
                new AdminUser();

        when(
                adminUserService.nuevoAdmin(
                        store,
                        AdminRole.STORE_OWNER
                )
        ).thenReturn(newUser);

        when(
                adminUserService.rolesPermitidosParaGestion(
                        AdminRole.STORE_OWNER
                )
        ).thenReturn(
                List.of(
                        AdminRole.STORE_ADMIN,
                        AdminRole.STORE_STAFF,
                        AdminRole.SALES
                )
        );

        Model model =
                new ExtendedModelMap();

        String view =
                controller.nuevo(
                        request,
                        authentication,
                        model
                );

        verify(
                featureAccessService
        ).requireFeature(
                store,
                Feature.MULTI_USER
        );

        assertThat(view)
                .isEqualTo(
                        "admin/users/form"
                );
    }

    @Test
    void lockedFeatureShouldPreventInviteCreation() {

        doThrow(
                new FeatureLockedException(
                        Feature.MULTI_USER
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.MULTI_USER
        );

        assertThatThrownBy(() ->
                controller.nuevo(
                        request,
                        ownerAuthentication(),
                        new ExtendedModelMap()
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                adminUserService
        );
    }

    @Test
    void saveShouldRequireMultiUserFeature() {

        AdminUser user = new AdminUser();

        user.setRole(
                AdminRole.STORE_STAFF
        );

        Authentication authentication =
                ownerAuthentication();

        String redirect =
                controller.guardar(
                        request,
                        authentication,
                        user
                );

        verify(
                featureAccessService
        ).requireFeature(
                store,
                Feature.MULTI_USER
        );

        verify(
                adminUserService
        ).guardarParaTienda(
                store,
                user,
                AdminRole.STORE_OWNER
        );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/users"
                );
    }

    @Test
    void stateChangeShouldRequireMultiUserFeature() {

        Authentication authentication =
                ownerAuthentication();

        String redirect =
                controller.cambiarEstado(
                        request,
                        authentication,
                        55L
                );

        verify(
                featureAccessService
        ).requireFeature(
                store,
                Feature.MULTI_USER
        );

        verify(
                adminUserService
        ).cambiarEstadoParaTienda(
                store,
                55L,
                AdminRole.STORE_OWNER
        );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/users"
                );
    }

    private Authentication ownerAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                "owner@stride.test",
                "password",
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_STORE_OWNER"
                        )
                )
        );
    }
}
