package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.CustomDomainService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/domains")
public class CustomDomainController {

    private final StoreContextService storeContextService;
    private final FeatureAccessService featureAccessService;
    private final CustomDomainService customDomainService;
    private final CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    public CustomDomainController(
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService,
            CustomDomainService customDomainService,
            CurrentAdminAuthorizationService
                    currentAdminAuthorizationService
    ) {
        this.storeContextService =
                storeContextService;

        this.featureAccessService =
                featureAccessService;

        this.customDomainService =
                customDomainService;

        this.currentAdminAuthorizationService =
                currentAdminAuthorizationService;
    }

    @GetMapping
    public String domains(
            HttpServletRequest request,
            Model model
    ) {

        Store store =
                currentStoreWithCustomDomain(request);

        model.addAttribute(
                "store",
                store
        );

        model.addAttribute(
                "canManageDomain",
                currentAdminAuthorizationService
                        .hasPermission(
                                AdminPermission.DOMAIN_MANAGE
                        )
        );

        return "admin/domains/index";
    }

    @PostMapping
    public String requestDomain(
            @RequestParam("domain") String domain,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {

        Store store =
                currentStoreWithCustomDomain(request);

        customDomainService.requestDomain(
                store,
                domain
        );

        redirectAttributes.addFlashAttribute(
                "domainSuccess",
                "Dominio guardado. La verificación DNS está pendiente."
        );

        return "redirect:/admin/domains";
    }

    @PostMapping("/disconnect")
    public String disconnectDomain(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {

        Store store =
                currentStoreWithCustomDomain(request);

        customDomainService.disconnectDomain(
                store
        );

        redirectAttributes.addFlashAttribute(
                "domainSuccess",
                "Dominio personalizado desconectado."
        );

        return "redirect:/admin/domains";
    }

    @PostMapping("/verify")
    public String verifyDomain(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {

        Store store =
                currentStoreWithCustomDomain(request);

        customDomainService.markVerified(
                store
        );

        redirectAttributes.addFlashAttribute(
                "domainSuccess",
                "Dominio personalizado marcado como verificado."
        );

        return "redirect:/admin/domains";
    }

    private Store currentStoreWithCustomDomain(
            HttpServletRequest request
    ) {

        Store store =
                storeContextService.getCurrentStore(
                        request
                );

        featureAccessService.requireFeature(
                store,
                Feature.CUSTOM_DOMAIN
        );

        return store;
    }
}
