package com.webempresarial.store.crm.scoring;

import com.webempresarial.store.entity.Lead;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadScoringEngine {

    private final List<LeadScoreRule> rules;

    public LeadScoringEngine(List<LeadScoreRule> rules) {
        this.rules = rules;
    }

    public LeadScoreResult calculate(Lead lead) {
        List<LeadScoreItem> items = rules.stream()
                .map(rule -> rule.evaluate(lead))
                .toList();

        int total = items.stream()
                .mapToInt(LeadScoreItem::points)
                .sum();

        return new LeadScoreResult(total, items);
    }
}