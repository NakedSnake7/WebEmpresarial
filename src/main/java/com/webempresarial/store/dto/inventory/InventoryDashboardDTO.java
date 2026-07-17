package com.webempresarial.store.dto.inventory;

import java.math.BigDecimal;
import java.util.List;

public record InventoryDashboardDTO(
        long totalProducts,
        long outOfStockProducts,
        long lowStockProducts,
        long unitsInToday,
        long unitsOutToday,
        BigDecimal estimatedInventoryValue,
        List<InventoryMovementRowDTO> recentMovements,
        List<InventoryProductStockDTO> lowStockItems,
        List<InventoryTopMovementDTO> topOutgoingProducts,

        long totalUnits,
        long movementsToday,
        long salesUnitsLast30Days,
        BigDecimal averageInventoryUnitValue,
        BigDecimal estimatedStockCoverageDays,
        List<InventoryTopMovementDTO> slowMovingProducts
) {
}