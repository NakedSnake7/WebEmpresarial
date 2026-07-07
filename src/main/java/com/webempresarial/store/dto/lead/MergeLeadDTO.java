package com.webempresarial.store.dto.lead;

import com.webempresarial.store.crm.merge.MergeStrategy;

public record MergeLeadDTO(
        Long sourceLeadId,
        Long targetLeadId,
        MergeStrategy strategy
) {}