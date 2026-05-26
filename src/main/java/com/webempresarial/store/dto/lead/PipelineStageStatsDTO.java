package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;

import com.webempresarial.store.model.LeadStatus;

public record PipelineStageStatsDTO(
        LeadStatus status,
        Long count,
        BigDecimal value
) {}