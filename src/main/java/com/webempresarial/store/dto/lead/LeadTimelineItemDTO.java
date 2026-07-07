package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record LeadTimelineItemDTO(
        String id,
        String type,
        String title,
        String description,
        String icon,
        String color,
        String actor,
        LocalDateTime createdAt
) {}