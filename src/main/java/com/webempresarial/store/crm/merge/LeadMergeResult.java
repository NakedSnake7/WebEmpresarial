package com.webempresarial.store.crm.merge;

public record LeadMergeResult(
        Long targetLeadId,
        Long sourceLeadId,
        MergeStrategy strategy,
        boolean success,
        String message
) {}