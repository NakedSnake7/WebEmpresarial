package com.webempresarial.store.interceptor;

import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AuthUserRepository;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerTenantAccessInterceptorTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CustomerTenantAccessInterceptor interceptor;

    @BeforeEach
    void setUp() {

        interceptor =
                new CustomerTenantAccessInterceptor(
                        authUserRepository,
                        storeContextService
                );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectCustomerCredentialForDifferentTenant()
            throws Exception {

        Store requestStore = store(20L);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(requestStore);

        /*
         * El correo existe en otro tenant,
         * pero no en el tenant solicitado.
         */
        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                requestStore
                        )
        ).thenReturn(Optional.empty());

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response)
                .sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );
    }

    @Test
    void shouldAllowCustomerForOwnTenant()
            throws Exception {

        Store store = store(10L);

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setEmail("cliente@example.com");
        cliente.setStore(store);

        AuthUser authUser = new AuthUser();
        authUser.setId(200L);
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(true);
        authUser.setCliente(cliente);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(store);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                store
                        )
        ).thenReturn(
                Optional.of(authUser)
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isTrue();

        verify(response, never())
                .sendError(anyInt());
    }

    @Test
    void shouldRejectWhenAuthenticatedCustomerDoesNotExist()
            throws Exception {

        Store store = store(10L);

        authenticate(
                "ghost@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(store);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "ghost@example.com",
                                store
                        )
        ).thenReturn(Optional.empty());

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response)
                .sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );
    }

    @Test
    void shouldRejectDisabledCustomer()
            throws Exception {

        Store store = store(10L);

        AuthUser authUser = new AuthUser();
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(false);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(store);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                store
                        )
        ).thenReturn(
                Optional.of(authUser)
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response)
                .sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );
    }

    @Test
    void shouldRejectCustomerWithoutLinkedCliente()
            throws Exception {

        Store store = store(10L);

        AuthUser authUser = new AuthUser();
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(true);
        authUser.setCliente(null);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(store);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                store
                        )
        ).thenReturn(
                Optional.of(authUser)
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response)
                .sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );
    }

    @Test
    void shouldRejectCustomerWhoseClienteHasNoStore()
            throws Exception {

        Store requestStore = store(10L);

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setEmail("cliente@example.com");

        AuthUser authUser = new AuthUser();
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(true);
        authUser.setCliente(cliente);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenReturn(requestStore);

        when(
                authUserRepository
                        .findByEmailAndCliente_Store(
                                "cliente@example.com",
                                requestStore
                        )
        ).thenReturn(
                Optional.of(authUser)
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response)
                .sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );
    }

    @Test
    void shouldRejectWhenRequestTenantCannotBeResolved()
            throws Exception {

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                storeContextService
                        .getCurrentStore(request)
        ).thenThrow(
                new RuntimeException(
                        "Tenant no encontrado"
                )
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response)
                .sendError(
                        HttpServletResponse.SC_FORBIDDEN
                );

        verifyNoInteractions(
                authUserRepository
        );
    }

    @Test
    void shouldAllowAnonymousRequestAndLeaveAuthenticationToSpringSecurity()
            throws Exception {

        SecurityContextHolder.clearContext();

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isTrue();

        verifyNoInteractions(
                authUserRepository,
                storeContextService
        );

        verify(response, never())
                .sendError(anyInt());
    }

    @Test
    void shouldIgnoreAuthenticatedNonCustomerIdentity()
            throws Exception {

        authenticate(
                "admin@example.com",
                "ROLE_STORE_ADMIN"
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isTrue();

        verifyNoInteractions(
                authUserRepository,
                storeContextService
        );

        verify(response, never())
                .sendError(anyInt());
    }

    private static Store store(
            Long id
    ) {

        Store store = new Store();
        store.setId(id);

        return store;
    }

    private static void authenticate(
            String email,
            String authority
    ) {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                "N/A",
                                java.util.List.of(
                                        new SimpleGrantedAuthority(
                                                authority
                                        )
                                )
                        )
                );
    }
}
