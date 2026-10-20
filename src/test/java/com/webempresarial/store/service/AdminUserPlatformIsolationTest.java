package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserPlatformIsolationTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminPermissionService adminPermissionService;

    private AdminUserService service;

    @BeforeEach
    void setUp() {
        service =
                new AdminUserService(
                        adminUserRepository,
                        storeRepository,
                        passwordEncoder,
                        adminPermissionService
                );
    }

    @Test
    void platformStateChangeShouldBeBoundToRequestedStore() {

        AdminUser target = new AdminUser();
        target.setId(7L);
        target.setEnabled(true);

        when(
                adminUserRepository.findByIdAndStoreId(
                        7L,
                        42L
                )
        ).thenReturn(Optional.of(target));

        service.cambiarEstado(
                42L,
                7L
        );

        assertThat(target.isEnabled())
                .isFalse();

        verify(adminUserRepository)
                .findByIdAndStoreId(
                        7L,
                        42L
                );
    }

    @Test
    void platformStateChangeShouldRejectUserFromAnotherStore() {

        when(
                adminUserRepository.findByIdAndStoreId(
                        7L,
                        42L
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.cambiarEstado(
                        42L,
                        7L
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "Admin no encontrado en esta tienda"
        );
    }
}
