package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService service;

    @BeforeEach
    void setUp() {

        service = new AdminUserService(
                adminUserRepository,
                storeRepository,
                passwordEncoder,
                new AdminPermissionService()
        );
    }

    @Test
    void shouldListOnlyUsersFromCurrentStore() {

        Store store = store(10L);

        AdminUser admin = new AdminUser();
        admin.setId(100L);
        admin.setStore(store);

        when(
                adminUserRepository.findByStoreId(10L)
        ).thenReturn(List.of(admin));

        assertThat(
                service.listarPorTienda(store)
        ).containsExactly(admin);
    }

    @Test
    void ownerShouldManageAdminStaffAndSalesRoles() {

        assertThat(
                service.rolesPermitidosParaGestion(
                        AdminRole.STORE_OWNER
                )
        ).containsExactly(
                AdminRole.STORE_ADMIN,
                AdminRole.STORE_STAFF,
                AdminRole.SALES
        );
    }

    @Test
    void storeAdminShouldManageOnlyStaffAndSales() {

        assertThat(
                service.rolesPermitidosParaGestion(
                        AdminRole.STORE_ADMIN
                )
        ).containsExactly(
                AdminRole.STORE_STAFF,
                AdminRole.SALES
        );
    }

    @Test
    void ownerShouldCreateStoreAdmin() {

        Store store = store(10L);

        AdminUser admin =
                validUser(
                        AdminRole.STORE_ADMIN,
                        "admin@example.com"
                );

        when(
                adminUserRepository.existsByEmail(
                        "admin@example.com"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode("password")
        ).thenReturn("encoded");

        service.guardarParaTienda(
                store,
                admin,
                AdminRole.STORE_OWNER
        );

        verify(adminUserRepository)
                .save(admin);

        assertThat(admin.getStore())
                .isSameAs(store);

        assertThat(admin.getPassword())
                .isEqualTo("encoded");
    }

    @Test
    void storeAdminShouldCreateSalesUser() {

        Store store = store(10L);

        AdminUser sales =
                validUser(
                        AdminRole.SALES,
                        "sales@example.com"
                );

        when(
                adminUserRepository.existsByEmail(
                        "sales@example.com"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode("password")
        ).thenReturn("encoded");

        service.guardarParaTienda(
                store,
                sales,
                AdminRole.STORE_ADMIN
        );

        verify(adminUserRepository)
                .save(sales);
    }

    @Test
    void storeAdminShouldNotCreateAnotherStoreAdmin() {

        Store store = store(10L);

        AdminUser admin =
                validUser(
                        AdminRole.STORE_ADMIN,
                        "admin2@example.com"
                );

        assertThatThrownBy(() ->
                service.guardarParaTienda(
                        store,
                        admin,
                        AdminRole.STORE_ADMIN
                )
        )
        .isInstanceOf(
                AccessDeniedException.class
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());
    }

    @Test
    void ownerShouldNotCreateAnotherOwnerFromTeamManager() {

        Store store = store(10L);

        AdminUser owner =
                validUser(
                        AdminRole.STORE_OWNER,
                        "owner2@example.com"
                );

        assertThatThrownBy(() ->
                service.guardarParaTienda(
                        store,
                        owner,
                        AdminRole.STORE_OWNER
                )
        )
        .isInstanceOf(
                AccessDeniedException.class
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldRejectSubmittedIdDuringCreation() {

        Store store = store(10L);

        AdminUser admin =
                validUser(
                        AdminRole.STORE_STAFF,
                        "staff@example.com"
                );

        admin.setId(999L);

        assertThatThrownBy(() ->
                service.guardarParaTienda(
                        store,
                        admin,
                        AdminRole.STORE_OWNER
                )
        )
        .isInstanceOf(
                IllegalArgumentException.class
        )
        .hasMessage(
                "El alta de usuarios no admite un id existente"
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());
    }

    @Test
    void ownerShouldNeverBeDisabledFromTenantTeamManager() {

        Store store = store(10L);

        AdminUser owner = new AdminUser();

        owner.setId(50L);
        owner.setStore(store);
        owner.setRole(
                AdminRole.STORE_OWNER
        );
        owner.setEnabled(true);

        when(
                adminUserRepository.findByIdAndStoreId(
                        50L,
                        10L
                )
        ).thenReturn(
                Optional.of(owner)
        );

        assertThatThrownBy(() ->
                service.cambiarEstadoParaTienda(
                        store,
                        50L,
                        AdminRole.STORE_OWNER
                )
        )
        .isInstanceOf(
                AccessDeniedException.class
        )
        .hasMessage(
                "El propietario de la tienda no puede ser desactivado"
        );

        assertThat(owner.isEnabled())
                .isTrue();
    }

    @Test
    void storeAdminShouldNotDisableAnotherStoreAdmin() {

        Store store = store(10L);

        AdminUser target = new AdminUser();

        target.setId(60L);
        target.setStore(store);
        target.setRole(
                AdminRole.STORE_ADMIN
        );
        target.setEnabled(true);

        when(
                adminUserRepository.findByIdAndStoreId(
                        60L,
                        10L
                )
        ).thenReturn(
                Optional.of(target)
        );

        assertThatThrownBy(() ->
                service.cambiarEstadoParaTienda(
                        store,
                        60L,
                        AdminRole.STORE_ADMIN
                )
        )
        .isInstanceOf(
                AccessDeniedException.class
        );

        assertThat(target.isEnabled())
                .isTrue();
    }

    @Test
    void ownerShouldDisableStoreAdmin() {

        Store store = store(10L);

        AdminUser target = new AdminUser();

        target.setId(60L);
        target.setStore(store);
        target.setRole(
                AdminRole.STORE_ADMIN
        );
        target.setEnabled(true);

        when(
                adminUserRepository.findByIdAndStoreId(
                        60L,
                        10L
                )
        ).thenReturn(
                Optional.of(target)
        );

        service.cambiarEstadoParaTienda(
                store,
                60L,
                AdminRole.STORE_OWNER
        );

        assertThat(target.isEnabled())
                .isFalse();
    }

    @Test
    void shouldNotChangeStateOfUserFromAnotherStore() {

        Store store = store(10L);

        when(
                adminUserRepository.findByIdAndStoreId(
                        200L,
                        10L
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                service.cambiarEstadoParaTienda(
                        store,
                        200L,
                        AdminRole.STORE_OWNER
                )
        )
        .isInstanceOf(
                IllegalArgumentException.class
        )
        .hasMessage(
                "Usuario no encontrado en esta tienda"
        );
    }

    @Test
    void platformStoreUserCreationShouldRejectSuperAdminRole() {

        Store store = store(10L);

        AdminUser user =
                validUser(
                        AdminRole.SUPER_ADMIN,
                        "root2@example.com"
                );

        when(
                storeRepository.findById(10L)
        ).thenReturn(
                Optional.of(store)
        );

        assertThatThrownBy(() ->
                service.guardar(
                        10L,
                        user
                )
        )
        .isInstanceOf(
                IllegalArgumentException.class
        )
        .hasMessage(
                "SUPER_ADMIN no puede asignarse a una tienda"
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());
    }

    @Test
    void platformStoreUserCreationShouldRejectExistingId() {

        Store store = store(10L);

        AdminUser user =
                validUser(
                        AdminRole.STORE_ADMIN,
                        "admin3@example.com"
                );

        user.setId(777L);

        when(
                storeRepository.findById(10L)
        ).thenReturn(
                Optional.of(store)
        );

        assertThatThrownBy(() ->
                service.guardar(
                        10L,
                        user
                )
        )
        .isInstanceOf(
                IllegalArgumentException.class
        )
        .hasMessage(
                "El alta de usuarios no admite un id existente"
        );

        verify(
                adminUserRepository,
                never()
        ).save(any());
    }

    private AdminUser validUser(
            AdminRole role,
            String email
    ) {

        AdminUser user = new AdminUser();

        user.setFullName("Test User");
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setEnabled(true);

        return user;
    }

    private Store store(Long id) {

        Store store = new Store();

        store.setId(id);

        return store;
    }
}
