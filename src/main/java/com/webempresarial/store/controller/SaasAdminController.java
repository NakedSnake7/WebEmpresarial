package com.webempresarial.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SaasAdminController {

    @GetMapping("/admin/saas")
    public String dashboard(Model model) {
        model.addAttribute("saasPage", true);
        return "admin/saas/dashboard";
    }
}