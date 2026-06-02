package com.webempresarial.store.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webempresarial.store.dto.saas.SaasDashboardDTO;
import com.webempresarial.store.service.SaasDashboardService;

@RestController
@RequestMapping("/api/admin/saas")
public class SaasDashboardRestController {

    private final SaasDashboardService saasDashboardService;

    public SaasDashboardRestController(SaasDashboardService saasDashboardService) {
        this.saasDashboardService = saasDashboardService;
    }

    @GetMapping
    public SaasDashboardDTO getDashboard() {
        return saasDashboardService.getDashboard();
    }
}