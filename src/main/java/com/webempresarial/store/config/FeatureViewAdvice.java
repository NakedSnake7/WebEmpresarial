package com.webempresarial.store.config;

import com.webempresarial.store.service.PlanFeatureService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class FeatureViewAdvice {

    private final PlanFeatureService planFeatureService;

    public FeatureViewAdvice(PlanFeatureService planFeatureService) {
        this.planFeatureService = planFeatureService;
    }

    @ModelAttribute("features")
    public PlanFeatureService features() {
        return planFeatureService;
    }
}