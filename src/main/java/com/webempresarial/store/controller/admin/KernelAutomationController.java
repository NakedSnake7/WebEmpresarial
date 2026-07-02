package com.webempresarial.store.controller.admin;

import com.webempresarial.store.feature.registry.AutomationRegistry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KernelAutomationController {

    private final AutomationRegistry automationRegistry;

    public KernelAutomationController(AutomationRegistry automationRegistry) {
        this.automationRegistry = automationRegistry;
    }

    @GetMapping("/admin/automations/kernel")
    public String automations(Model model) {
        model.addAttribute("automations", automationRegistry.all());
        return "admin/automations/kernel";
    }
}