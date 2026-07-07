package com.webempresarial.store.crm.scoring;

import com.webempresarial.store.entity.Lead;
import org.springframework.stereotype.Component;

@Component
public class CompanyLeadScoreRule implements LeadScoreRule {

    @Override
    public LeadScoreItem evaluate(Lead lead) {
        boolean applied =
                lead.getEmpresa() != null && !lead.getEmpresa().isBlank();

        return new LeadScoreItem(
                "company_present",
                "Tiene empresa registrada",
                applied ? 20 : 0,
                applied
        );
    }
}