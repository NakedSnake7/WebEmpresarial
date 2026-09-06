package com.webempresarial.store.commerce.application.inventory;

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

import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository repository;

    private InventoryMovementService service;

    @BeforeEach
    void setUp() {
        service = new InventoryMovementService(repository);
    }

    @Test
    void hasMovementsForOrder_shouldReturnTrueWhenMovementsExist() {
        Long orderId = 100L;

        when(repository.existsByOrderId(orderId))
                .thenReturn(true);

        boolean result =
                service.hasMovementsForOrder(orderId);

        assertThat(result).isTrue();

        verify(repository)
                .existsByOrderId(orderId);
    }

    @Test
    void hasMovementsForOrder_shouldReturnFalseWhenMovementsDoNotExist() {
        Long orderId = 100L;

        when(repository.existsByOrderId(orderId))
                .thenReturn(false);

        boolean result =
                service.hasMovementsForOrder(orderId);

        assertThat(result).isFalse();

        verify(repository)
                .existsByOrderId(orderId);
    }

    @Test
    void hasMovementsForOrder_shouldRejectNullOrderId() {
        assertThatThrownBy(() ->
                service.hasMovementsForOrder(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El orderId es obligatorio");

        verifyNoInteractions(repository);
    }
}