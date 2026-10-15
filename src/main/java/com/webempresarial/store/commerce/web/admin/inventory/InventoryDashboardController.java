package com.webempresarial.store.commerce.web.admin.inventory;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryDashboardService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryDashboardController {

    private final InventoryDashboardService dashboardService;
    private final StoreResolver storeResolver;
    private final FeatureAccessService featureAccessService;

    public InventoryDashboardController(
            InventoryDashboardService dashboardService,
            StoreResolver storeResolver,
            FeatureAccessService featureAccessService
    ) {
        this.dashboardService = dashboardService;
        this.storeResolver = storeResolver;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping
    public String dashboard(
            HttpServletRequest request,
            Model model
    ) {
        Store store =
                storeResolver.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.INVENTORY
        );

        model.addAttribute(
                "dashboard",
                dashboardService.getDashboard(store)
        );

        return "admin/inventory/dashboard";
    }
}