package com.webempresarial.store.commerce.application.inventory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.webempresarial.store.commerce.domain.inventory.InventoryMovement;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;

@Service
public class InventoryMovementService {

    private final InventoryMovementRepository repository;

    public InventoryMovementService(
            InventoryMovementRepository repository
    ) {
        this.repository = repository;
    }

    
    
    public InventoryMovement record(
            Store store,
            Producto producto,
            ProductoVariante variante,
            Order order,
            InventoryMovementType type,
            int quantity,
            int stockBefore,
            int stockAfter,
            String reason
    ) {
        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        if (producto == null || producto.getId() == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad del movimiento debe ser mayor a cero"
            );
        }

        InventoryMovement movement =
                new InventoryMovement();

        movement.setStore(store);
        movement.setProducto(producto);
        movement.setVariante(variante);
        movement.setOrder(order);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setStockBefore(stockBefore);
        movement.setStockAfter(stockAfter);
        movement.setReason(reason);

        return repository.save(movement);
    }
    
    public List<InventoryMovement> findFiltered(
            Store store,
            InventoryMovementType type,
            Long productId,
            Long variantId,
            Long orderId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return repository.findFiltered(
                store,
                type,
                productId,
                variantId,
                orderId,
                from,
                to
        );
    }
    public boolean hasMovementsForOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException(
                    "El orderId es obligatorio"
            );
        }

        return repository.existsByOrderId(orderId);
    }
}