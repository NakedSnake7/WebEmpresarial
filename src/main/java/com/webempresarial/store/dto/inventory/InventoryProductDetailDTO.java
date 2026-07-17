package com.webempresarial.store.dto.inventory;

import java.math.BigDecimal;
import java.util.List;

public record InventoryProductDetailDTO(
        Long productId,
        String productName,
        String sku,
        boolean hasVariants,
        Integer simpleStock,
        BigDecimal basePrice,
        List<InventoryVariantDetailDTO> variants,
        List<InventoryMovementRowDTO> movements
) {
}