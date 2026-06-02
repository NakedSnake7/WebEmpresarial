package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record CrmActivityFeedDTO(
        Long id,
        String type,
        String title,
        String description,
        String leadName,
        LocalDateTime createdAt
) {}