package com.webempresarial.store.crm.scoring;

import com.webempresarial.store.entity.Lead;

public interface LeadScoreRule {

    LeadScoreItem evaluate(Lead lead);
}