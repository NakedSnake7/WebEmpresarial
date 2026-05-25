package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record LeadActivityDTO(
        Long id,
        String type,
        String title,
        String description,
        LocalDateTime createdAt
) {}