package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AdminAccountActivationService {

    private static final int TOKEN_BYTES = 32;

    private final AdminAccountActivationTokenRepository tokenRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public AdminAccountActivationService(
            AdminAccountActivationTokenRepository tokenRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenRepository = tokenRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminAccountActivationToken createToken(
            AdminUser adminUser
    ) {

        if (adminUser == null || adminUser.getId() == null) {
            throw new IllegalArgumentException(
                    "El administrador debe estar persistido"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        AdminAccountActivationToken token =
                new AdminAccountActivationToken();

        token.setAdminUser(adminUser);
        token.setToken(generateToken());
        token.setCreatedAt(now);
        token.setExpiresAt(
                now.plusHours(24)
        );
        token.setUsed(false);

        return tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public AdminAccountActivationToken validate(
            String rawToken
    ) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Token de activación inválido"
            );
        }

        AdminAccountActivationToken token =
                tokenRepository
                        .findByToken(rawToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Token de activación inválido"
                                )
                        );

        if (!token.isUsable()) {
            throw new IllegalArgumentException(
                    "Token de activación expirado o utilizado"
            );
        }

        return token;
    }

    @Transactional
    public void activate(
            String rawToken,
            String newPassword
    ) {

        if (newPassword == null
                || newPassword.length() < 8) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres"
            );
        }

        AdminAccountActivationToken token =
                validate(rawToken);

        AdminUser admin =
                token.getAdminUser();

        admin.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        admin.setEnabled(true);

        token.markUsed();

        adminUserRepository.save(admin);
        tokenRepository.save(token);
    }

    private String generateToken() {

        byte[] bytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}