package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpgradeController {

    private final FeatureAccessService featureAccessService;
    private final StoreContextService storeContextService;
    private final PlatformAccessService platformAccessService;

    public UpgradeController(
            FeatureAccessService featureAccessService,
            StoreContextService storeContextService,
            PlatformAccessService platformAccessService
    ) {
        this.featureAccessService = featureAccessService;
        this.storeContextService = storeContextService;
        this.platformAccessService = platformAccessService;
    }

    @GetMapping("/admin/upgrade")
    public String upgrade(
            @RequestParam(required = false) Feature feature,
            Model model,
            HttpServletRequest request
    ) {
        StorePlan requiredPlan =
                resolveRequiredPlan(feature);

        Store store =
                storeContextService.getCurrentStore(request);

        StorePlan currentPlan =
                platformAccessService.resolveEffectivePlan(store);

        boolean upgradeRequired =
                requiredPlan.isHigherThan(currentPlan);

        model.addAttribute("feature", feature);
        model.addAttribute(
                "requiredPlan",
                requiredPlan
        );
        model.addAttribute(
                "currentPlan",
                currentPlan
        );
        model.addAttribute(
                "upgradeRequired",
                upgradeRequired
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
