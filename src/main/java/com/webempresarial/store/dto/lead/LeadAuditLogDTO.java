package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record LeadAuditLogDTO(
        Long id,
        String action,
        String fieldName,
        String oldValue,
        String newValue,
        String actor,
        LocalDateTime createdAt
) {}