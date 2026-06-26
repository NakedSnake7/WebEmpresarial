package com.webempresarial.store.controller.advice;

import com.webempresarial.store.exceptions.FeatureLockedException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FeatureAccessExceptionHandler {

    @ExceptionHandler(FeatureLockedException.class)
    public String handleFeatureLocked(
            FeatureLockedException ex,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(
                "lockedFeature",
                ex.getFeature().name()
        );

        return "redirect:/admin/upgrade?feature="
                + ex.getFeature().name();
    }
}