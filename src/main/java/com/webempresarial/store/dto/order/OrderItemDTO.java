package com.webempresarial.store.dto.order;

import com.webempresarial.store.contracts.StockItem;

public class OrderItemDTO implements StockItem {

    private Long productId;
    private Long varianteId;
    private Integer quantity;

    public OrderItemDTO() {
    }

    public OrderItemDTO(
            Long productId,
            Long varianteId,
            Integer quantity
    ) {
        this.productId = productId;
        this.varianteId = varianteId;
        this.quantity = quantity;
    }

    @Override
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public Long getVarianteId() {
        return varianteId;
    }

    public void setVarianteId(Long varianteId) {
        this.varianteId = varianteId;
    }

    @Override
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}