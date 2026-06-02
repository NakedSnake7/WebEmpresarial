package com.webempresarial.store.dto.saas;

import java.math.BigDecimal;

public record SaasDashboardDTO(
        long totalStores,
        long activeStores,
        long basicStores,
        long proStores,
        long premiumStores,
        long totalLeads,
        long totalProposals,
        BigDecimal globalPipelineValue,
        BigDecimal globalRevenueForecast,
        BigDecimal estimatedMRR,
        BigDecimal estimatedARR
) {}