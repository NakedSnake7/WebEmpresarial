package com.webempresarial.store.dto.lead;

import com.webempresarial.store.crm.scoring.LeadScoreItem;
import java.util.List;

public record LeadScoreDTO(
        int total,
        List<LeadScoreItem> items
) {}