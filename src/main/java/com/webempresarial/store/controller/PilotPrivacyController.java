package com.webempresarial.store.controller;

import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/** Aviso exclusivo de la plataforma: no reutilizar páginas de otros tenants. */
@Controller
public class PilotPrivacyController {

    private final StoreResolver storeResolver;
    private final PilotPrivacyReadiness readiness;

    public PilotPrivacyController(StoreResolver storeResolver,
            PilotPrivacyReadiness readiness) {
        this.storeResolver = storeResolver;
        this.readiness = readiness;
    }

    @GetMapping("/piloto/privacidad")
    public String privacy(Model model, HttpServletRequest request) {
        if (!storeResolver.isPlatformRequest(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        readiness.requireApproved();
        model.addAttribute("responsibleName", readiness.responsibleName());
        model.addAttribute("responsibleAddress", readiness.responsibleAddress());
        model.addAttribute("privacyEmail", readiness.privacyEmail());
        return "themes/WebEmpresarial/pilot-privacy";
    }
}
