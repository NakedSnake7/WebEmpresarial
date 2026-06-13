package com.webempresarial.store.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpgradeController {

    @GetMapping("/admin/upgrade")
    public String upgrade(
            @RequestParam(required = false) String feature,
            Model model
    ) {

        model.addAttribute("feature", feature);

        return "admin/subscription/upgrade";
    }
}