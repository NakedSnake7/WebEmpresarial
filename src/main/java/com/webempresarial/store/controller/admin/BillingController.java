package com.webempresarial.store.controller.admin;

import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.model.PlanChangeResult;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeBillingPortalService;
import com.webempresarial.store.service.StripeBillingService;
import com.webempresarial.store.service.StripeSubscriptionChangeService;

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
    private final PlatformAccessService platformAccessService;
    private final StripeSubscriptionChangeService stripeSubscriptionChangeService;

    public BillingController(
            StoreContextService storeContextService,
            StripeBillingService stripeBillingService,
            StripeBillingPortalService stripeBillingPortalService,
            PlatformAccessService platformAccessService,
            StripeSubscriptionChangeService stripeSubscriptionChangeService
    ) {
        this.storeContextService = storeContextService;
        this.stripeBillingService = stripeBillingService;
        this.stripeBillingPortalService = stripeBillingPortalService;
        this.platformAccessService = platformAccessService;
        this.stripeSubscriptionChangeService =
                stripeSubscriptionChangeService;
    }

    @GetMapping("/admin/billing")
    public String billing(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);

        StorePlan currentPlan =
                platformAccessService.resolveEffectivePlan(store);

        model.addAttribute("store", store);
        model.addAttribute("subscription", store.getSubscription());
        model.addAttribute("currentPlan", currentPlan);

        return "admin/billing/index";
    }
    
    @PostMapping("/admin/billing/checkout")
    public String changePlan(
            @RequestParam StorePlan plan,
            HttpServletRequest request
    ) throws Exception {

        Store store =
                storeContextService.getCurrentStore(request);

        var localSubscription =
                platformAccessService.getEffectiveSubscription(store);

        boolean reusable =
                stripeSubscriptionChangeService.isReusable(
                        localSubscription
                );

        if (!reusable) {

            String checkoutUrl =
                    stripeBillingService.createCheckoutSession(
                            store,
                            plan
                    );

            return "redirect:" + checkoutUrl;
        }

        PlanChangeResult result =
                stripeSubscriptionChangeService.changePlan(
                        store,
                        plan
                );

        return switch (result) {

            case NO_CHANGE ->
                    "redirect:/admin/billing";

            case UPGRADED ->
                    "redirect:/admin/billing?changeRequested=true";

            case DOWNGRADE_SCHEDULED ->
                    "redirect:/admin/billing?downgradeScheduled=true";
        };
    }
    
    @PostMapping("/admin/billing/portal")
    public String portal(HttpServletRequest request) throws Exception {

        Store store = storeContextService.getCurrentStore(request);

        String portalUrl =
                stripeBillingPortalService.createPortalSession(store);

        return "redirect:" + portalUrl;
    }
    @GetMapping("/admin/billing/success")
    public String billingSuccess(
            @RequestParam(name = "session_id", required = false) String sessionId,
            HttpServletRequest request,
            Model model
    ) {
        Store store = storeContextService.getCurrentStore(request);

        StorePlan currentPlan =
                platformAccessService.resolveEffectivePlan(store);

        model.addAttribute("store", store);
        model.addAttribute("subscription", store.getSubscription());
        model.addAttribute("currentPlan", currentPlan);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("paymentSuccess", true);

        return "redirect:/admin/billing?success=true";
    }
    
}