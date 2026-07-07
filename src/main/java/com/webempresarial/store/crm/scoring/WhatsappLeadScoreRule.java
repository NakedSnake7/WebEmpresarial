package com.webempresarial.store.crm.scoring;

import com.webempresarial.store.entity.Lead;
import org.springframework.stereotype.Component;

@Component
public class WhatsappLeadScoreRule implements LeadScoreRule {

    @Override
    public LeadScoreItem evaluate(Lead lead) {
        boolean applied =
                lead.getWhatsapp() != null && !lead.getWhatsapp().isBlank();

        return new LeadScoreItem(
                "whatsapp_present",
                "Tiene WhatsApp",
                applied ? 10 : 0,
                applied
        );
    }
}