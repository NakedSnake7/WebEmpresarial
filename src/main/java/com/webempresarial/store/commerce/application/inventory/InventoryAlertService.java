package com.webempresarial.store.commerce.application.inventory;

import java.util.ArrayList; 
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.inventory.InventoryProductStockDTO;
import com.webempresarial.store.dto.inventory.StockSeverity;
import com.webempresarial.store.model.Store;


@Service
public class InventoryAlertService {

	private final InventoryStockQueryGateway inventoryStockQueryGateway;

    private final int lowStockThreshold;
    private final int criticalStockThreshold;

    public InventoryAlertService(
            InventoryStockQueryGateway inventoryStockQueryGateway,

            @Value("${inventory.low-stock-threshold:5}")
            int lowStockThreshold,

            @Value("${inventory.critical-stock-threshold:2}")
            int criticalStockThreshold
    ) {
        this.inventoryStockQueryGateway =
                inventoryStockQueryGateway;

        this.lowStockThreshold =
                lowStockThreshold;

        this.criticalStockThreshold =
                criticalStockThreshold;
    }

    @Transactional(readOnly = true)
    public List<InventoryProductStockDTO> getLowStockItems(
            Store store
    ) {
        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        List<InventoryProductStockDTO> result =
                new ArrayList<>();

        inventoryStockQueryGateway
        .findLowStockSimpleProducts(
                store,
                lowStockThreshold
        )
                .forEach(product ->
                        result.add(
                                new InventoryProductStockDTO(
                                        product.productId(),
                                        product.productName(),
                                        null,
                                        null,
                                        product.currentStock(),
                                        lowStockThreshold,
                                        product.unitPrice(),
                                        resolveSeverity(
                                                product.currentStock()
                                        )
                                )
                        )
                );

        inventoryStockQueryGateway
        .findLowStockVariants(
                store,
                lowStockThreshold
        )
                .forEach(variant ->
                        result.add(
                                new InventoryProductStockDTO(
                                        variant.productId(),
                                        variant.productName(),
                                        variant.variantId(),
                                        "Variante #"
                                                + variant.variantId(),
                                        variant.currentStock(),
                                        lowStockThreshold,
                                        variant.unitPrice(),
                                        resolveSeverity(
                                                variant.currentStock()
                                        )
                                )
                        )
                );

        return result.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        InventoryProductStockDTO::currentStock
                                )
                                .thenComparing(
                                        InventoryProductStockDTO::productName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public long countOutOfStockItems(
            Store store
    ) {
        return getLowStockItems(store)
                .stream()
                .filter(item ->
                        item.severity()
                                == StockSeverity.OUT_OF_STOCK
                )
                .count();
    }

    @Transactional(readOnly = true)
    public long countCriticalStockItems(
            Store store
    ) {
        return getLowStockItems(store)
                .stream()
                .filter(item ->
                        item.severity()
                                == StockSeverity.CRITICAL
                )
                .count();
    }

    private StockSeverity resolveSeverity(
            Integer stock
    ) {
        int safeStock = stock != null ? stock : 0;

        if (safeStock <= 0) {
            return StockSeverity.OUT_OF_STOCK;
        }

        if (safeStock <= criticalStockThreshold) {
            return StockSeverity.CRITICAL;
        }

        return StockSeverity.LOW;
    }
}