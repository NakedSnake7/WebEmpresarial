package com.webempresarial.store.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.InventoryAlert;
import com.webempresarial.store.model.InventoryAlertLevel;
import com.webempresarial.store.model.InventoryAlertStatus;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.InventoryAlertRepository;

@Service
public class InventoryPersistentAlertService {

    private static final List<InventoryAlertStatus>
            ACTIVE_STATUSES =
            List.of(
                    InventoryAlertStatus.OPEN,
                    InventoryAlertStatus.ACKNOWLEDGED
            );

    private final InventoryAlertRepository repository;
    private final int lowStockThreshold;
    private final int criticalStockThreshold;

    public InventoryPersistentAlertService(
            InventoryAlertRepository repository,

            @Value("${inventory.low-stock-threshold:5}")
            int lowStockThreshold,

            @Value("${inventory.critical-stock-threshold:2}")
            int criticalStockThreshold
    ) {
        if (criticalStockThreshold > lowStockThreshold) {
            throw new IllegalArgumentException(
                    "El límite crítico no puede ser mayor "
                            + "que el límite de stock bajo"
            );
        }

        this.repository = repository;
        this.lowStockThreshold = lowStockThreshold;
        this.criticalStockThreshold =
                criticalStockThreshold;
    }

    @Transactional
    public void evaluateSimpleProduct(
            Producto producto,
            Store store
    ) {
        validateProduct(producto, store);

        int stock =
                producto.getStockSimple() != null
                        ? producto.getStockSimple()
                        : 0;

        evaluate(
                producto,
                null,
                store,
                stock
        );
    }

    @Transactional
    public void evaluateVariant(
            ProductoVariante variante,
            Store store
    ) {
        if (variante == null
                || variante.getId() == null
                || variante.getProducto() == null) {
            throw new IllegalArgumentException(
                    "La variante es obligatoria"
            );
        }

        Producto producto = variante.getProducto();

        validateProduct(producto, store);

        int stock =
                variante.getStock() != null
                        ? variante.getStock()
                        : 0;

        evaluate(
                producto,
                variante,
                store,
                stock
        );
    }

    @Transactional(readOnly = true)
    public long countActive(Store store) {
        return repository.countByStoreAndStatusIn(
                store,
                ACTIVE_STATUSES
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryAlert> findActive(
            Store store
    ) {
        return repository.findActiveWithDetails(
                store,
                ACTIVE_STATUSES
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryAlert> findHistory(
            Store store
    ) {
        return repository.findHistoryWithDetails(store);
    }

    @Transactional
    public void acknowledge(
            Long alertId,
            Store store,
            String username
    ) {
        InventoryAlert alert = findForStore(
                alertId,
                store
        );

        alert.acknowledge(
                username != null && !username.isBlank()
                        ? username
                        : "SYSTEM"
        );
    }

    private void evaluate(
            Producto producto,
            ProductoVariante variante,
            Store store,
            int currentStock
    ) {
        String activeKey = buildActiveKey(
                store,
                producto,
                variante
        );

        InventoryAlertLevel level =
                resolveLevel(currentStock);

        InventoryAlert activeAlert =
                repository.findByActiveKey(activeKey)
                        .orElse(null);

        /*
         * El stock ya está saludable.
         * Si existía una alerta activa, se resuelve.
         */
        if (level == null) {
            if (activeAlert != null
                    && activeAlert.isActive()) {
                activeAlert.resolve(
                        "Resuelta automáticamente al recuperarse "
                                + "el inventario. Stock actual: "
                                + currentStock
                );
            }

            return;
        }

        /*
         * Actualiza la alerta existente sin crear duplicados.
         */
        if (activeAlert != null) {
            activeAlert.refresh(
                    level,
                    currentStock,
                    lowStockThreshold
            );
            return;
        }

        InventoryAlert alert =
                new InventoryAlert();

        alert.setStore(store);
        alert.setProducto(producto);
        alert.setVariante(variante);
        alert.setLevel(level);
        alert.setStatus(InventoryAlertStatus.OPEN);
        alert.setCurrentStock(currentStock);
        alert.setStockThreshold(lowStockThreshold);
        alert.setActiveKey(activeKey);

        repository.save(alert);
    }

    private InventoryAlertLevel resolveLevel(
            int stock
    ) {
        if (stock <= 0) {
            return InventoryAlertLevel.OUT_OF_STOCK;
        }

        if (stock <= criticalStockThreshold) {
            return InventoryAlertLevel.CRITICAL;
        }

        if (stock <= lowStockThreshold) {
            return InventoryAlertLevel.LOW;
        }

        return null;
    }

    private String buildActiveKey(
            Store store,
            Producto producto,
            ProductoVariante variante
    ) {
        return "INVENTORY:"
                + store.getId()
                + ":PRODUCT:"
                + producto.getId()
                + ":VARIANT:"
                + (
                    variante != null
                            ? variante.getId()
                            : "SIMPLE"
                );
    }

    private InventoryAlert findForStore(
            Long alertId,
            Store store
    ) {
        return repository
                .findByIdAndStore(alertId, store)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Alerta de inventario no encontrada"
                        )
                );
    }

    private void validateProduct(
            Producto producto,
            Store store
    ) {
        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        if (producto == null
                || producto.getId() == null
                || producto.getStore() == null
                || !store.getId().equals(
                        producto.getStore().getId()
                )) {
            throw new IllegalArgumentException(
                    "El producto no pertenece a la tienda"
            );
        }
    }
}