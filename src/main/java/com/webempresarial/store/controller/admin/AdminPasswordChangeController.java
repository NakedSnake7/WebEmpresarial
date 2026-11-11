package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.AdminPasswordChangeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/account/password")
public class AdminPasswordChangeController {

    private static final String VIEW =
            "admin/account/change-password";

    private final AdminPasswordChangeService service;

    public AdminPasswordChangeController(
            AdminPasswordChangeService service
    ) {
        this.service = service;
    }

    @GetMapping
    public String showForm() {
        return VIEW;
    }

    @PostMapping
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute(
                    "passwordError",
                    "Las contraseñas nuevas no coinciden"
            );
            return VIEW;
        }

        try {
            service.changeOwnPassword(
                    authentication == null
                            ? null
                            : authentication.getName(),
                    currentPassword,
                    newPassword
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute(
                    "passwordError",
                    ex.getMessage()
            );
            return VIEW;
        }

        /*
         * Cerramos la sesión actual después de confirmar
         * que la contraseña fue actualizada.
         *
         * La revocación de otras sesiones abiertas requiere
         * un mecanismo adicional y no se presupone aquí.
         */
        new SecurityContextLogoutHandler().logout(
                request,
                response,
                authentication
        );

        return "redirect:/admin/login?passwordChanged";
    }
}
