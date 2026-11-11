package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminPasswordChangeServiceTest {

    private static final String EMAIL = "admin@example.com";
    private static final String CURRENT = "ClaveActual2026!";
    private static final String NEW_PASSWORD =
            "NuevaClaveSegura2026!";

    private AdminUserRepository repository;
    private PasswordEncoder encoder;
    private AdminPasswordChangeService service;
    private AdminUser admin;

    @BeforeEach
    void setUp() {
        repository = mock(AdminUserRepository.class);
        encoder = new BCryptPasswordEncoder();

        service = new AdminPasswordChangeService(
                repository,
                encoder
        );

        admin = new AdminUser();
        admin.setEmail(EMAIL);
        admin.setPassword(encoder.encode(CURRENT));
        admin.setEnabled(true);
    }

    @Test
    void changesOnlyAuthenticatedAdminPassword() {
        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        service.changeOwnPassword(
                " ADMIN@EXAMPLE.COM ",
                CURRENT,
                NEW_PASSWORD
        );

        assertTrue(
                encoder.matches(
                        NEW_PASSWORD,
                        admin.getPassword()
                )
        );

        assertFalse(
                encoder.matches(
                        CURRENT,
                        admin.getPassword()
                )
        );

        assertEquals(1L, admin.getCredentialVersion());

        verify(repository).save(admin);
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        "ClaveIncorrecta",
                        NEW_PASSWORD
                )
        );

        assertTrue(
                encoder.matches(CURRENT, admin.getPassword())
        );

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsReusingCurrentPassword() {
        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        CURRENT,
                        CURRENT
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDisabledAdmin() {
        admin.setEnabled(false);

        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        assertThrows(
                AccessDeniedException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        CURRENT,
                        NEW_PASSWORD
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsNonexistentAdmin() {
        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        CURRENT,
                        NEW_PASSWORD
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsShortNewPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        CURRENT,
                        "corta"
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsPasswordOver72Utf8Bytes() {
        String password = "á".repeat(40);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        CURRENT,
                        password
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMissingAuthenticatedIdentity() {
        assertThrows(
                AccessDeniedException.class,
                () -> service.changeOwnPassword(
                        null,
                        CURRENT,
                        NEW_PASSWORD
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void incrementsVersionOnEachSuccessfulPasswordChange() {
        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        service.changeOwnPassword(
                EMAIL,
                CURRENT,
                NEW_PASSWORD
        );

        assertEquals(1L, admin.getCredentialVersion());

        service.changeOwnPassword(
                EMAIL,
                NEW_PASSWORD,
                "OtraClaveSegura2026!"
        );

        assertEquals(2L, admin.getCredentialVersion());

        verify(repository, times(2)).save(admin);
    }

    @Test
    void incorrectPasswordDoesNotRotateVersion() {
        when(repository.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeOwnPassword(
                        EMAIL,
                        "Incorrecta2026!",
                        NEW_PASSWORD
                )
        );

        assertEquals(0L, admin.getCredentialVersion());
        verify(repository, never()).save(any());
    }
}
