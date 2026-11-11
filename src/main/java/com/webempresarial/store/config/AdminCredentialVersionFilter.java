package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

public final class AdminCredentialVersionFilter
        extends OncePerRequestFilter {

    public static final String SESSION_KEY =
            "WEBEMPRESARIAL_ADMIN_CREDENTIAL_VERSION";

    private final AdminUserRepository adminUserRepository;

    public AdminCredentialVersionFilter(
            AdminUserRepository adminUserRepository
    ) {
        this.adminUserRepository = adminUserRepository;
    }

    /*
     * Ruta relativa al contexto de la aplicación.
     * Compatible con MockMvc y despliegues bajo context-path.
     */
    private String pathWithinApplication(
            HttpServletRequest request
    ) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null
                && !contextPath.isEmpty()
                && (uri.equals(contextPath)
                    || uri.startsWith(contextPath + "/"))) {

            return uri.substring(contextPath.length());
        }

        return uri;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path = pathWithinApplication(request);

        return path.equals("/admin/login")
                || path.equals("/admin/activate")
                || path.startsWith("/admin/activate/")
                || path.equals("/admin/password/forgot")
                || path.equals("/admin/password/reset");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !hasAdminRole(authentication)) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        Object sessionVersion = session == null
                ? null
                : session.getAttribute(SESSION_KEY);

        String email = authentication.getName();

        Optional<AdminUser> admin = Optional.empty();

        if (email != null && !email.isBlank()) {
            admin = adminUserRepository.findByEmail(
                    email.trim().toLowerCase(Locale.ROOT)
            );
        }

        boolean valid =
                sessionVersion instanceof Long
                && admin.isPresent()
                && admin.get().isEnabled()
                && ((Long) sessionVersion).longValue()
                        == admin.get().getCredentialVersion();

        if (!valid) {
            new SecurityContextLogoutHandler().logout(
                    request,
                    response,
                    authentication
            );

            if (pathWithinApplication(request).startsWith("/api/")) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED
                );
            } else {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/login?sessionExpired"
                );
            }

            return;
        }

        chain.doFilter(request, response);
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(role ->
                        role.equals("ROLE_SUPER_ADMIN")
                        || role.equals("ROLE_STORE_OWNER")
                        || role.equals("ROLE_STORE_ADMIN")
                        || role.equals("ROLE_STORE_STAFF")
                        || role.equals("ROLE_SALES")
                );
    }
}
