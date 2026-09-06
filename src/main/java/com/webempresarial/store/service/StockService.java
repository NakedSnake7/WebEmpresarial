package com.webempresarial.store.service;

import com.webempresarial.store.contracts.StockItem;   
import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.exceptions.ResourceNotFoundException;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.commerce.application.inventory.InventoryMovementService;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderItem;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.webempresarial.store.commerce.application.inventory.InventoryStockGateway;

import java.util.List;

@Service
public class StockService {

	private final InventoryStockGateway inventoryStockGateway;
    private final InventoryMovementService inventoryMovementService;
    private final InventoryPersistentAlertService
    persistentAlertService;
    

    public StockService(
            InventoryStockGateway inventoryStockGateway,
            InventoryMovementService inventoryMovementService,
            InventoryPersistentAlertService persistentAlertService
    ) {
        this.inventoryStockGateway = inventoryStockGateway;
        this.inventoryMovementService = inventoryMovementService;
        this.persistentAlertService = persistentAlertService;
    }

    @Transactional
    public void validarStock(
            List<? extends StockItem> items,
            Store store
    ) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay items en la orden"
            );
        }

        for (StockItem item : items) {

            if (item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor a cero"
                );
            }

            if (item.getVarianteId() != null) {

            	ProductoVariante variante =
            	        inventoryStockGateway.getVariantForUpdate(
            	                item.getVarianteId(),
            	                store
            	        );

                if (variante.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para variante"
                    );
                }

            } else {

                if (item.getProductId() == null) {
                    throw new IllegalArgumentException(
                            "El ID del producto es obligatorio"
                    );
                }

                var producto =
                        inventoryStockGateway.getProductForUpdate(
                                item.getProductId(),
                                store
                        );

                if (producto.getStockSimple()
                        < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para producto: "
                                    + producto.getProductName()
                    );
                }
            }
        }
    }

    @Transactional
    public void descontarStock(
            Order order,
            Store store
    ) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException(
                    "La orden debe estar persistida antes de descontar stock"
            );
        }

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        if (order.isStockReduced()) {
            return;
        }

        if (order.getItems() == null
                || order.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "La orden no contiene productos"
            );
        }

        for (OrderItem item : order.getItems()) {

            if (item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad del producto debe ser mayor a cero"
                );
            }

            if (item.getVariante() != null) {

            	ProductoVariante variante =
            	        inventoryStockGateway.getVariantForUpdate(
            	                item.getVariante().getId(),
            	                store
            	        );

                int stockBefore = variante.getStock();
                int quantity = item.getQuantity();

                if (stockBefore < quantity) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para variante"
                    );
                }

                int stockAfter =
                        stockBefore - quantity;

                variante.setStock(stockAfter);
                inventoryStockGateway.saveVariant(variante);

                inventoryMovementService.record(
                        store,
                        variante.getProducto(),
                        variante,
                        order,
                        InventoryMovementType.SALE,
                        quantity,
                        stockBefore,
                        stockAfter,
                        "Salida de inventario por orden #"
                                + order.getId()
                );

                persistentAlertService.evaluateVariant(
                        variante,
                        store
                );
                
            } else {

                if (item.getProducto() == null
                        || item.getProducto().getId() == null) {
                    throw new ResourceNotFoundException(
                            "Producto no definido en el item"
                    );
                }

                var producto =
                        inventoryStockGateway.getProductForUpdate(
                                item.getProducto().getId(),
                                store
                        );

                int stockBefore =
                        producto.getStockSimple();

                int quantity =
                        item.getQuantity();

                if (stockBefore < quantity) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para producto: "
                                    + producto.getProductName()
                    );
                }

                int stockAfter =
                        stockBefore - quantity;

                producto.setStockSimple(stockAfter);
                inventoryStockGateway.saveProduct(producto);

                inventoryMovementService.record(
                        store,
                        producto,
                        null,
                        order,
                        InventoryMovementType.SALE,
                        quantity,
                        stockBefore,
                        stockAfter,
                        "Salida de inventario por orden #"
                                + order.getId()
                );

                persistentAlertService.evaluateSimpleProduct(
                        producto,
                        store
                );
            }
        }

        order.setStockReduced(true);
    }

    @Transactional
    public void restaurarStock(
            Order order,
            Store store
    ) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException(
                    "La orden debe estar persistida antes de restaurar stock"
            );
        }

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        if (!order.isStockReduced()) {
            return;
        }

        if (order.getItems() == null
                || order.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "La orden no contiene productos"
            );
        }

        for (OrderItem item : order.getItems()) {

            if (item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad del producto debe ser mayor a cero"
                );
            }

            if (item.getVariante() != null) {

            	ProductoVariante variante =
            	        inventoryStockGateway.getVariantForUpdate(
            	                item.getVariante().getId(),
            	                store
            	        );

                int stockBefore =
                        variante.getStock();

                int quantity =
                        item.getQuantity();

                int stockAfter =
                        stockBefore + quantity;

                variante.setStock(stockAfter);
                inventoryStockGateway.saveVariant(variante);

                inventoryMovementService.record(
                        store,
                        variante.getProducto(),
                        variante,
                        order,
                        InventoryMovementType.RESTORE,
                        quantity,
                        stockBefore,
                        stockAfter,
                        "Inventario restaurado por cancelación "
                                + "o expiración de la orden #"
                                + order.getId()
                );

                persistentAlertService.evaluateVariant(
                        variante,
                        store
                );

            } else {

                if (item.getProducto() == null
                        || item.getProducto().getId() == null) {
                    throw new ResourceNotFoundException(
                            "Producto no definido en el item"
                    );
                }

                var producto =
                        inventoryStockGateway.getProductForUpdate(
                                item.getProducto().getId(),
                                store
                        );

                int stockBefore =
                        producto.getStockSimple();

                int quantity =
                        item.getQuantity();

                int stockAfter =
                        stockBefore + quantity;

                producto.setStockSimple(stockAfter);
                inventoryStockGateway.saveProduct(producto);

                inventoryMovementService.record(
                        store,
                        producto,
                        null,
                        order,
                        InventoryMovementType.RESTORE,
                        quantity,
                        stockBefore,
                        stockAfter,
                        "Inventario restaurado por cancelación "
                                + "o expiración de la orden #"
                                + order.getId()
                );

                persistentAlertService.evaluateSimpleProduct(
                        producto,
                        store
                );
            }
        }

        order.setStockReduced(false);
    }
}