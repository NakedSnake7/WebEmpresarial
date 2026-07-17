package com.webempresarial.store.dto.inventory;

import java.math.BigDecimal;

public record InventoryProductStockDTO(
        Long productId,
        String productName,
        Long variantId,
        String variantLabel,
        Integer currentStock,
        Integer threshold,
        BigDecimal unitPrice,
        StockSeverity severity
) {

    public boolean isVariant() {
        return variantId != null;
    }
}