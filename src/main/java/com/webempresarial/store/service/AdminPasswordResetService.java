package com.webempresarial.store.service;

import com.webempresarial.store.event.AdminPasswordResetIssuedEvent;
import com.webempresarial.store.model.AdminPasswordResetToken;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminPasswordResetTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
public class AdminPasswordResetService {

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_BYTES = 72;

    private static final int EXPIRATION_MINUTES = 20;
    private static final int REQUEST_COOLDOWN_MINUTES = 5;

    private final AdminUserRepository admins;
    private final AdminPasswordResetTokenRepository tokens;
    private final AdminPasswordResetTokenCodec codec;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;

    @Autowired
    public AdminPasswordResetService(
            AdminUserRepository admins,
            AdminPasswordResetTokenRepository tokens,
            AdminPasswordResetTokenCodec codec,
            PasswordEncoder encoder,
            ApplicationEventPublisher publisher
    ) {
        this(
                admins,
                tokens,
                codec,
                encoder,
                publisher,
                Clock.systemUTC()
        );
    }

    /*
     * Constructor para pruebas con reloj determinista.
     */
    AdminPasswordResetService(
            AdminUserRepository admins,
            AdminPasswordResetTokenRepository tokens,
            AdminPasswordResetTokenCodec codec,
            PasswordEncoder encoder,
            ApplicationEventPublisher publisher,
            Clock clock
    ) {
        this.admins = admins;
        this.tokens = tokens;
        this.codec = codec;
        this.encoder = encoder;
        this.publisher = publisher;
        this.clock = clock;
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );
    }

    /*
     * No revela si la cuenta existe.
     * El controlador siempre devolverá una respuesta genérica.
     */
    @Transactional
    public void requestRecovery(String submittedEmail) {

        if (submittedEmail == null
                || submittedEmail.isBlank()
                || submittedEmail.length() > 254) {
            return;
        }

        String email = submittedEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        AdminUser admin = admins
                .findByEmailForUpdate(email)
                .orElse(null);

        if (admin == null || !admin.isEnabled()) {
            return;
        }

        LocalDateTime currentTime = now();

        List<AdminPasswordResetToken> pending =
                tokens.findByAdminUserIdAndConsumedAtIsNull(
                        admin.getId()
                );

        /*
         * Evita emitir varios correos sucesivos
         * para una misma cuenta.
         */
        boolean inCooldown = pending.stream()
                .anyMatch(token ->
                        token.isUsableAt(currentTime)
                        && token.getCredentialVersionAtIssue()
                                == admin.getCredentialVersion()
                        && !token.getCreatedAt().isBefore(
                                currentTime.minusMinutes(
                                        REQUEST_COOLDOWN_MINUTES
                                )
                        )
                );

        if (inCooldown) {
            return;
        }

        /*
         * Invalida los enlaces anteriores aún activos.
         */
        for (AdminPasswordResetToken previous : pending) {
            if (previous.isUsableAt(currentTime)) {
                previous.consume(currentTime);
                tokens.save(previous);
            }
        }

        String rawToken = codec.generateRawToken();
        String tokenHash = codec.hashToken(rawToken);

        AdminPasswordResetToken issued =
                AdminPasswordResetToken.issue(
                        admin,
                        tokenHash,
                        currentTime,
                        currentTime.plusMinutes(
                                EXPIRATION_MINUTES
                        )
                );

        tokens.save(issued);

        publisher.publishEvent(
                new AdminPasswordResetIssuedEvent(
                        admin.getEmail(),
                        admin.getFullName(),
                        rawToken
                )
        );
    }

    /*
     * Bloqueo en orden:
     *
     * 1. Identificar token sin bloquear.
     * 2. Bloquear administrador.
     * 3. Bloquear token y revalidar.
     *
     * Evita competencia entre emisión y consumo.
     */
    @Transactional
    public void completeRecovery(
            String rawToken,
            String newPassword
    ) {

        final String hash;

        try {
            hash = codec.hashToken(rawToken);
        } catch (IllegalArgumentException ex) {
            throw invalidToken();
        }

        /*
         * Consulta escalar sin materializar el token.
         * El token se carga como entidad únicamente
         * después de bloquear al administrador.
         */
        String email = tokens.findAdminEmailByTokenHash(hash)
                .orElseThrow(
                        AdminPasswordResetService::invalidToken
                );

        AdminUser admin = admins
                .findByEmailForUpdate(email)
                .orElseThrow(
                        AdminPasswordResetService::invalidToken
                );

        AdminPasswordResetToken locked =
                tokens.findByTokenHashForUpdate(hash)
                        .orElseThrow(
                                AdminPasswordResetService::invalidToken
                        );

        LocalDateTime currentTime = now();

        if (!admin.isEnabled()
                || !admin.getId().equals(
                        locked.getAdminUser().getId()
                )
                || locked.getCredentialVersionAtIssue()
                        != admin.getCredentialVersion()
                || !locked.isUsableAt(currentTime)) {
            throw invalidToken();
        }

        validateNewPassword(newPassword);

        if (encoder.matches(
                newPassword,
                admin.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente"
            );
        }

        admin.setPassword(
                encoder.encode(newPassword)
        );

        admin.rotateCredentialVersion();

        locked.consume(currentTime);

        /*
         * Revoca los demás tokens activos
         * de la misma cuenta.
         */
        for (AdminPasswordResetToken token :
                tokens.findByAdminUserIdAndConsumedAtIsNull(
                        admin.getId()
                )) {

            if (token.isUsableAt(currentTime)) {
                token.consume(currentTime);
                tokens.save(token);
            }
        }

        tokens.save(locked);
        admins.save(admin);
    }

    private void validateNewPassword(String password) {

        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe tener al menos 12 caracteres"
            );
        }

        if (password.getBytes(
                StandardCharsets.UTF_8
        ).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "La nueva contraseña supera el límite de 72 bytes"
            );
        }
    }

    private static IllegalArgumentException invalidToken() {
        return new IllegalArgumentException(
                "Enlace de recuperación inválido o expirado"
        );
    }
}
