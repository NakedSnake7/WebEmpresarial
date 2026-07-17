package com.webempresarial.store.dto.inventory;

import java.math.BigDecimal;

public record SimpleProductStockProjection(
        Long productId,
        String productName,
        Integer currentStock,
        BigDecimal unitPrice
) {
}