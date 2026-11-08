package com.webempresarial.store.controller;

import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PilotLandingController {

    private final StoreResolver storeResolver;

    public PilotLandingController(StoreResolver storeResolver) {
        this.storeResolver = storeResolver;
    }

    @GetMapping("/piloto")
    public String showPilot(
            Model model,
            HttpServletRequest request
    ) {
        // La landing comercial pertenece a la plataforma,
        // no a un storefront de tenant.
        if (!storeResolver.isPlatformRequest(request)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            );
        }

        model.addAttribute("store", null);
        model.addAttribute("theme", "WebEmpresarial");
        model.addAttribute("showCart", false);
        model.addAttribute("showCheckout", false);
        model.addAttribute("showAuth", false);
        model.addAttribute("showScripts", true);

        model.addAttribute(
                "title",
                "WebEmpresarial™ | Piloto asistido de 30 días"
        );

        model.addAttribute(
                "description",
                "Evalúa WebEmpresarial con un piloto "
                + "pagado de 30 días y acompañamiento."
        );

        return "themes/WebEmpresarial/index";
    }
}
