package com.webempresarial.store.event;

import com.webempresarial.store.service.AdminAccountInvitationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountInvitationListenerTest {

    @Mock
    private AdminAccountInvitationService invitationService;

    @Test
    void shouldSendActivationInvitation() throws Exception {

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        101L,
                        "owner@acme.test",
                        "Alice Owner",
                        "ACME",
                        "acme.web-empresarial.com",
                        "activation-token-123"
                );

        AdminAccountInvitationListener listener =
                new AdminAccountInvitationListener(
                        invitationService
                );

        listener.onAdminAccountCreated(event);

        verify(invitationService)
                .sendActivationInvitation(event);
    }

    @Test
    void shouldNotPropagateEmailFailure()
            throws Exception {

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        101L,
                        "owner@acme.test",
                        "Alice Owner",
                        "ACME",
                        "acme.web-empresarial.com",
                        "activation-token-123"
                );

        doThrow(
                new IOException("MailerSend unavailable")
        )
        .when(invitationService)
        .sendActivationInvitation(event);

        AdminAccountInvitationListener listener =
                new AdminAccountInvitationListener(
                        invitationService
                );

        assertThatCode(
                () -> listener.onAdminAccountCreated(event)
        )
        .doesNotThrowAnyException();

        verify(invitationService)
                .sendActivationInvitation(event);
    }
}