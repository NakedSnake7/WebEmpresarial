package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountActivationServiceTest {

    @Mock
    private AdminAccountActivationTokenRepository tokenRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminAccountActivationService service;

    @BeforeEach
    void setUp() {

        service =
                new AdminAccountActivationService(
                        tokenRepository,
                        adminUserRepository,
                        passwordEncoder
                );
    }

    @Test
    void shouldCreateActivationTokenForPersistedAdmin() {

        AdminUser admin = createAdmin();

        when(
                tokenRepository.save(
                        any(AdminAccountActivationToken.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        AdminAccountActivationToken result =
                service.createToken(admin);

        assertThat(result)
                .isNotNull();

        assertThat(result.getAdminUser())
                .isSameAs(admin);

        assertThat(result.getToken())
                .isNotBlank();

        /*
         * 32 bytes codificados como Base64 URL-safe
         * sin padding producen 43 caracteres.
         */
        assertThat(result.getToken())
                .hasSize(43);

        assertThat(result.getCreatedAt())
                .isNotNull();

        assertThat(result.getExpiresAt())
                .isNotNull();

        assertThat(result.getExpiresAt())
                .isAfter(result.getCreatedAt());

        assertThat(result.getExpiresAt())
                .isBetween(
                        result.getCreatedAt()
                                .plusHours(24)
                                .minusSeconds(1),
                        result.getCreatedAt()
                                .plusHours(24)
                                .plusSeconds(1)
                );

        assertThat(result.isUsed())
                .isFalse();

        assertThat(result.isUsable())
                .isTrue();

        verify(tokenRepository)
                .save(result);
    }

    @Test
    void shouldRejectTokenCreationForNonPersistedAdmin() {

        AdminUser admin = new AdminUser();

        admin.setEmail("owner@stride.test");

        assertThatThrownBy(() ->
                service.createToken(admin)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "El administrador debe estar persistido"
                );

        verifyNoInteractions(tokenRepository);
    }

    @Test
    void shouldActivateAdminAndConsumeToken() {

        AdminUser admin = createAdmin();

        assertThat(admin.isEnabled())
                .isFalse();

        AdminAccountActivationToken token =
                validToken(admin);

        when(
                tokenRepository.findByToken(
                        "valid-token"
                )
        ).thenReturn(Optional.of(token));

        when(
                passwordEncoder.encode(
                        "StrongPassword123!"
                )
        ).thenReturn(
                "encoded-password"
        );

        service.activate(
                "valid-token",
                "StrongPassword123!"
        );

        assertThat(admin.isEnabled())
                .isTrue();

        assertThat(admin.getPassword())
                .isEqualTo(
                        "encoded-password"
                );

        assertThat(token.isUsed())
                .isTrue();

        verify(passwordEncoder)
                .encode(
                        "StrongPassword123!"
                );

        verify(adminUserRepository)
                .save(admin);

        verify(tokenRepository)
                .save(token);
    }

    @Test
    void shouldRejectAlreadyUsedToken() {

        AdminUser admin = createAdmin();

        AdminAccountActivationToken token =
                validToken(admin);

        token.setUsed(true);

        when(
                tokenRepository.findByToken(
                        "used-token"
                )
        ).thenReturn(Optional.of(token));

        assertThatThrownBy(() ->
                service.activate(
                        "used-token",
                        "StrongPassword123!"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Token de activación expirado o utilizado"
                );

        assertThat(admin.isEnabled())
                .isFalse();

        verifyNoInteractions(passwordEncoder);

        verify(adminUserRepository, never())
                .save(any());

        verify(tokenRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectExpiredToken() {

        AdminUser admin = createAdmin();

        AdminAccountActivationToken token =
                new AdminAccountActivationToken();

        token.setAdminUser(admin);
        token.setToken("expired-token");
        token.setCreatedAt(
                LocalDateTime.now()
                        .minusDays(2)
        );
        token.setExpiresAt(
                LocalDateTime.now()
                        .minusHours(1)
        );
        token.setUsed(false);

        when(
                tokenRepository.findByToken(
                        "expired-token"
                )
        ).thenReturn(Optional.of(token));

        assertThatThrownBy(() ->
                service.activate(
                        "expired-token",
                        "StrongPassword123!"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Token de activación expirado o utilizado"
                );

        assertThat(admin.isEnabled())
                .isFalse();

        assertThat(token.isUsed())
                .isFalse();

        verifyNoInteractions(passwordEncoder);

        verify(adminUserRepository, never())
                .save(any());

        verify(tokenRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectUnknownToken() {

        when(
                tokenRepository.findByToken(
                        "unknown-token"
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.activate(
                        "unknown-token",
                        "StrongPassword123!"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Token de activación inválido"
                );

        verifyNoInteractions(passwordEncoder);

        verify(adminUserRepository, never())
                .save(any());

        verify(tokenRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectBlankToken() {

        assertThatThrownBy(() ->
                service.activate(
                        "   ",
                        "StrongPassword123!"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Token de activación inválido"
                );

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(adminUserRepository);
    }

    @Test
    void shouldRejectShortPasswordWithoutConsumingToken() {

        assertThatThrownBy(() ->
                service.activate(
                        "valid-token",
                        "1234567"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "La contraseña debe tener al menos 8 caracteres"
                );

        /*
         * Importante:
         * la contraseña se valida antes incluso
         * de consultar/consumir el token.
         */
        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(adminUserRepository);
    }

    @Test
    void shouldValidateUsableToken() {

        AdminUser admin = createAdmin();

        AdminAccountActivationToken token =
                validToken(admin);

        when(
                tokenRepository.findByToken(
                        "valid-token"
                )
        ).thenReturn(Optional.of(token));

        AdminAccountActivationToken result =
                service.validate(
                        "valid-token"
                );

        assertThat(result)
                .isSameAs(token);

        assertThat(result.isUsable())
                .isTrue();

        verify(tokenRepository)
                .findByToken(
                        "valid-token"
                );

        verifyNoInteractions(
                adminUserRepository,
                passwordEncoder
        );
    }

    private AdminUser createAdmin() {

        Store store = new Store();
        store.setId(3L);

        AdminUser admin =
                new AdminUser();

        /*
         * Simulamos una entidad ya persistida.
         */
        ReflectionTestUtils.setField(
                admin,
                "id",
                10L
        );

        admin.setFullName(
                "Stride Owner"
        );

        admin.setEmail(
                "owner@stride.test"
        );

        admin.setPassword(
                "placeholder-hash"
        );

        admin.setRole(
                AdminRole.STORE_ADMIN
        );

        admin.setStore(store);

        admin.setEnabled(false);

        return admin;
    }

    private AdminAccountActivationToken validToken(
            AdminUser admin
    ) {

        AdminAccountActivationToken token =
                new AdminAccountActivationToken();

        token.setAdminUser(admin);

        token.setToken(
                "valid-token"
        );

        token.setCreatedAt(
                LocalDateTime.now()
                        .minusMinutes(5)
        );

        token.setExpiresAt(
                LocalDateTime.now()
                        .plusHours(23)
        );

        token.setUsed(false);

        return token;
    }
}