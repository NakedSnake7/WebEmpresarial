package com.webempresarial.store.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.InventoryDashboardService;
import com.webempresarial.store.theme.StoreResolver;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryDashboardController {

    private final InventoryDashboardService dashboardService;
    private final StoreResolver storeResolver;

    public InventoryDashboardController(
            InventoryDashboardService dashboardService,
            StoreResolver storeResolver
    ) {
        this.dashboardService = dashboardService;
        this.storeResolver = storeResolver;
    }

    @GetMapping
    public String dashboard(
            HttpServletRequest request,
            Model model
    ) {
        Store store =
                storeResolver.getCurrentStore(request);

        model.addAttribute(
                "dashboard",
                dashboardService.getDashboard(store)
        );

        return "admin/inventory/dashboard";
    }
}