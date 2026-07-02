package com.webempresarial.store.controller.admin;

import com.webempresarial.store.feature.registry.PermissionRegistry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PermissionController {

    private final PermissionRegistry permissionRegistry;

    public PermissionController(PermissionRegistry permissionRegistry) {
        this.permissionRegistry = permissionRegistry;
    }

    @GetMapping("/admin/permissions")
    public String permissions(Model model) {
        model.addAttribute("permissions", permissionRegistry.all());
        return "admin/permissions/index";
    }
}