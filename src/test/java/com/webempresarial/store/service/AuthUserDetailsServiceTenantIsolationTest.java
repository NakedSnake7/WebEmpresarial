package com.webempresarial.store.service;

import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AuthUserRepository;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails
        .UsernameNotFoundException;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request
        .ServletRequestAttributes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTenantIsolationTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private StoreContextService storeContextService;

    private AuthUserDetailsService service;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {

        service =
                new AuthUserDetailsService(
                        authUserRepository,
                        storeContextService
                );

        request =
                new MockHttpServletRequest();

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(
                        request
                )
        );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldAuthenticateCustomerFromCurrentTenant() {

        Store barley = store(20L);

        AuthUser customer =
                customer(
                        barley,
                        "cliente@example.com",
                        "{bcrypt}barley-password"
                );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(barley);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                barley
                        )
        ).thenReturn(
                Optional.of(customer)
        );

        UserDetails result =
                service.loadUserByUsername(
                        " Cliente@Example.com "
                );

        assertThat(result.getUsername())
                .isEqualTo(
                        "cliente@example.com"
                );

        assertThat(result.getPassword())
                .isEqualTo(
                        "{bcrypt}barley-password"
                );

        assertThat(result.getAuthorities())
                .extracting(
                        authority ->
                                authority.getAuthority()
                )
                .containsExactly(
                        "ROLE_CLIENTE"
                );
    }

    @Test
    void shouldNotFallBackToCredentialFromAnotherTenant() {

        Store barley = store(20L);

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(barley);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                barley
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                        service.loadUserByUsername(
                                "cliente@example.com"
                        )
        )
        .isInstanceOf(
                UsernameNotFoundException.class
        );

        verify(authUserRepository)
                .findByEmailAndCliente_Store(
                        "cliente@example.com",
                        barley
                );

        verifyNoMoreInteractions(
                authUserRepository
        );
    }

    @Test
    void shouldFailClosedWhenTenantCannotBeResolved() {

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenThrow(
                new RuntimeException(
                        "Tenant no encontrado"
                )
        );

        assertThatThrownBy(
                () ->
                        service.loadUserByUsername(
                                "cliente@example.com"
                        )
        )
        .isInstanceOf(
                UsernameNotFoundException.class
        );

        verifyNoInteractions(
                authUserRepository
        );
    }

    private static Store store(
            Long id
    ) {

        Store store = new Store();
        store.setId(id);

        return store;
    }

    private static AuthUser customer(
            Store store,
            String email,
            String password
    ) {

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setEmail(email);
        cliente.setStore(store);

        AuthUser authUser = new AuthUser();
        authUser.setId(200L);
        authUser.setEmail(email);
        authUser.setPassword(password);
        authUser.setRole(
                AuthUser.Role.CLIENTE
        );
        authUser.setEnabled(true);
        authUser.setCliente(cliente);

        return authUser;
    }
}
