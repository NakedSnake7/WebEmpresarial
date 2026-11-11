package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.boot.test.autoconfigure.web.servlet
        .MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import java.util.Optional;

/**
 * Soporte exclusivo para pruebas @WebMvcTest
 * de reglas de autorización.
 *
 * No debe importarse en integraciones que
 * validen sesiones administrativas reales.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AdminAuthorizationSliceTestSupport {

    @Bean
    AdminUserRepository authorizationSliceAdminRepository() {

        AdminUserRepository repository =
                mock(AdminUserRepository.class);

        /*
         * Las identidades de estas pruebas son
         * deliberadamente sintéticas.
         *
         * La verificación de cuentas reales y
         * revocación de sesiones se realiza en
         * las pruebas HTTP + MySQL.
         */
        when(repository.findByEmail(anyString()))
                .thenAnswer(invocation -> {

                    AdminUser admin =
                            mock(AdminUser.class);

                    when(admin.isEnabled())
                            .thenReturn(true);

                    when(admin.getCredentialVersion())
                            .thenReturn(0L);

                    return Optional.of(admin);
                });

        return repository;
    }

    @Bean
    MockMvcBuilderCustomizer authorizationSliceSession() {

        /*
         * Simula una sesión con versión registrada.
         * No autentica a nadie por sí misma.
         *
         * Cada prueba conserva su propio principal,
         * roles y permisos mediante .with(user(...)).
         */
        return builder -> builder.defaultRequest(
                get("/").sessionAttr(
                        AdminCredentialVersionFilter.SESSION_KEY,
                        0L
                )
        );
    }
}
