package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeBillingPortalService;
import com.webempresarial.store.service.StripeBillingService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BillingController {

    private final StoreContextService storeContextService;
    private final StripeBillingService stripeBillingService;
    private final StripeBillingPortalService stripeBillingPortalService;

    public BillingController(
            StoreContextService storeContextService,
            StripeBillingService stripeBillingService,
            StripeBillingPortalService stripeBillingPortalService
    ) {
        this.storeContextService = storeContextService;
        this.stripeBillingService = stripeBillingService;
        this.stripeBillingPortalService = stripeBillingPortalService;
    }

    @GetMapping("/admin/billing")
    public String billing(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        model.addAttribute("store", store);
        model.addAttribute("subscription", store.getSubscription());

        return "admin/billing/index";
    }
    
    @PostMapping("/admin/billing/checkout")
    public String checkout(
            @RequestParam StorePlan plan,
            HttpServletRequest request
    ) throws Exception {

        Store store = storeContextService.getCurrentStore(request);

        String checkoutUrl =
                stripeBillingService.createCheckoutSession(
                        store,
                        plan
                );

        return "redirect:" + checkoutUrl;
    }
    
    @PostMapping("/admin/billing/portal")
    public String portal(HttpServletRequest request) throws Exception {

        Store store = storeContextService.getCurrentStore(request);

        String portalUrl =
                stripeBillingPortalService.createPortalSession(store);

        return "redirect:" + portalUrl;
    }
    
}