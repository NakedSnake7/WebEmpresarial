package com.webempresarial.store.commerce.application.order;

import org.springframework.stereotype.Service;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;

@Service
public class OrderMetricsQueryService {

    private final OrderRepository repository;

    public OrderMetricsQueryService(
            OrderRepository repository
    ) {
        this.repository = repository;
    }

    public long countOrdersByStore(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException(
                    "El storeId es obligatorio"
            );
        }

        return repository.countByStoreId(storeId);
    }
}