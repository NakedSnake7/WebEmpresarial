package com.webempresarial.store.crm.scoring;

import com.webempresarial.store.entity.Lead;
import org.springframework.stereotype.Component;

@Component
public class ObjectiveLeadScoreRule implements LeadScoreRule {

    @Override
    public LeadScoreItem evaluate(Lead lead) {
        boolean applied =
                lead.getObjetivo() != null && !lead.getObjetivo().isBlank();

        return new LeadScoreItem(
                "objective_defined",
                "Tiene objetivo definido",
                applied ? 15 : 0,
                applied
        );
    }
}