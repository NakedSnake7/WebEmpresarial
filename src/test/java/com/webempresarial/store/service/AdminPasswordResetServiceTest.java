package com.webempresarial.store.service;

import com.webempresarial.store.event.AdminPasswordResetIssuedEvent;
import com.webempresarial.store.model.AdminPasswordResetToken;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminPasswordResetTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPasswordResetServiceTest {

    private static final String EMAIL =
            "admin@example.invalid";

    private static final String RAW =
            "A".repeat(43);

    private static final String HASH =
            "ab".repeat(32);

    private static final String NEW_PASSWORD =
            "NuevaClaveSegura#2026";

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 10, 12, 0);

    @Mock
    private AdminUserRepository admins;

    @Mock
    private AdminPasswordResetTokenRepository tokens;

    @Mock
    private AdminPasswordResetTokenCodec codec;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private ApplicationEventPublisher publisher;

    private AdminPasswordResetService service;
    private AdminUser admin;

    @BeforeEach
    void setUp() {

        Clock clock = Clock.fixed(
                Instant.parse("2026-11-10T12:00:00Z"),
                ZoneOffset.UTC
        );

        service = new AdminPasswordResetService(
                admins,
                tokens,
                codec,
                encoder,
                publisher,
                clock
        );

        admin = new AdminUser();
        admin.setId(7L);
        admin.setFullName("Admin Test");
        admin.setEmail(EMAIL);
        admin.setPassword("existing-hash");
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
    }

    private AdminPasswordResetToken token(
            LocalDateTime created
    ) {
        return AdminPasswordResetToken.issue(
                admin,
                HASH,
                created,
                created.plusMinutes(20)
        );
    }

    @Test
    void unknownEmailDoesNotPublishEvent() {

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.empty());

        service.requestRecovery(EMAIL);

        verifyNoInteractions(tokens, codec, publisher);
    }

    @Test
    void disabledAccountDoesNotIssueToken() {

        admin.setEnabled(false);

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        service.requestRecovery(EMAIL);

        verifyNoInteractions(tokens, codec, publisher);
    }

    @Test
    void issuesHashedTokenAndRedactedEvent() {

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByAdminUserIdAndConsumedAtIsNull(7L))
                .thenReturn(List.of());

        when(codec.generateRawToken()).thenReturn(RAW);
        when(codec.hashToken(RAW)).thenReturn(HASH);

        service.requestRecovery(EMAIL);

        ArgumentCaptor<AdminPasswordResetToken> saved =
                ArgumentCaptor.forClass(
                        AdminPasswordResetToken.class
                );

        verify(tokens).save(saved.capture());

        assertEquals(
                HASH,
                saved.getValue().getTokenHash()
        );

        assertEquals(
                NOW.plusMinutes(20),
                saved.getValue().getExpiresAt()
        );

        ArgumentCaptor<Object> events =
                ArgumentCaptor.forClass(Object.class);

        verify(publisher).publishEvent(events.capture());

        var event = assertInstanceOf(
                AdminPasswordResetIssuedEvent.class,
                events.getValue()
        );

        assertEquals(RAW, event.rawToken());
        assertFalse(event.toString().contains(RAW));
    }

    @Test
    void cooldownSuppressesSecondIssue() {

        var pending = token(NOW.minusMinutes(1));

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByAdminUserIdAndConsumedAtIsNull(7L))
                .thenReturn(List.of(pending));

        service.requestRecovery(EMAIL);

        verifyNoInteractions(codec, publisher);
        assertNull(pending.getConsumedAt());
    }

    @Test
    void olderActiveTokenIsInvalidatedOnNewIssue() {

        var previous = token(NOW.minusMinutes(6));

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByAdminUserIdAndConsumedAtIsNull(7L))
                .thenReturn(List.of(previous));

        when(codec.generateRawToken()).thenReturn(RAW);
        when(codec.hashToken(RAW)).thenReturn(HASH);

        service.requestRecovery(EMAIL);

        assertEquals(NOW, previous.getConsumedAt());

        verify(tokens, times(2)).save(
                any(AdminPasswordResetToken.class)
        );

        verify(publisher).publishEvent(any(Object.class));
    }

    @Test
    void completionRotatesCredentialsAndInvalidatesOtherToken() {

        var current = token(NOW.minusMinutes(2));

        var another = AdminPasswordResetToken.issue(
                admin,
                "cd".repeat(32),
                NOW.minusMinutes(3),
                NOW.plusMinutes(17)
        );

        when(codec.hashToken(RAW)).thenReturn(HASH);

        when(tokens.findAdminEmailByTokenHash(HASH))
                .thenReturn(Optional.of(EMAIL));

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByTokenHashForUpdate(HASH))
                .thenReturn(Optional.of(current));

        when(tokens.findByAdminUserIdAndConsumedAtIsNull(7L))
                .thenReturn(List.of(current, another));

        when(encoder.encode(NEW_PASSWORD))
                .thenReturn("new-hash");

        service.completeRecovery(RAW, NEW_PASSWORD);

        assertEquals("new-hash", admin.getPassword());
        assertEquals(1L, admin.getCredentialVersion());

        assertEquals(NOW, current.getConsumedAt());
        assertEquals(NOW, another.getConsumedAt());

        verify(admins).save(admin);
    }

    @Test
    void consumedTokenCannotBeReused() {

        var current = token(NOW.minusMinutes(2));
        current.consume(NOW.minusMinutes(1));

        when(codec.hashToken(RAW)).thenReturn(HASH);

        when(tokens.findAdminEmailByTokenHash(HASH))
                .thenReturn(Optional.of(EMAIL));

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByTokenHashForUpdate(HASH))
                .thenReturn(Optional.of(current));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.completeRecovery(
                        RAW,
                        NEW_PASSWORD
                )
        );

        assertEquals(0L, admin.getCredentialVersion());
        verify(admins, never()).save(any(AdminUser.class));
    }

    @Test
    void expiredTokenCannotChangePassword() {

        var expired = token(NOW.minusMinutes(21));

        when(codec.hashToken(RAW)).thenReturn(HASH);

        when(tokens.findAdminEmailByTokenHash(HASH))
                .thenReturn(Optional.of(EMAIL));

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByTokenHashForUpdate(HASH))
                .thenReturn(Optional.of(expired));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.completeRecovery(
                        RAW,
                        NEW_PASSWORD
                )
        );

        assertEquals(0L, admin.getCredentialVersion());
    }

    @Test
    void credentialRotationRejectsOldToken() {

        var issued = token(NOW.minusMinutes(2));

        admin.rotateCredentialVersion();

        when(codec.hashToken(RAW)).thenReturn(HASH);

        when(tokens.findAdminEmailByTokenHash(HASH))
                .thenReturn(Optional.of(EMAIL));

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByTokenHashForUpdate(HASH))
                .thenReturn(Optional.of(issued));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.completeRecovery(
                        RAW,
                        NEW_PASSWORD
                )
        );

        assertEquals(
                1L,
                admin.getCredentialVersion()
        );

        assertEquals("existing-hash", admin.getPassword());
        assertNull(issued.getConsumedAt());

        verify(encoder, never()).encode(anyString());
        verify(admins, never()).save(any(AdminUser.class));
    }


    @Test
    void rotatedCredentialsAllowImmediateNewRecovery() {

        var previous = token(NOW.minusMinutes(1));

        admin.rotateCredentialVersion();

        when(admins.findByEmailForUpdate(EMAIL))
                .thenReturn(Optional.of(admin));

        when(tokens.findByAdminUserIdAndConsumedAtIsNull(7L))
                .thenReturn(List.of(previous));

        when(codec.generateRawToken())
                .thenReturn(RAW);

        when(codec.hashToken(RAW))
                .thenReturn("ef".repeat(32));

        service.requestRecovery(EMAIL);

        assertEquals(
                NOW,
                previous.getConsumedAt()
        );

        ArgumentCaptor<AdminPasswordResetToken> saved =
                ArgumentCaptor.forClass(
                        AdminPasswordResetToken.class
                );

        verify(tokens, times(2)).save(saved.capture());

        AdminPasswordResetToken newest =
                saved.getAllValues().stream()
                        .filter(token -> token != previous)
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                1L,
                newest.getCredentialVersionAtIssue()
        );

        verify(publisher).publishEvent(any(Object.class));
    }

}
