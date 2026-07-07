package com.webempresarial.store.crm.duplicates;

import com.webempresarial.store.entity.Lead;

public record DuplicateCheckResult(
        boolean duplicated,
        Lead existingLead,
        DuplicateReason reason
) {
    public static DuplicateCheckResult none() {
        return new DuplicateCheckResult(false, null, null);
    }

    public static DuplicateCheckResult found(
            Lead lead,
            DuplicateReason reason
    ) {
        return new DuplicateCheckResult(true, lead, reason);
    }
}