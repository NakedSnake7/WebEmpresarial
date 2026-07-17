package com.webempresarial.store.dto.inventory;

import java.time.LocalDateTime;

import com.webempresarial.store.model.InventoryMovementType;

public record InventoryMovementRowDTO(
        Long id,
        LocalDateTime createdAt,
        InventoryMovementType type,
        Long productId,
        String productName,
        Long variantId,
        String variantLabel,
        Long orderId,
        Integer quantity,
        Integer stockBefore,
        Integer stockAfter,
        String reason
) {
}