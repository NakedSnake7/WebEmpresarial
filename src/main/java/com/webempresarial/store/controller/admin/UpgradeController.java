package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.FeatureAccessService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpgradeController {

    private final FeatureAccessService featureAccessService;

    public UpgradeController(
            FeatureAccessService featureAccessService
    ) {
        this.featureAccessService = featureAccessService;
    }

    @GetMapping("/admin/upgrade")
    public String upgrade(
            @RequestParam(required = false) Feature feature,
            Model model
    ) {
        StorePlan requiredPlan =
                resolveRequiredPlan(feature);

        model.addAttribute("feature", feature);
        model.addAttribute(
                "requiredPlan",
                requiredPlan
        );

        return "admin/upgrade/index";
    }

    private StorePlan resolveRequiredPlan(
            Feature feature
    ) {

        if (feature == null) {
            return StorePlan.PRO;
        }

        return featureAccessService
                .definition(feature)
                .getAccessPolicy()
                .getMinimumPlan();
    }
}
