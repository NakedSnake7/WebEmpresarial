package com.webempresarial.store.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.lead.CreateLeadDTO;
import com.webempresarial.store.model.LeadTemperature;

@Service
public class LeadScoringService {

    public int calculateScore(CreateLeadDTO dto) {

        int score = 0;

        if (dto.estimatedBudget() != null) {
            if (dto.estimatedBudget().compareTo(new BigDecimal("30000")) >= 0) score += 40;
            else if (dto.estimatedBudget().compareTo(new BigDecimal("15000")) >= 0) score += 25;
            else score += 10;
        }

        if ("GOOGLE_ADS".equalsIgnoreCase(dto.source())) score += 25;
        if ("FACEBOOK_ADS".equalsIgnoreCase(dto.source())) score += 20;
        if ("WHATSAPP".equalsIgnoreCase(dto.source())) score += 30;
        if ("ORGANIC".equalsIgnoreCase(dto.source())) score += 15;

        if (dto.empresa() != null && !dto.empresa().isBlank()) score += 10;
        if (dto.whatsapp() != null && !dto.whatsapp().isBlank()) score += 15;

        return Math.min(score, 100);
    }

    public LeadTemperature resolveTemperature(int score) {
        if (score >= 75) return LeadTemperature.HOT;
        if (score >= 40) return LeadTemperature.WARM;
        return LeadTemperature.COLD;
    }
}