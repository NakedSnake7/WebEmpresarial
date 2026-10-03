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
                passwordEncoder
        );
    }

    @Test
    void shouldListOnlyUsersFromCurrentStore() {

        Store store = store(10L);

        AdminUser admin = new AdminUser();
        admin.setId(100L);
        admin.setStore(store);

        when(adminUserRepository.findByStoreId(10L))
                .thenReturn(List.of(admin));

        List<AdminUser> result =
                service.listarPorTienda(store);

        assertThat(result)
                .containsExactly(admin);

        verify(adminUserRepository)
                .findByStoreId(10L);
    }

    @Test
    void shouldCreateNewTenantAdminWithSafeDefaults() {

        Store store = store(10L);

        AdminUser admin =
                service.nuevoAdmin(store);

        assertThat(admin.getStore())
                .isSameAs(store);

        assertThat(admin.getRole())
                .isEqualTo(AdminRole.STORE_STAFF);

        assertThat(admin.isEnabled())
                .isTrue();

        verifyNoInteractions(adminUserRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldSaveTenantUserWithNormalizedEmailAndEncodedPassword() {

        Store currentStore = store(10L);
        Store forgedStore = store(99L);

        AdminUser admin = new AdminUser();
        admin.setFullName("Staff User");
        admin.setEmail(" Staff@Example.COM ");
        admin.setPassword("temporary-password");
        admin.setRole(AdminRole.STORE_STAFF);
        admin.setStore(forgedStore);

        when(
                adminUserRepository.existsByEmail(
                        "staff@example.com"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(
                        "temporary-password"
                )
        ).thenReturn("encoded-password");

        service.guardarParaTienda(
                currentStore,
                admin
        );

        ArgumentCaptor<AdminUser> captor =
                ArgumentCaptor.forClass(
                        AdminUser.class
                );

        verify(adminUserRepository)
                .save(captor.capture());

        AdminUser saved =
                captor.getValue();

        assertThat(saved.getStore())
                .isSameAs(currentStore);

        assertThat(saved.getEmail())
                .isEqualTo("staff@example.com");

        assertThat(saved.getPassword())
                .isEqualTo("encoded-password");

        assertThat(saved.getRole())
                .isEqualTo(AdminRole.STORE_STAFF);

        verify(passwordEncoder)
                .encode("temporary-password");
    }

    @Test
    void shouldRejectSuperAdminCreationFromTenant() {

        Store store = store(10L);

        AdminUser admin = new AdminUser();
        admin.setEmail("root@example.com");
        admin.setPassword("temporary-password");
        admin.setRole(AdminRole.SUPER_ADMIN);

        assertThatThrownBy(
                () -> service.guardarParaTienda(
                        store,
                        admin
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Una tienda no puede crear SUPER_ADMIN"
                );

        verifyNoInteractions(adminUserRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectDuplicateEmail() {

        Store store = store(10L);

        AdminUser admin = new AdminUser();
        admin.setEmail(" Existing@Example.COM ");
        admin.setPassword("temporary-password");
        admin.setRole(AdminRole.STORE_ADMIN);

        when(
                adminUserRepository.existsByEmail(
                        "existing@example.com"
                )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.guardarParaTienda(
                        store,
                        admin
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Ya existe un usuario con ese email"
                );

        verify(adminUserRepository, never())
                .save(any(AdminUser.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldNotChangeStateOfUserFromAnotherStore() {

        Store currentStore = store(10L);

        when(
                adminUserRepository.findByIdAndStoreId(
                        200L,
                        10L
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.cambiarEstadoParaTienda(
                        currentStore,
                        200L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Usuario no encontrado en esta tienda"
                );

        verify(adminUserRepository)
                .findByIdAndStoreId(
                        200L,
                        10L
                );
    }

    @Test
    void shouldToggleStateOfUserFromCurrentStore() {

        Store store = store(10L);

        AdminUser admin = new AdminUser();
        admin.setId(200L);
        admin.setStore(store);
        admin.setEnabled(true);

        when(
                adminUserRepository.findByIdAndStoreId(
                        200L,
                        10L
                )
        ).thenReturn(Optional.of(admin));

        service.cambiarEstadoParaTienda(
                store,
                200L
        );

        assertThat(admin.isEnabled())
                .isFalse();

        verify(adminUserRepository)
                .findByIdAndStoreId(
                        200L,
                        10L
                );
    }

    private Store store(Long id) {
        Store store = new Store();
        store.setId(id);
        return store;
    }
}
