package com.webempresarial.store.crm.scoring;

import com.webempresarial.store.entity.Lead;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BudgetLeadScoreRule implements LeadScoreRule {

    @Override
    public LeadScoreItem evaluate(Lead lead) {
        BigDecimal budget = lead.getEstimatedBudget();

        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            return new LeadScoreItem(
                    "budget_undefined",
                    "Presupuesto no definido",
                    0,
                    false
            );
        }

        if (budget.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return new LeadScoreItem(
                    "budget_high",
                    "Presupuesto alto",
                    25,
                    true
            );
        }

        if (budget.compareTo(BigDecimal.valueOf(20000)) >= 0) {
            return new LeadScoreItem(
                    "budget_medium_high",
                    "Presupuesto medio-alto",
                    15,
                    true
            );
        }

        if (budget.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return new LeadScoreItem(
                    "budget_medium",
                    "Presupuesto medio",
                    8,
                    true
            );
        }

        return new LeadScoreItem(
                "budget_low",
                "Presupuesto bajo",
                2,
                true
        );
    }
}