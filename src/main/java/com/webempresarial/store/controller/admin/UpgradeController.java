package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpgradeController {

    @GetMapping("/admin/upgrade")
    public String upgrade(
            @RequestParam(required = false) Feature feature,
            Model model
    ) {
        StorePlan requiredPlan = resolveRequiredPlan(feature);

        model.addAttribute("feature", feature);
        model.addAttribute("requiredPlan", requiredPlan);

        return "admin/upgrade/index";
    }

    private StorePlan resolveRequiredPlan(Feature feature) {

        if (feature == null) {
            return StorePlan.PRO;
        }

        return switch (feature) {

            case CRM,
                 LEADS,
                 TASKS,
                 COUPONS,
                 PIPELINE,
                 PROPOSALS,
                 CUSTOM_DOMAIN,
                 STRIPE_CONNECT,
                 ANALYTICS -> StorePlan.PRO;

            case EMAIL_MARKETING,
                 WHATSAPP_AUTOMATION,
                 AUTOMATIONS,
                 MULTI_USER,
                 API_ACCESS,
                 WHITE_LABEL_FULL -> StorePlan.PREMIUM;

            default -> StorePlan.PRO;
        };
    }
}