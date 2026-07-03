package com.webempresarial.store.controller.admin;

import com.webempresarial.store.service.PlatformOperationsService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlatformOperationsController {

    private final PlatformOperationsService operationsService;

    public PlatformOperationsController(
            PlatformOperationsService operationsService
    ) {
        this.operationsService = operationsService;
    }

    @GetMapping("/admin/platform/operations")
    public String operations(Model model) {
        model.addAttribute("operations", operationsService.operations());
        return "admin/platform/operations";
    }
}