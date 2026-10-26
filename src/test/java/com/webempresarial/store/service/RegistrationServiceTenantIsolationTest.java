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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                authUserRepository
                        .existsByEmailAndCliente_Store(
                                "cliente@example.com",
                                store
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

    @Test
    void sameEmailShouldBeAllowedAcrossDifferentStores() {

        Store stride = new Store();
        stride.setId(10L);

        Store barley = new Store();
        barley.setId(20L);

        Cliente strideCliente = new Cliente();
        strideCliente.setId(100L);
        strideCliente.setStore(stride);
        strideCliente.setEmail("cliente@example.com");

        Cliente barleyCliente = new Cliente();
        barleyCliente.setId(200L);
        barleyCliente.setStore(barley);
        barleyCliente.setEmail("cliente@example.com");

        when(dto.getEmail())
                .thenReturn(" Cliente@Example.com ");

        when(dto.getFullName())
                .thenReturn("Cliente Test");

        when(dto.getPhone())
                .thenReturn("2221234567");

        when(dto.getPassword())
                .thenReturn("secret123");

        when(
                authUserRepository
                        .existsByEmailAndCliente_Store(
                                "cliente@example.com",
                                stride
                        )
        ).thenReturn(false);

        when(
                authUserRepository
                        .existsByEmailAndCliente_Store(
                                "cliente@example.com",
                                barley
                        )
        ).thenReturn(false);

        when(
                userService.registerUser(
                        "cliente@example.com",
                        "Cliente Test",
                        "2221234567",
                        stride
                )
        ).thenReturn(strideCliente);

        when(
                userService.registerUser(
                        "cliente@example.com",
                        "Cliente Test",
                        "2221234567",
                        barley
                )
        ).thenReturn(barleyCliente);

        when(
                passwordEncoder.encode("secret123")
        ).thenReturn("encoded-password");

        registrationService.register(
                dto,
                stride
        );

        registrationService.register(
                dto,
                barley
        );

        ArgumentCaptor<AuthUser> captor =
                ArgumentCaptor.forClass(
                        AuthUser.class
                );

        verify(authUserRepository, times(2))
                .save(captor.capture());

        assertThat(captor.getAllValues())
                .hasSize(2);

        assertThat(
                captor.getAllValues()
                        .get(0)
                        .getCliente()
                        .getStore()
        ).isSameAs(stride);

        assertThat(
                captor.getAllValues()
                        .get(1)
                        .getCliente()
                        .getStore()
        ).isSameAs(barley);
    }

    @Test
    void sameEmailShouldBeRejectedInsideSameStore() {

        Store stride = new Store();
        stride.setId(10L);

        when(dto.getEmail())
                .thenReturn(" Cliente@Example.com ");

        when(
                authUserRepository
                        .existsByEmailAndCliente_Store(
                                "cliente@example.com",
                                stride
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () ->
                        registrationService.register(
                                dto,
                                stride
                        )
        )
        .isInstanceOf(
                IllegalArgumentException.class
        )
        .hasMessage(
                "El usuario ya está registrado"
        );

        verify(authUserRepository)
                .existsByEmailAndCliente_Store(
                        "cliente@example.com",
                        stride
                );

        verifyNoInteractions(
                userService,
                passwordEncoder
        );

        verify(authUserRepository, never())
                .save(any(AuthUser.class));
    }

}
