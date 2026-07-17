package com.webempresarial.store.dto.inventory;

import java.math.BigDecimal;

public record VariantStockProjection(
        Long productId,
        String productName,
        Long variantId,
        Integer currentStock,
        BigDecimal unitPrice
) {
}