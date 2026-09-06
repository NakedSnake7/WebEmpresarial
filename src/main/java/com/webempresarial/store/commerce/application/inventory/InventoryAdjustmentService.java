package com.webempresarial.store.commerce.application.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.dto.inventory.InventoryAdjustmentRequestDTO;
import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

@Service
public class InventoryAdjustmentService {

    private final InventoryStockGateway inventoryStockGateway;
    private final InventoryMovementService movementService;
    private final InventoryPersistentAlertService persistentAlertService;

    public InventoryAdjustmentService(
            InventoryStockGateway inventoryStockGateway,
            InventoryMovementService movementService,
            InventoryPersistentAlertService persistentAlertService
    ) {
        this.inventoryStockGateway = inventoryStockGateway;
        this.movementService = movementService;
        this.persistentAlertService = persistentAlertService;
    }

    @Transactional
    public void adjust(
            Long productId,
            InventoryAdjustmentRequestDTO request,
            Store store
    ) {

        validateRequest(request);

        if (request.getVariantId() != null) {
            adjustVariant(
                    productId,
                    request,
                    store
            );
            return;
        }

        adjustSimpleProduct(
                productId,
                request,
                store
        );
    }

    private void adjustVariant(
            Long productId,
            InventoryAdjustmentRequestDTO request,
            Store store
    ) {

        ProductoVariante variante =
                inventoryStockGateway
                        .getVariantForUpdate(
                                request.getVariantId(),
                                store
                        );

        Producto producto =
                variante.getProducto();

        if (producto == null
                || producto.getId() == null
                || !producto.getId()
                        .equals(productId)) {

            throw new IllegalArgumentException(
                    "La variante no pertenece al producto indicado"
            );
        }

        int before =
                variante.getStock();

        int after =
                calculateAfter(
                        before,
                        request.getQuantity(),
                        request.getType()
                );

        variante.setStock(after);

        inventoryStockGateway
                .saveVariant(variante);

        movementService.record(
                store,
                producto,
                variante,
                null,
                request.getType(),
                request.getQuantity(),
                before,
                after,
                request.getReason().trim()
        );

        persistentAlertService
                .evaluateVariant(
                        variante,
                        store
                );
    }

    private void adjustSimpleProduct(
            Long productId,
            InventoryAdjustmentRequestDTO request,
            Store store
    ) {

        Producto producto =
                inventoryStockGateway
                        .getProductForUpdate(
                                productId,
                                store
                        );

        if (producto.tieneVariantes()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una variante"
            );
        }

        int before =
                producto.getStockSimple();

        int after =
                calculateAfter(
                        before,
                        request.getQuantity(),
                        request.getType()
                );

        producto.setStockSimple(after);

        inventoryStockGateway
                .saveProduct(producto);

        movementService.record(
                store,
                producto,
                null,
                null,
                request.getType(),
                request.getQuantity(),
                before,
                after,
                request.getReason().trim()
        );

        persistentAlertService
                .evaluateSimpleProduct(
                        producto,
                        store
                );
    }

    private int calculateAfter(
            int currentStock,
            int quantity,
            InventoryMovementType type
    ) {

        if (type == InventoryMovementType.ADJUSTMENT_IN) {
            return currentStock + quantity;
        }

        if (type == InventoryMovementType.ADJUSTMENT_OUT) {

            if (currentStock < quantity) {
                throw new InsufficientStockException(
                        "No hay stock suficiente para realizar la salida"
                );
            }

            return currentStock - quantity;
        }

        throw new IllegalArgumentException(
                "Tipo de ajuste manual no válido"
        );
    }

    private void validateRequest(
            InventoryAdjustmentRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "El ajuste es obligatorio"
            );
        }

        if (request.getType() == null) {
            throw new IllegalArgumentException(
                    "El tipo de ajuste es obligatorio"
            );
        }

        if (request.getType()
                    != InventoryMovementType.ADJUSTMENT_IN
                && request.getType()
                    != InventoryMovementType.ADJUSTMENT_OUT) {

            throw new IllegalArgumentException(
                    "Solo se permiten ajustes de entrada o salida"
            );
        }

        if (request.getQuantity() == null
                || request.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }

        if (request.getReason() == null
                || request.getReason().isBlank()) {

            throw new IllegalArgumentException(
                    "El motivo es obligatorio"
            );
        }
    }
}