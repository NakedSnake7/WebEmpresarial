package com.webempresarial.store.dto.inventory;

import com.webempresarial.store.model.InventoryMovementType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InventoryAdjustmentRequestDTO {

    private Long variantId;

    @NotNull
    private InventoryMovementType type;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotBlank
    private String reason;

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public InventoryMovementType getType() {
        return type;
    }

    public void setType(InventoryMovementType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}