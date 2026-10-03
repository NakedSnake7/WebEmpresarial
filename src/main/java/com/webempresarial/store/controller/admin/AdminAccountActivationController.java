package com.webempresarial.store.controller.admin;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.service.AdminAccountActivationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/activate")
public class AdminAccountActivationController {

    private final AdminAccountActivationService activationService;

    public AdminAccountActivationController(
            AdminAccountActivationService activationService
    ) {
        this.activationService = activationService;
    }

    @GetMapping
    public String showActivationForm(
            @RequestParam String token,
            Model model
    ) {

        try {

            AdminAccountActivationToken activation =
                    activationService.validate(token);

            model.addAttribute(
                    "token",
                    token
            );

            model.addAttribute(
                    "email",
                    activation
                            .getAdminUser()
                            .getEmail()
            );

            return "admin/activate";

        } catch (IllegalArgumentException ex) {

            model.addAttribute(
                    "activationError",
                    ex.getMessage()
            );

            return "admin/activate";
        }
    }

    @PostMapping
    public String activate(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model
    ) {

        if (!password.equals(confirmPassword)) {

            model.addAttribute(
                    "token",
                    token
            );

            model.addAttribute(
                    "activationError",
                    "Las contraseñas no coinciden"
            );

            return "admin/activate";
        }

        try {

            activationService.activate(
                    token,
                    password
            );

            return "redirect:/admin/login?activated";

        } catch (IllegalArgumentException ex) {

            model.addAttribute(
                    "token",
                    token
            );

            model.addAttribute(
                    "activationError",
                    ex.getMessage()
            );

            return "admin/activate";
        }
    }
}