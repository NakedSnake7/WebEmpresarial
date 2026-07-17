package com.webempresarial.store.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.dto.inventory.InventoryMovementRowDTO;
import com.webempresarial.store.entity.InventoryMovement;
import com.webempresarial.store.model.InventoryMovementType;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.InventoryMovementService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;
    private final ProductoService productoService;
    private final StoreResolver storeResolver;

    public InventoryMovementController(
            InventoryMovementService inventoryMovementService,
            ProductoService productoService,
            StoreResolver storeResolver
    ) {
        this.inventoryMovementService =
                inventoryMovementService;
        this.productoService = productoService;
        this.storeResolver = storeResolver;
    }

    @GetMapping("/movements")
    public String list(
            @RequestParam(required = false)
            InventoryMovementType type,

            @RequestParam(required = false)
            Long productId,

            @RequestParam(required = false)
            Long variantId,

            @RequestParam(required = false)
            Long orderId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            HttpServletRequest request,
            Model model
    ) {
        Store store =
                storeResolver.getCurrentStore(request);

        LocalDateTime fromDate =
                from != null
                        ? from.atStartOfDay()
                        : null;

        LocalDateTime toDate =
                to != null
                        ? to.atTime(23, 59, 59)
                        : null;

        List<InventoryMovementRowDTO> movements =
                inventoryMovementService
                        .findFiltered(
                                store,
                                type,
                                productId,
                                variantId,
                                orderId,
                                fromDate,
                                toDate
                        )
                        .stream()
                        .map(this::toRow)
                        .toList();

        model.addAttribute("movements", movements);
        model.addAttribute(
                "movementTypes",
                InventoryMovementType.values()
        );

        model.addAttribute(
                "products",
                productoService
                        .obtenerProductosAdminOptimizado(store)
        );

        return "admin/inventory/movements";
    }

    private InventoryMovementRowDTO toRow(
            InventoryMovement movement
    ) {
        String variantLabel = null;

        if (movement.getVariante() != null) {
            variantLabel =
                    "Variante #"
                    + movement.getVariante().getId();
        }

        return new InventoryMovementRowDTO(
                movement.getId(),
                movement.getCreatedAt(),
                movement.getType(),
                movement.getProducto().getId(),
                movement.getProducto().getProductName(),
                movement.getVariante() != null
                        ? movement.getVariante().getId()
                        : null,
                variantLabel,
                movement.getOrder() != null
                        ? movement.getOrder().getId()
                        : null,
                movement.getQuantity(),
                movement.getStockBefore(),
                movement.getStockAfter(),
                movement.getReason()
        );
    }
}