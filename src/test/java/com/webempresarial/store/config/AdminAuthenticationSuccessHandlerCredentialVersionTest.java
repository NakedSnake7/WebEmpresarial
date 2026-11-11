package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAuthenticationSuccessHandlerCredentialVersionTest {

    @Test
    void storesCredentialVersionAtSuccessfulLogin()
            throws Exception {

        AdminUserRepository repository =
                mock(AdminUserRepository.class);

        AdminUser admin = new AdminUser();
        admin.setEmail("admin@example.com");
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.rotateCredentialVersion();

        when(repository.findWithStoreByEmail(
                "admin@example.com"
        )).thenReturn(Optional.of(admin));

        AdminAuthenticationSuccessHandler handler =
                new AdminAuthenticationSuccessHandler(repository);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                request,
                response,
                new UsernamePasswordAuthenticationToken(
                        "admin@example.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_SUPER_ADMIN"
                                )
                        )
                )
        );

        assertNotNull(request.getSession(false));

        assertEquals(
                1L,
                request.getSession(false).getAttribute(
                        AdminCredentialVersionFilter.SESSION_KEY
                )
        );

        assertEquals(
                "/admin/dashboard",
                response.getRedirectedUrl()
        );
    }
}
