package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record CrmUpcomingTaskDTO(
        Long id,
        String title,
        String description,
        String priority,
        String leadName,
        LocalDateTime dueAt
) {}