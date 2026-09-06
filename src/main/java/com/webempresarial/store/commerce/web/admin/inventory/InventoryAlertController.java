package com.webempresarial.store.commerce.web.admin.inventory;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.theme.StoreResolver;

@Controller
@RequestMapping("/admin/inventory/alerts")
public class InventoryAlertController {

    private final InventoryPersistentAlertService alertService;
    private final StoreResolver storeResolver;

    public InventoryAlertController(
            InventoryPersistentAlertService alertService,
            StoreResolver storeResolver
    ) {
        this.alertService = alertService;
        this.storeResolver = storeResolver;
    }

    @GetMapping
    public String index(
            HttpServletRequest request,
            Model model
    ) {
        Store store =
                storeResolver.getCurrentStore(request);

        model.addAttribute(
                "activeAlerts",
                alertService.findActive(store)
        );

        model.addAttribute(
                "alertHistory",
                alertService.findHistory(store)
        );

        model.addAttribute(
                "activeAlertCount",
                alertService.countActive(store)
        );

        return "admin/inventory/alerts";
    }

    @PostMapping("/{id}/acknowledge")
    public String acknowledge(
            @PathVariable Long id,
            HttpServletRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        Store store =
                storeResolver.getCurrentStore(request);

        String username =
                principal != null
                        ? principal.getName()
                        : "SYSTEM";

        alertService.acknowledge(
                id,
                store,
                username
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Alerta reconocida correctamente."
        );

        return "redirect:/admin/inventory/alerts";
    }
}