package com.webempresarial.store.crm.scoring;

public record LeadScoreItem(
        String rule,
        String description,
        int points,
        boolean applied
) {}