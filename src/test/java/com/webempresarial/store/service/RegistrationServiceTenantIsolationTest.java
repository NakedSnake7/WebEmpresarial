package com.webempresarial.store.service;

import com.webempresarial.store.dto.auth.RegisterRequestDTO;
import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AuthUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTenantIsolationTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterRequestDTO dto;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService =
                new RegistrationService(
                        authUserRepository,
                        userService,
                        passwordEncoder
                );
    }

    @Test
    void registrationShouldBindCredentialToCustomerFromCurrentTenant() {

        Store store = new Store();
        store.setId(10L);

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setStore(store);
        cliente.setEmail("cliente@example.com");

        when(dto.getEmail())
                .thenReturn(" Cliente@Example.com ");

        when(dto.getFullName())
                .thenReturn("Cliente Test");

        when(dto.getPhone())
                .thenReturn("2221234567");

        when(dto.getPassword())
                .thenReturn("secret123");

        when(
                authUserRepository.existsByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(false);

        when(
                userService.registerUser(
                        "cliente@example.com",
                        "Cliente Test",
                        "2221234567",
                        store
                )
        ).thenReturn(cliente);

        when(passwordEncoder.encode("secret123"))
                .thenReturn("encoded-password");

        registrationService.register(dto, store);

        ArgumentCaptor<AuthUser> captor =
                ArgumentCaptor.forClass(AuthUser.class);

        verify(authUserRepository)
                .save(captor.capture());

        AuthUser savedAuthUser = captor.getValue();

        assertThat(savedAuthUser.getCliente())
                .as(
                        "la credencial debe quedar vinculada "
                        + "al Cliente creado en el tenant actual"
                )
                .isSameAs(cliente);
    }
}
