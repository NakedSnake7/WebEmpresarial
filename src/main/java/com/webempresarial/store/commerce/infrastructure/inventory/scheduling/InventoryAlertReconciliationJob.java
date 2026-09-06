package com.webempresarial.store.commerce.infrastructure.inventory.scheduling;

import java.util.List;  

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.application.inventory.InventoryStockQueryGateway;

@Component
public class InventoryAlertReconciliationJob {

    private final StoreRepository storeRepository;
    private final InventoryStockQueryGateway inventoryStockQueryGateway;
    private final InventoryPersistentAlertService alertService;

    public InventoryAlertReconciliationJob(
            StoreRepository storeRepository,
            InventoryStockQueryGateway inventoryStockQueryGateway,
            InventoryPersistentAlertService alertService
    ) {
        this.storeRepository = storeRepository;
        this.inventoryStockQueryGateway = inventoryStockQueryGateway;
        this.alertService = alertService;
    }

    @Scheduled(
            fixedDelayString =
                    "${inventory.alerts.reconciliation-delay-ms:900000}"
    )
    public void reconcile() {

        List<Store> stores =
                storeRepository.findAll()
                        .stream()
                        .filter(Store::isActiva)
                        .toList();

        for (Store store : stores) {

        	List<Producto> products =
        	        inventoryStockQueryGateway
        	                .findProductsWithVariantsAndStock(store);

            for (Producto product : products) {

                if (product.tieneVariantes()) {
                    product.getVariantes()
                            .forEach(variant ->
                                    alertService.evaluateVariant(
                                            variant,
                                            store
                                    )
                            );
                } else {
                    alertService.evaluateSimpleProduct(
                            product,
                            store
                    );
                }
            }
        }
    }
}