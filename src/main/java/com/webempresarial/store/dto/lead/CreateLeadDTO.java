package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;

public record CreateLeadDTO(
        String nombre,
        String whatsapp,
        String empresa,
        String instagram,
        String servicio,
        String presupuesto,
        String objetivo,
        String source,
        String exactSource,
        BigDecimal estimatedBudget
) {}