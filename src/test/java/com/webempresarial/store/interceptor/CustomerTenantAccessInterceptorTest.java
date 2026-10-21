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

        Store credentialStore = store(10L);
        Store requestStore = store(20L);

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setEmail("cliente@example.com");
        cliente.setStore(credentialStore);

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
                authUserRepository.findByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(Optional.of(authUser));

        when(
                storeContextService.getCurrentStore(request)
        ).thenReturn(requestStore);

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
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
                authUserRepository.findByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(Optional.of(authUser));

        when(
                storeContextService.getCurrentStore(request)
        ).thenReturn(store);

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

        authenticate(
                "ghost@example.com",
                "ROLE_CLIENTE"
        );

        when(
                authUserRepository.findByEmail(
                        "ghost@example.com"
                )
        ).thenReturn(Optional.empty());

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verifyNoInteractions(storeContextService);
    }

    @Test
    void shouldRejectDisabledCustomer()
            throws Exception {

        AuthUser authUser = new AuthUser();
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(false);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                authUserRepository.findByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(Optional.of(authUser));

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verifyNoInteractions(storeContextService);
    }

    @Test
    void shouldRejectCustomerWithoutLinkedCliente()
            throws Exception {

        AuthUser authUser = new AuthUser();
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(true);
        authUser.setCliente(null);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                authUserRepository.findByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(Optional.of(authUser));

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verifyNoInteractions(storeContextService);
    }

    @Test
    void shouldRejectCustomerWhoseClienteHasNoStore()
            throws Exception {

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
                authUserRepository.findByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(Optional.of(authUser));

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verifyNoInteractions(storeContextService);
    }

    @Test
    void shouldRejectWhenRequestTenantCannotBeResolved()
            throws Exception {

        Store credentialStore = store(10L);

        Cliente cliente = new Cliente();
        cliente.setId(100L);
        cliente.setEmail("cliente@example.com");
        cliente.setStore(credentialStore);

        AuthUser authUser = new AuthUser();
        authUser.setEmail("cliente@example.com");
        authUser.setEnabled(true);
        authUser.setCliente(cliente);

        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        when(
                authUserRepository.findByEmail(
                        "cliente@example.com"
                )
        ).thenReturn(Optional.of(authUser));

        when(
                storeContextService.getCurrentStore(request)
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

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
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

        verifyNoInteractions(authUserRepository);
        verifyNoInteractions(storeContextService);

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

        verifyNoInteractions(authUserRepository);
        verifyNoInteractions(storeContextService);

        verify(response, never())
                .sendError(anyInt());
    }

    private static Store store(Long id) {
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
