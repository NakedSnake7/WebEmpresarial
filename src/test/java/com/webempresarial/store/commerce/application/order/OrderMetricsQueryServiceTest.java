package com.webempresarial.store.commerce.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderMetricsQueryServiceTest {

    @Mock
    private OrderRepository repository;

    private OrderMetricsQueryService service;

    @BeforeEach
    void setUp() {
        service = new OrderMetricsQueryService(repository);
    }

    @Test
    void countOrdersByStore_shouldReturnRepositoryCount() {
        Long storeId = 10L;

        when(repository.countByStoreId(storeId))
                .thenReturn(25L);

        long result =
                service.countOrdersByStore(storeId);

        assertThat(result).isEqualTo(25L);

        verify(repository).countByStoreId(storeId);
    }

    @Test
    void countOrdersByStore_shouldReturnZeroWhenStoreHasNoOrders() {
        Long storeId = 10L;

        when(repository.countByStoreId(storeId))
                .thenReturn(0L);

        long result =
                service.countOrdersByStore(storeId);

        assertThat(result).isZero();

        verify(repository).countByStoreId(storeId);
    }

    @Test
    void countOrdersByStore_shouldRejectNullStoreId() {
        assertThatThrownBy(() ->
                service.countOrdersByStore(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El storeId es obligatorio");

        verifyNoInteractions(repository);
    }
}