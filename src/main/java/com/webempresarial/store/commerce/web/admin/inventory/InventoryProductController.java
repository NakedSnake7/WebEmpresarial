package com.webempresarial.store.commerce.web.admin.inventory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webempresarial.store.dto.inventory.InventoryAdjustmentRequestDTO;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryAdjustmentService;
import com.webempresarial.store.commerce.application.inventory.InventoryProductDetailService;
import com.webempresarial.store.theme.StoreResolver;

@Controller
@RequestMapping("/admin/productos")
public class InventoryProductController {

    private final InventoryProductDetailService detailService;
    private final InventoryAdjustmentService adjustmentService;
    private final StoreResolver storeResolver;

    public InventoryProductController(
            InventoryProductDetailService detailService,
            InventoryAdjustmentService adjustmentService,
            StoreResolver storeResolver
    ) {
        this.detailService = detailService;
        this.adjustmentService = adjustmentService;
        this.storeResolver = storeResolver;
    }

    @GetMapping("/{id}/inventory")
    public String detail(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        model.addAttribute(
                "inventory",
                detailService.getDetail(id, store)
        );

        model.addAttribute(
                "adjustment",
                new InventoryAdjustmentRequestDTO()
        );

        model.addAttribute(
                "adjustmentTypes",
                new InventoryMovementType[]{
                        InventoryMovementType.ADJUSTMENT_IN,
                        InventoryMovementType.ADJUSTMENT_OUT
                }
        );

        return "admin/inventory/product-detail";
    }

    @PostMapping("/{id}/inventory/adjust")
    public String adjust(
            @PathVariable Long id,
            @Valid
            @ModelAttribute("adjustment")
            InventoryAdjustmentRequestDTO adjustment,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        try {
            adjustmentService.adjust(
                    id,
                    adjustment,
                    store
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Inventario actualizado correctamente."
            );

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible actualizar el inventario."
            );
        }

        return "redirect:/admin/productos/"
                + id
                + "/inventory";
    }
}