package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalDTO(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        Integer closeProbability,
        String status,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {}