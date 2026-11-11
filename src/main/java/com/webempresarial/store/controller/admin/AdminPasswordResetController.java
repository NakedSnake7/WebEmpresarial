package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.AdminPasswordResetRateLimiter;
import com.webempresarial.store.service.AdminPasswordResetRequestDispatcher;
import com.webempresarial.store.service.AdminPasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Controller
@RequestMapping("/admin/password")
public class AdminPasswordResetController {

    private static final String FORGOT_VIEW =
            "admin/account/forgot-password";

    private static final String RESET_VIEW =
            "admin/account/reset-password";

    private static final String RESET_SESSION_KEY =
            "WEBEMPRESARIAL_ADMIN_PASSWORD_RESET_TOKEN";

    private static final String GENERIC_RESPONSE =
            "Si existe una cuenta asociada al correo, "
            + "recibirás instrucciones para recuperar tu acceso.";

    private static final String INVALID_LINK =
            "El enlace no es válido o ha expirado. "
            + "Puedes solicitar uno nuevo.";

    private final AdminPasswordResetRequestDispatcher dispatcher;
    private final AdminPasswordResetService resetService;
    private final AdminPasswordResetRateLimiter rateLimiter;

    public AdminPasswordResetController(
            AdminPasswordResetRequestDispatcher dispatcher,
            AdminPasswordResetService resetService,
            AdminPasswordResetRateLimiter rateLimiter
    ) {
        this.dispatcher = dispatcher;
        this.resetService = resetService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/forgot")
    public String forgotForm(
            HttpServletResponse response
    ) {
        secureResponse(response);
        return FORGOT_VIEW;
    }

    @PostMapping("/forgot")
    public String requestRecovery(
            @RequestParam(value = "email", required = false)
            String email,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        secureResponse(response);

        if (rateLimiter.allowRecoveryRequest(
                request.getRemoteAddr()
        )) {
            dispatcher.dispatch(email);
        }

        /*
         * Tanto las cuentas existentes como las
         * desconocidas reciben la misma respuesta.
         * También se oculta el agotamiento del límite.
         */
        return "redirect:/admin/password/forgot?sent";
    }

    @GetMapping("/reset")
    public String resetForm(
            @RequestParam(value = "token", required = false)
            String token,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        secureResponse(response);

        if (token != null) {

            HttpSession existing =
                    request.getSession(false);

            if (isCanonicalToken(token)) {

                request.getSession(true).setAttribute(
                        RESET_SESSION_KEY,
                        token
                );

            } else if (existing != null) {

                existing.removeAttribute(
                        RESET_SESSION_KEY
                );
            }

            /*
             * Quitar el token de la URL antes de
             * renderizar el formulario.
             *
             * Nunca añadirlo al modelo ni a un campo
             * hidden del HTML.
             */
            return "redirect:/admin/password/reset";
        }

        if (sessionToken(request) == null) {
            model.addAttribute(
                    "passwordError",
                    INVALID_LINK
            );
        }

        return RESET_VIEW;
    }

    @PostMapping("/reset")
    public String completeRecovery(
            @RequestParam(
                value = "newPassword",
                required = false
            )
            String newPassword,
            @RequestParam(
                value = "confirmPassword",
                required = false
            )
            String confirmPassword,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            Model model
    ) {
        secureResponse(response);

        if (!rateLimiter.allowRecoveryCompletion(
                request.getRemoteAddr()
        )) {

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            model.addAttribute(
                    "passwordError",
                    "Demasiados intentos. Inténtalo más tarde."
            );

            return RESET_VIEW;
        }

        String rawToken = sessionToken(request);

        if (rawToken == null) {
            model.addAttribute(
                    "passwordError",
                    INVALID_LINK
            );
            return RESET_VIEW;
        }

        if (newPassword == null
                || !Objects.equals(
                        newPassword,
                        confirmPassword
                )) {

            model.addAttribute(
                    "passwordError",
                    "Las contraseñas no coinciden"
            );

            return RESET_VIEW;
        }

        if (newPassword.length() < 12
                || newPassword.getBytes(
                        StandardCharsets.UTF_8
                ).length > 72) {

            model.addAttribute(
                    "passwordError",
                    "La contraseña debe tener al menos "
                    + "12 caracteres y no superar 72 bytes."
            );

            return RESET_VIEW;
        }

        try {

            resetService.completeRecovery(
                    rawToken,
                    newPassword
            );

        } catch (IllegalArgumentException ex) {

            /*
             * No distinguir públicamente entre un
             * token inválido y otros rechazos del
             * servicio.
             */
            model.addAttribute(
                    "passwordError",
                    "No fue posible restablecer la contraseña. "
                    + "Comprueba el enlace o utiliza otra "
                    + "contraseña."
            );

            return RESET_VIEW;
        }

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.removeAttribute(
                    RESET_SESSION_KEY
            );
        }

        new SecurityContextLogoutHandler().logout(
                request,
                response,
                authentication
        );

        return "redirect:/admin/login?passwordReset";
    }

    private static String sessionToken(
            HttpServletRequest request
    ) {
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object token =
                session.getAttribute(RESET_SESSION_KEY);

        if (!(token instanceof String value)
                || !isCanonicalToken(value)) {
            return null;
        }

        return value;
    }

    private static boolean isCanonicalToken(
            String value
    ) {
        return value != null
                && value.length() == 43
                && value.matches("[A-Za-z0-9_-]{43}");
    }

    private static void secureResponse(
            HttpServletResponse response
    ) {
        response.setHeader(
                "Cache-Control",
                "no-store, max-age=0"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setHeader(
                "Referrer-Policy",
                "no-referrer"
        );

        response.setHeader(
                "X-Robots-Tag",
                "noindex, nofollow"
        );

        response.setHeader(
                "Content-Security-Policy",
                "default-src 'none'; "
                + "style-src 'unsafe-inline'; "
                + "form-action 'self'; "
                + "base-uri 'none'; "
                + "frame-ancestors 'none'"
        );
    }
}
