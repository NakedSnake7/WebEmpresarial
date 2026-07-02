package com.webempresarial.store.controller.admin;

import com.webempresarial.store.feature.registry.MarketplaceRegistry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MarketplaceController {

    private final MarketplaceRegistry marketplaceRegistry;

    public MarketplaceController(MarketplaceRegistry marketplaceRegistry) {
        this.marketplaceRegistry = marketplaceRegistry;
    }

    @GetMapping("/admin/marketplace")
    public String marketplace(Model model) {
        model.addAttribute("modules", marketplaceRegistry.modules());
        return "admin/marketplace/index";
    }
}