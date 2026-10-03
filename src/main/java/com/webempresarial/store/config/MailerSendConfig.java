package com.webempresarial.store.config;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailerSendConfig {

    @Value("${mailersend.api.key}")
    private String apiKey;

    @Value("${mailersend.api.url:https://api.mailersend.com/v1/email}")
    private String apiUrl;

    @Value("${mailersend.from.email}")
    private String fromEmail;

    @Value("${mailersend.from.name:WebEmpresarial}")
    private String fromName;

    @Value("${mailersend.reply.to}")
    private String replyTo;

    /* ========================
       GETTERS
       ======================== */

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public String getReplyTo() {
        return replyTo;
    }

    /* ========================
       VALIDACIÓN OPCIONAL
       ======================== */

    public void validate() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("MAILERSEND_API_KEY no configurada");
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("mailersend.from.email no configurado");
        }

        if (replyTo == null || replyTo.isBlank()) {
            throw new IllegalStateException("mailersend.reply.to no configurado");
        }
    }
}
