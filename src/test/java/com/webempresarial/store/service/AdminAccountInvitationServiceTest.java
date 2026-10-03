package com.webempresarial.store.service;

import com.webempresarial.store.event.AdminAccountCreatedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAccountInvitationServiceTest {

    @Mock
    private EmailService emailService;

    private AdminAccountInvitationService invitationService;

    @BeforeEach
    void setUp() {

        invitationService =
                new AdminAccountInvitationService(
                        emailService
                );

        ReflectionTestUtils.setField(
                invitationService,
                "baseUrl",
                "https://web-empresarial.com"
        );
    }

    @Test
    void shouldSendActivationEmail()
            throws IOException {

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        101L,
                        "owner@acme.test",
                        "Alice Owner",
                        "ACME",
                        "acme.web-empresarial.com",
                        "activation-token-123"
                );

        invitationService
                .sendActivationInvitation(event);

        ArgumentCaptor<String> recipientCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> subjectCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> htmlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService)
                .enviarCorreoHTML(
                        recipientCaptor.capture(),
                        subjectCaptor.capture(),
                        htmlCaptor.capture()
                );

        assertThat(recipientCaptor.getValue())
                .isEqualTo("owner@acme.test");

        assertThat(subjectCaptor.getValue())
                .containsIgnoringCase("activa");

        String html =
                htmlCaptor.getValue();

        assertThat(html)
                .contains("Alice Owner");

        assertThat(html)
                .contains("ACME");

        assertThat(html)
                .contains(
                        "https://acme.web-empresarial.com"
                        + "/admin/activate?token="
                        + "activation-token-123"
                );

        assertThat(html)
                .contains("24 horas");
    }

    @Test
    void shouldEscapeUserControlledHtml()
            throws IOException {

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        102L,
                        "owner@test.com",
                        "<script>alert('x')</script>",
                        "<b>Store</b>",
                        "store.web-empresarial.com",
                        "safe-token"
                );

        invitationService
                .sendActivationInvitation(event);

        ArgumentCaptor<String> htmlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService)
                .enviarCorreoHTML(
                        org.mockito.ArgumentMatchers.eq(
                                "owner@test.com"
                        ),
                        org.mockito.ArgumentMatchers.anyString(),
                        htmlCaptor.capture()
                );

        String html =
                htmlCaptor.getValue();

        assertThat(html)
                .doesNotContain(
                        "<script>alert('x')</script>"
                );

        assertThat(html)
                .doesNotContain(
                        "<b>Store</b>"
                );

        assertThat(html)
                .contains("&lt;script&gt;");

        assertThat(html)
                .contains("&lt;b&gt;Store&lt;/b&gt;");
    }

    @Test
    void shouldUseLocalBaseUrlInDevelopment()
            throws IOException {

        ReflectionTestUtils.setField(
                invitationService,
                "baseUrl",
                "http://localhost:8080"
        );

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        103L,
                        "owner@local.test",
                        "Local Owner",
                        "Local Store",
                        "acme.web-empresarial.com",
                        "local-token"
                );

        invitationService
                .sendActivationInvitation(event);

        ArgumentCaptor<String> htmlCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(emailService)
                .enviarCorreoHTML(
                        org.mockito.ArgumentMatchers.eq(
                                "owner@local.test"
                        ),
                        org.mockito.ArgumentMatchers.anyString(),
                        htmlCaptor.capture()
                );

        assertThat(htmlCaptor.getValue())
                .contains(
                        "http://localhost:8080"
                        + "/admin/activate?token="
                        + "local-token"
                );

        assertThat(htmlCaptor.getValue())
                .doesNotContain(
                        "https://acme.web-empresarial.com"
                        + "/admin/activate"
                );
    }
}