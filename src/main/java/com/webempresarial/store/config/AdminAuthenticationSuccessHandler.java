package com.webempresarial.store.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final AdminUserRepository adminUserRepository;
    @Value("${app.environment:prod}")
    private String environment;

    @Value("${server.port:8080}")
    private int serverPort;

    public AdminAuthenticationSuccessHandler(
            AdminUserRepository adminUserRepository
    ) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        AdminUser admin =
                adminUserRepository
                .findWithStoreByEmail(
                                authentication
                                        .getName()
                                        .trim()
                                        .toLowerCase()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Administrador autenticado no encontrado"
                                )
                        );

        /*
         * SUPER_ADMIN pertenece a la plataforma y no
         * necesita contexto de tenant.
         */
        if (admin.getRole() == AdminRole.SUPER_ADMIN) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        Store store = admin.getStore();

        if (store == null
                || store.getDominio() == null
                || store.getDominio().isBlank()) {

            throw new IllegalStateException(
                    "El administrador no tiene una tienda válida"
            );
        }

        String domain =
                normalizeDomain(store.getDominio());

        response.sendRedirect(
                resolveDashboardUrl(domain)
        );
    }

    private String resolveDashboardUrl(String domain) {

        if ("dev".equalsIgnoreCase(environment)
                || "local".equalsIgnoreCase(environment)) {

            return "http://"
                    + domain
                    + ":"
                    + serverPort
                    + "/admin/dashboard";
        }

        return "https://"
                + domain
                + "/admin/dashboard";
    }

    private String normalizeDomain(String domain) {

        String normalized =
                domain.trim().toLowerCase();

        if (normalized.startsWith("https://")) {
            normalized =
                    normalized.substring(
                            "https://".length()
                    );
        } else if (normalized.startsWith("http://")) {
            normalized =
                    normalized.substring(
                            "http://".length()
                    );
        }

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return normalized;
    }
}
