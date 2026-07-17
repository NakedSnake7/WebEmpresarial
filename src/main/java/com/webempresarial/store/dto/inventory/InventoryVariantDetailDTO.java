package com.webempresarial.store.dto.inventory;

import java.math.BigDecimal;

public record InventoryVariantDetailDTO(
        Long variantId,
        String label,
        Integer stock,
        BigDecimal price
) {
}