package com.webempresarial.store.commerce.web.admin.inventory;

import com.webempresarial.store.commerce.application.inventory.InventoryAdjustmentService;
import com.webempresarial.store.commerce.application.inventory.InventoryDashboardService;
import com.webempresarial.store.commerce.application.inventory.InventoryMovementService;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.application.inventory.InventoryProductDetailService;
import com.webempresarial.store.dto.inventory.InventoryAdjustmentRequestDTO;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFeatureAccessContractTest {

    @Mock
    private InventoryDashboardService dashboardService;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private InventoryProductDetailService detailService;

    @Mock
    private InventoryAdjustmentService adjustmentService;

    @Mock
    private InventoryPersistentAlertService alertService;

    @Mock
    private ProductoService productoService;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Principal principal;

    @Mock
    private InventoryAdjustmentRequestDTO adjustment;

    private Store store;

    private InventoryDashboardController dashboardController;
    private InventoryMovementController movementController;
    private InventoryProductController productController;
    private InventoryAlertController alertController;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeResolver.getCurrentStore(request)
        )
        .thenReturn(store);

        dashboardController =
                new InventoryDashboardController(
                        dashboardService,
                        storeResolver,
                        featureAccessService
                );

        movementController =
                new InventoryMovementController(
                        inventoryMovementService,
                        productoService,
                        storeResolver,
                        featureAccessService
                );

        productController =
                new InventoryProductController(
                        detailService,
                        adjustmentService,
                        storeResolver,
                        featureAccessService
                );

        alertController =
                new InventoryAlertController(
                        alertService,
                        storeResolver,
                        featureAccessService
                );
    }

    @Test
    void standardInventoryOperationsShouldRequireInventoryFeature() {

        dashboardController.dashboard(
                request,
                model
        );

        productController.detail(
                1L,
                model,
                request
        );

        productController.adjust(
                1L,
                adjustment,
                redirectAttributes,
                request
        );

        alertController.index(
                request,
                model
        );

        alertController.acknowledge(
                1L,
                request,
                principal,
                redirectAttributes
        );

        verify(
                featureAccessService,
                times(5)
        )
        .requireFeature(
                store,
                Feature.INVENTORY
        );
    }

    @Test
    void kardexShouldRequireInventoryKardexFeature() {

        movementController.list(
                null,
                null,
                null,
                null,
                null,
                null,
                request,
                model
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.INVENTORY_KARDEX
                );

        verify(
                featureAccessService,
                never()
        )
        .requireFeature(
                store,
                Feature.INVENTORY
        );
    }

    @Test
    void lockedInventoryFeatureShouldStopStockAdjustment() {

        doThrow(
                new FeatureLockedException(
                        Feature.INVENTORY
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.INVENTORY
        );

        assertThatThrownBy(
                () ->
                        productController.adjust(
                                1L,
                                adjustment,
                                redirectAttributes,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                adjustmentService
        );
    }

    @Test
    void lockedInventoryFeatureShouldStopAlertMutation() {

        doThrow(
                new FeatureLockedException(
                        Feature.INVENTORY
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.INVENTORY
        );

        assertThatThrownBy(
                () ->
                        alertController.acknowledge(
                                1L,
                                request,
                                principal,
                                redirectAttributes
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                alertService
        );
    }

    @Test
    void lockedKardexShouldStopMovementQueries() {

        doThrow(
                new FeatureLockedException(
                        Feature.INVENTORY_KARDEX
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.INVENTORY_KARDEX
        );

        assertThatThrownBy(
                () ->
                        movementController.list(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                request,
                                model
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                inventoryMovementService,
                productoService
        );
    }
}
