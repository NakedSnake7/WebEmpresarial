package com.webempresarial.store.dto.lead;

import java.time.LocalDateTime;

public record CreateTaskDTO(
        String title,
        String description,
        String priority,
        LocalDateTime dueAt
) {}