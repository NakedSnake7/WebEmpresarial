package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.event.AdminAccountCreatedEvent;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountInvitationResendServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminAccountActivationTokenRepository tokenRepository;

    @Mock
    private AdminAccountInvitationService invitationService;

    @Mock
    private AdminAccountActivationService
            activationService;

    private AdminAccountInvitationResendService service;





    @BeforeEach
    void setUp() {
        service =
                new AdminAccountInvitationResendService(
                        adminUserRepository,
                        tokenRepository,
                        invitationService,
                        activationService
                );
    }

    @Test
    void shouldResendPendingActivationInvitation()
            throws Exception {

        Store store = new Store();
        store.setId(4383L);
        store.setNombre("WebEmpresarial E2E");
        store.setDominio(
                "onboarding-e2e.web-empresarial.com"
        );

        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setEmail("owner@example.com");
        admin.setFullName("Jovani E2E");
        admin.setEnabled(false);
        admin.setStore(store);

        AdminAccountActivationToken token =
                new AdminAccountActivationToken();

        token.setAdminUser(admin);
        token.setToken("token-real");
        token.setCreatedAt(
                LocalDateTime.now().minusMinutes(10)
        );
        token.setExpiresAt(
                LocalDateTime.now().plusHours(23)
        );
        token.setUsed(false);

        when(adminUserRepository.findById(7L))
                .thenReturn(Optional.of(admin));

        when(
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                7L
                        )
        ).thenReturn(Optional.of(token));

        service.resend(7L);

        ArgumentCaptor<AdminAccountCreatedEvent>
                eventCaptor =
                ArgumentCaptor.forClass(
                        AdminAccountCreatedEvent.class
                );

        verify(invitationService)
                .sendActivationInvitation(
                        eventCaptor.capture()
                );

        AdminAccountCreatedEvent event =
                eventCaptor.getValue();

        assertThat(event.adminUserId())
                .isEqualTo(7L);

        assertThat(event.email())
                .isEqualTo("owner@example.com");

        assertThat(event.fullName())
                .isEqualTo("Jovani E2E");

        assertThat(event.storeName())
                .isEqualTo("WebEmpresarial E2E");

        assertThat(event.storeDomain())
                .isEqualTo(
                        "onboarding-e2e.web-empresarial.com"
                );

        assertThat(event.activationToken())
                .isEqualTo("token-real");
    }

    @Test
    void shouldRejectAlreadyActivatedAdmin() {

        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setEnabled(true);

        when(adminUserRepository.findById(7L))
                .thenReturn(Optional.of(admin));

        assertThatThrownBy(
                () -> service.resend(7L)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "La cuenta ya está activada"
                );

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(invitationService);
    }

    @Test
    void shouldCreateNewTokenWhenPreviousTokenExpired()
            throws Exception {

        Store store = new Store();
        store.setId(4383L);
        store.setNombre("WebEmpresarial E2E");
        store.setDominio(
                "onboarding-e2e.web-empresarial.com"
        );

        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setEmail("owner@example.com");
        admin.setFullName("Jovani E2E");
        admin.setEnabled(false);
        admin.setStore(store);

        AdminAccountActivationToken expiredToken =
                new AdminAccountActivationToken();

        expiredToken.setAdminUser(admin);
        expiredToken.setToken("expired-token");
        expiredToken.setCreatedAt(
                LocalDateTime.now().minusDays(2)
        );
        expiredToken.setExpiresAt(
                LocalDateTime.now().minusHours(1)
        );
        expiredToken.setUsed(false);

        AdminAccountActivationToken newToken =
                new AdminAccountActivationToken();

        newToken.setAdminUser(admin);
        newToken.setToken("new-token");
        newToken.setCreatedAt(
                LocalDateTime.now()
        );
        newToken.setExpiresAt(
                LocalDateTime.now().plusHours(24)
        );
        newToken.setUsed(false);

        when(adminUserRepository.findById(7L))
                .thenReturn(Optional.of(admin));

        when(
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                7L
                        )
        ).thenReturn(Optional.of(expiredToken));

        when(activationService.createToken(admin))
                .thenReturn(newToken);

        service.resend(7L);

        ArgumentCaptor<AdminAccountCreatedEvent>
                eventCaptor =
                ArgumentCaptor.forClass(
                        AdminAccountCreatedEvent.class
                );

        verify(activationService)
                .createToken(admin);

        verify(invitationService)
                .sendActivationInvitation(
                        eventCaptor.capture()
                );

        assertThat(
                eventCaptor.getValue()
                        .activationToken()
        ).isEqualTo("new-token");
    }
    @Test
    void shouldCreateNewTokenWhenNoPendingTokenExists()
            throws Exception {

        Store store = new Store();
        store.setId(4383L);
        store.setNombre("WebEmpresarial E2E");
        store.setDominio(
                "onboarding-e2e.web-empresarial.com"
        );

        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setEmail("owner@example.com");
        admin.setFullName("Jovani E2E");
        admin.setEnabled(false);
        admin.setStore(store);

        AdminAccountActivationToken newToken =
                new AdminAccountActivationToken();

        newToken.setAdminUser(admin);
        newToken.setToken("new-token");
        newToken.setCreatedAt(
                LocalDateTime.now()
        );
        newToken.setExpiresAt(
                LocalDateTime.now().plusHours(24)
        );
        newToken.setUsed(false);

        when(adminUserRepository.findById(7L))
                .thenReturn(Optional.of(admin));

        when(
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                7L
                        )
        ).thenReturn(Optional.empty());

        when(activationService.createToken(admin))
                .thenReturn(newToken);

        service.resend(7L);

        ArgumentCaptor<AdminAccountCreatedEvent>
                eventCaptor =
                ArgumentCaptor.forClass(
                        AdminAccountCreatedEvent.class
                );

        verify(activationService)
                .createToken(admin);

        verify(invitationService)
                .sendActivationInvitation(
                        eventCaptor.capture()
                );

        assertThat(
                eventCaptor.getValue()
                        .activationToken()
        ).isEqualTo("new-token");
    }
}
