package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;

public record ProposalDTO(
        Long id,
        String title,
        BigDecimal amount,
        Integer closeProbability,
        String status
) {}