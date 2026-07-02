package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.PlatformExplorerService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlatformConsoleController {

    private final PlatformExplorerService platformExplorerService;

    public PlatformConsoleController(
            PlatformExplorerService platformExplorerService
    ) {
        this.platformExplorerService = platformExplorerService;
    }

    @GetMapping("/admin/platform")
    public String platform(Model model) {
        model.addAttribute("console", platformExplorerService.console());
        return "admin/platform/index";
    }
}