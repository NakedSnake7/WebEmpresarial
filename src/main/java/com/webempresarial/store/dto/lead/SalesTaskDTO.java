package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record SalesTaskDTO(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDateTime dueAt
) {}