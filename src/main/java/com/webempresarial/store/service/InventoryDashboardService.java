package com.webempresarial.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.dto.inventory.InventoryDashboardDTO;
import com.webempresarial.store.dto.inventory.InventoryMovementRowDTO;
import com.webempresarial.store.dto.inventory.InventoryProductStockDTO;
import com.webempresarial.store.dto.inventory.InventoryTopMovementDTO;
import com.webempresarial.store.dto.inventory.StockSeverity;
import com.webempresarial.store.entity.InventoryMovement;
import com.webempresarial.store.model.InventoryMovementType;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.InventoryMovementRepository;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;

@Service
public class InventoryDashboardService {

    private static final List<InventoryMovementType> INPUT_TYPES =
            List.of(
                    InventoryMovementType.RESTORE,
                    InventoryMovementType.ADJUSTMENT_IN,
                    InventoryMovementType.PURCHASE,
                    InventoryMovementType.RETURN
            );

    private static final List<InventoryMovementType> OUTPUT_TYPES =
            List.of(
                    InventoryMovementType.SALE,
                    InventoryMovementType.ADJUSTMENT_OUT
            );

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository varianteRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryAlertService inventoryAlertService;

    public InventoryDashboardService(
            ProductoRepository productoRepository,
            ProductoVarianteRepository varianteRepository,
            InventoryMovementRepository movementRepository,
            InventoryAlertService inventoryAlertService
    ) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
        this.movementRepository = movementRepository;
        this.inventoryAlertService = inventoryAlertService;
    }

    @Transactional(readOnly = true)
    public InventoryDashboardDTO getDashboard(
            Store store
    ) {
        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        LocalDateTime last30Days = now.minusDays(30);

        long totalProducts =
                productoRepository.countByStoreId(
                        store.getId()
                );

        List<InventoryProductStockDTO> lowStockItems =
                inventoryAlertService
                        .getLowStockItems(store);

        long outOfStock =
                lowStockItems.stream()
                        .filter(item ->
                                item.severity()
                                        == StockSeverity.OUT_OF_STOCK
                        )
                        .count();

        long lowStock =
                lowStockItems.stream()
                        .filter(item ->
                                item.severity()
                                        != StockSeverity.OUT_OF_STOCK
                        )
                        .count();

        long unitsInToday =
                movementRepository
                        .sumQuantityByTypesAndPeriod(
                                store,
                                INPUT_TYPES,
                                from,
                                to
                        );

        long unitsOutToday =
                movementRepository
                        .sumQuantityByTypesAndPeriod(
                                store,
                                OUTPUT_TYPES,
                                from,
                                to
                        );

        long simpleUnits =
                productoRepository.sumSimpleStock(store);

        long variantUnits =
                varianteRepository.sumVariantStock(store);

        long totalUnits =
                simpleUnits + variantUnits;

        long movementsToday =
                movementRepository.countMovementsByPeriod(
                        store,
                        from,
                        to
                );

        long salesUnitsLast30Days =
                movementRepository.sumSalesUnitsSince(
                        store,
                        last30Days
                );

        BigDecimal simpleValue =
                nvl(
                        productoRepository
                                .calculateSimpleInventoryValue(store)
                );

        BigDecimal variantValue =
                nvl(
                        varianteRepository
                                .calculateVariantInventoryValue(store)
                );

        BigDecimal totalValue =
                simpleValue.add(variantValue);

        BigDecimal averageInventoryUnitValue =
                totalUnits > 0
                        ? totalValue.divide(
                                BigDecimal.valueOf(totalUnits),
                                2,
                                RoundingMode.HALF_UP
                        )
                        : BigDecimal.ZERO;

        BigDecimal dailySalesAverage =
                BigDecimal.valueOf(salesUnitsLast30Days)
                        .divide(
                                BigDecimal.valueOf(30),
                                4,
                                RoundingMode.HALF_UP
                        );

        BigDecimal estimatedStockCoverageDays =
                dailySalesAverage.signum() > 0
                        ? BigDecimal.valueOf(totalUnits)
                                .divide(
                                        dailySalesAverage,
                                        1,
                                        RoundingMode.HALF_UP
                                )
                        : BigDecimal.ZERO;

        List<InventoryMovementRowDTO> recentMovements =
                movementRepository
                        .findTop8ByStoreOrderByCreatedAtDesc(
                                store
                        )
                        .stream()
                        .map(this::toMovementRow)
                        .toList();

        List<InventoryTopMovementDTO> topOutgoing =
                movementRepository
                        .findTopOutgoingProducts(
                                store,
                                OUTPUT_TYPES,
                                last30Days,
                                PageRequest.of(0, 5)
                        );

        List<InventoryTopMovementDTO> slowMovingProducts =
                movementRepository
                        .findSlowMovingProducts(
                                store,
                                last30Days,
                                PageRequest.of(0, 5)
                        );

        return new InventoryDashboardDTO(
                totalProducts,
                outOfStock,
                lowStock,
                unitsInToday,
                unitsOutToday,
                totalValue,
                recentMovements,
                lowStockItems,
                topOutgoing,
                totalUnits,
                movementsToday,
                salesUnitsLast30Days,
                averageInventoryUnitValue,
                estimatedStockCoverageDays,
                slowMovingProducts
        );
    }

    private InventoryMovementRowDTO toMovementRow(
            InventoryMovement movement
    ) {
        return new InventoryMovementRowDTO(
                movement.getId(),
                movement.getCreatedAt(),
                movement.getType(),
                movement.getProducto().getId(),
                movement.getProducto().getProductName(),
                movement.getVariante() != null
                        ? movement.getVariante().getId()
                        : null,
                movement.getVariante() != null
                        ? movement.getVariante().getNombreVisual()
                        : null,
                movement.getOrder() != null
                        ? movement.getOrder().getId()
                        : null,
                movement.getQuantity(),
                movement.getStockBefore(),
                movement.getStockAfter(),
                movement.getReason()
        );
    }

    private BigDecimal nvl(
            BigDecimal value
    ) {
        return value != null
                ? value
                : BigDecimal.ZERO;
    }
}