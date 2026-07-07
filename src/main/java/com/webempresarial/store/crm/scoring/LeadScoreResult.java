package com.webempresarial.store.crm.scoring;

import java.util.List;

public record LeadScoreResult(
        int total,
        List<LeadScoreItem> items
) {}