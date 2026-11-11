package com.webempresarial.store.service;

import com.webempresarial.store.event.AdminPasswordResetIssuedEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

@Service
public class AdminPasswordResetMailService {

    private final EmailService emailService;
    private final String trustedBaseUrl;

    public AdminPasswordResetMailService(
            EmailService emailService,
            @Value("${app.base-url}") String baseUrl
    ) {
        this.emailService = emailService;
        this.trustedBaseUrl = validateBaseUrl(baseUrl);
    }

    public void sendRecoveryEmail(
            AdminPasswordResetIssuedEvent event
    ) throws IOException {

        if (event == null
                || event.email() == null
                || event.email().isBlank()
                || event.rawToken() == null
                || !event.rawToken().matches(
                        "[A-Za-z0-9_-]{43}"
                )) {
            throw new IllegalArgumentException(
                    "Evento de recuperación inválido"
            );
        }

        /*
         * Nunca construir la URL desde Host,
         * X-Forwarded-Host o datos del request.
         */
        String resetUrl =
                trustedBaseUrl
                + "/admin/password/reset?token="
                + event.rawToken();

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <body style="font-family:Arial,sans-serif;
                         background:#f5f5f5;padding:30px;">
              <div style="max-width:600px;margin:auto;
                          background:#fff;padding:32px;
                          border-radius:12px;">
                <h2>Recuperación de contraseña</h2>
                <p>Hola %s,</p>
                <p>Recibimos una solicitud para restablecer
                   la contraseña de tu cuenta administrativa
                   de WebEmpresarial.</p>
                <p>
                  <a href="%s"
                     style="background:#111;color:#fff;
                            padding:14px 22px;
                            text-decoration:none;
                            border-radius:8px;
                            display:inline-block;">
                    Restablecer contraseña
                  </a>
                </p>
                <p>El enlace caduca en 20 minutos
                   y solo puede utilizarse una vez.</p>
                <p>Si no solicitaste este cambio,
                   puedes ignorar el mensaje.</p>
              </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(event.fullName()),
                escapeHtml(resetUrl)
            );

        emailService.enviarCorreoHTML(
                event.email(),
                "Recupera tu contraseña de WebEmpresarial",
                html
        );
    }

    private static String validateBaseUrl(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "app.base-url no configurada"
            );
        }

        String normalized = value.trim();
        URI uri;

        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "app.base-url inválida"
            );
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getRawPath();

        if (scheme == null
                || host == null
                || host.isBlank()
                || uri.getUserInfo() != null
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || (path != null
                    && !path.isBlank()
                    && !"/".equals(path))) {

            throw new IllegalStateException(
                    "app.base-url debe ser un origen válido"
            );
        }

        String lowerScheme =
                scheme.toLowerCase(Locale.ROOT);

        String lowerHost =
                host.toLowerCase(Locale.ROOT);

        boolean localDevelopment =
                lowerHost.equals("localhost")
                || lowerHost.equals("127.0.0.1")
                || lowerHost.equals("::1")
                || lowerHost.endsWith(".local");

        if (!lowerScheme.equals("https")
                && !(lowerScheme.equals("http")
                     && localDevelopment)) {

            throw new IllegalStateException(
                    "La URL de recuperación requiere HTTPS"
            );
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }

    private static String escapeHtml(String value) {

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
