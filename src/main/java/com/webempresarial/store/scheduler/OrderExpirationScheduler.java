package com.webempresarial.store.scheduler;

import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderExpirationScheduler {

    private final OrderService orderService;
    private final StoreRepository storeRepository;

    public OrderExpirationScheduler(
            OrderService orderService,
            StoreRepository storeRepository
    ) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }

    // ⏰ Cada 30 minutos
    @Scheduled(cron = "0 */30 * * * *")
    public void verificarOrdenesPendientes() {

        System.out.println(
                "🔎 OrderExpirationScheduler: buscando órdenes pendientes..."
        );

        List<Store> stores = storeRepository.findAll();

        for (Store store : stores) {

            List<Order> ordenes =
                    orderService.findPendingOrders(store);

            for (Order order : ordenes) {

                try {

                    orderService.expirarOrdenTransferencia(
                            order,
                            store
                    );

                } catch (Exception e) {

                    System.err.println(
                            "❌ Error expirando orden "
                                    + order.getId()
                                    + " store="
                                    + store.getNombre()
                    );

                    e.printStackTrace();
                }
            }
        }

        System.out.println(
                "✔️ OrderExpirationScheduler finalizado."
        );
    }
}