package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;
import java.util.List;

public record LeadDetailDTO(
        Long id,
        String fullName,
        String businessName,
        String email,
        String phone,
        String status,
        String temperature,
        String priority,
        Integer score,
        String budgetLabel,
        BigDecimal estimatedBudget,
        BigDecimal projectedValue,
        List<LeadActivityDTO> activities,
        List<SalesTaskDTO> tasks,
        List<ProposalDTO> proposals,
        List<LeadAuditLogDTO> auditLogs,
        List<LeadTimelineItemDTO> timeline
) {}