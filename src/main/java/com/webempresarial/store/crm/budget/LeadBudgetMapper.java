package com.webempresarial.store.crm.budget;

import org.springframework.stereotype.Component;

@Component
public class LeadBudgetMapper {

	public BudgetRange resolve(String rawBudget) {
	    if (rawBudget == null || rawBudget.isBlank()) {
	        return BudgetRange.UNDEFINED;
	    }

	    String value = normalize(rawBudget);

	    if (value.contains("50") || value.contains("plus") || value.contains("mas") || value.contains("mayor")) {
	        return BudgetRange.MORE_THAN_50000;
	    }

	    if (
	            value.contains("20") && value.contains("50")
	            || value.contains("25") && value.contains("50")
	    ) {
	        return BudgetRange.FROM_20000_TO_50000;
	    }

	    if (
	            value.contains("10") && value.contains("25")
	            || value.contains("10") && value.contains("20")
	    ) {
	        return BudgetRange.FROM_10000_TO_20000;
	    }

	    if (value.contains("5") && value.contains("10")) {
	        return BudgetRange.FROM_5000_TO_10000;
	    }

	    if (value.contains("menos") || value.contains("menor")) {
	        return BudgetRange.LESS_THAN_5000;
	    }

	    return BudgetRange.UNDEFINED;
	}

    public String label(String rawBudget) {
        return resolve(rawBudget).getLabel();
    }

    public java.math.BigDecimal estimate(String rawBudget) {
        return resolve(rawBudget).getEstimatedValue();
    }

    private String normalize(String value) {
        return value
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .trim();
    }
}