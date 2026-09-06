package com.webempresarial.store.commerce.infrastructure.order.scheduling;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.feature.runtime.TraceType;
import com.webempresarial.store.feature.runtime.annotations.Trace;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.commerce.application.order.OrderService;

@Component
public class OrderExpirationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderExpirationScheduler.class
            );

    private final OrderService orderService;
    private final StoreRepository storeRepository;

    public OrderExpirationScheduler(
            OrderService orderService,
            StoreRepository storeRepository
    ) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }

    @Scheduled(
            cron = "${orders.expiration.cron:0 */30 * * * *}"
    )
    @Trace(
            type = TraceType.SCHEDULER,
            name = "OrderExpirationScheduler.verificarOrdenesPendientes",
            source = "Ecommerce Scheduler"
    )
    public void verificarOrdenesPendientes() {

        List<Store> stores =
                storeRepository.findAll();

        int checked = 0;
        int expired = 0;
        int failed = 0;

        log.info(
                "[Order Expiration] Revisando {} tiendas",
                stores.size()
        );

        for (Store store : stores) {

            if (!store.isActiva()) {
                continue;
            }

            List<Order> orders =
                    orderService.findPendingOrders(store);

            for (Order order : orders) {
                checked++;

                try {
                    boolean wasExpired =
                            orderService
                                    .expirarOrdenTransferencia(
                                            order.getId(),
                                            store
                                    );

                    if (wasExpired) {
                        expired++;
                    }

                } catch (Exception ex) {
                    failed++;

                    log.error(
                            "[Order Expiration] Error en orden {} de tienda {}",
                            order.getId(),
                            store.getId(),
                            ex
                    );
                }
            }
        }

        log.info(
                "[Order Expiration] Finalizado. Revisadas: {}, expiradas: {}, fallidas: {}",
                checked,
                expired,
                failed
        );
    }
}