package com.webempresarial.store.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webempresarial.store.event.AdminAccountCreatedEvent;

@Service
public class AdminAccountInvitationService {

    private final EmailService emailService;

    @Value("${app.base-url:https://web-empresarial.com}")
    private String baseUrl;

    public AdminAccountInvitationService(
            EmailService emailService
    ) {
        this.emailService = emailService;
    }
    private String resolveActivationBaseUrl(
            AdminAccountCreatedEvent event
    ) {

        /*
         * En desarrollo se conserva localhost.
         *
         * El dominio real del tenant no necesariamente
         * será resoluble desde la máquina local.
         */
        if (baseUrl.contains("localhost")
                || baseUrl.contains("127.0.0.1")) {

            return removeTrailingSlash(baseUrl);
        }

        String storeDomain =
                event.storeDomain();

        if (storeDomain == null
                || storeDomain.isBlank()) {

            return removeTrailingSlash(baseUrl);
        }

        String normalized =
                storeDomain.trim();

        if (normalized.startsWith("https://")
                || normalized.startsWith("http://")) {

            return removeTrailingSlash(normalized);
        }

        return "https://" + normalized;
    }

    private String removeTrailingSlash(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return "";
        }

        String normalized =
                value.trim();

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return normalized;
    }

    public void sendActivationInvitation(
            AdminAccountCreatedEvent event
    ) throws IOException {

        String activationUrl =
                resolveActivationBaseUrl(event)
                + "/admin/activate?token="
                + event.activationToken();

        String subject =
                "Activa tu cuenta de WebEmpresarial";

        String html = buildEmail(
                event,
                activationUrl
        );

        emailService.enviarCorreoHTML(
                event.email(),
                subject,
                html
        );
    }

    private String buildEmail(
            AdminAccountCreatedEvent event,
            String activationUrl
    ) {

        return """
                <!DOCTYPE html>
                <html lang="es">
                <body style="
                    font-family: Arial, sans-serif;
                    background:#f5f5f5;
                    padding:30px;
                ">

                    <div style="
                        max-width:600px;
                        margin:auto;
                        background:white;
                        padding:32px;
                        border-radius:12px;
                    ">

                        <h2>
                            Bienvenido a WebEmpresarial
                        </h2>

                        <p>
                            Hola %s,
                        </p>

                        <p>
                            Tu tienda <strong>%s</strong>
                            ya está lista.
                        </p>

                        <p>
                            Para comenzar, crea la contraseña
                            de tu cuenta administrativa.
                        </p>

                        <p style="margin:32px 0;">
                            <a href="%s"
                               style="
                                   background:#111;
                                   color:white;
                                   padding:14px 22px;
                                   text-decoration:none;
                                   border-radius:8px;
                                   display:inline-block;
                               ">
                                Activar mi cuenta
                            </a>
                        </p>

                        <p>
                            Este enlace expirará en 24 horas.
                        </p>

                        <p style="
                            color:#777;
                            font-size:13px;
                        ">
                            Si no realizaste esta compra,
                            puedes ignorar este mensaje.
                        </p>

                    </div>

                </body>
                </html>
                """.formatted(
                escapeHtml(event.fullName()),
                escapeHtml(event.storeName()),
                escapeHtml(activationUrl)
        );
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
