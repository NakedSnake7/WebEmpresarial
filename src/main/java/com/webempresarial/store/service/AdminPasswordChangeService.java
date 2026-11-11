package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AdminPasswordChangeService {

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordChangeService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changeOwnPassword(
            String authenticatedEmail,
            String currentPassword,
            String newPassword
    ) {
        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {
            throw new AccessDeniedException(
                    "Sesión administrativa inválida"
            );
        }

        if (currentPassword == null
                || currentPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña actual es obligatoria"
            );
        }

        validateNewPassword(newPassword);

        String email = authenticatedEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        AdminUser admin = adminUserRepository
                .findByEmailForUpdate(email)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Cuenta administrativa no disponible"
                        )
                );

        if (!admin.isEnabled()) {
            throw new AccessDeniedException(
                    "Cuenta administrativa no disponible"
            );
        }

        if (!passwordEncoder.matches(
                currentPassword,
                admin.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta"
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                admin.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente"
            );
        }

        admin.setPassword(
                passwordEncoder.encode(newPassword)
        );

        admin.rotateCredentialVersion();

        adminUserRepository.save(admin);
    }

    private void validateNewPassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe tener al menos 12 caracteres"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length
                > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "La nueva contraseña supera el límite de 72 bytes"
            );
        }
    }
}
