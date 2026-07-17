package com.webempresarial.store.jobs;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.service.InventoryPersistentAlertService;

@Component
public class InventoryAlertReconciliationJob {

    private final StoreRepository storeRepository;
    private final ProductoRepository productoRepository;
    private final InventoryPersistentAlertService alertService;

    public InventoryAlertReconciliationJob(
            StoreRepository storeRepository,
            ProductoRepository productoRepository,
            InventoryPersistentAlertService alertService
    ) {
        this.storeRepository = storeRepository;
        this.productoRepository = productoRepository;
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
                    productoRepository
                            .findProductosConVariantesYStock(store);

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