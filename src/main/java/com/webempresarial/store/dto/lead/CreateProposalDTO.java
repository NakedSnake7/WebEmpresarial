package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProposalDTO(
        @NotBlank String title,
        String description,
        @NotNull BigDecimal amount,
        Integer closeProbability
) {}