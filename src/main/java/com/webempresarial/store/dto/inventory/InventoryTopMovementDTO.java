package com.webempresarial.store.dto.inventory;

public record InventoryTopMovementDTO(
        Long productId,
        String productName,
        Long unitsOut
) {
}