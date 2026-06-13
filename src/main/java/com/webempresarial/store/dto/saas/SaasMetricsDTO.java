package com.webempresarial.store.dto.saas;

import java.math.BigDecimal;

public class SaasMetricsDTO {

    private long activeStores;

    private long inactiveStores;

    private long activeSubscriptions;

    private long basicStores;

    private long proStores;

    private long premiumStores;
    
    private long totalStores;

    private long stripeConnectedStores;
    
    private long totalLeads;

    private long totalProposals;

    private BigDecimal pipelineValue = BigDecimal.ZERO;

    private BigDecimal revenueForecast = BigDecimal.ZERO;

    private BigDecimal monthlyRecurringRevenue = BigDecimal.ZERO;

    private BigDecimal annualRecurringRevenue = BigDecimal.ZERO;
    
    

	public long getTotalLeads() {
		return totalLeads;
	}

	public void setTotalLeads(long totalLeads) {
		this.totalLeads = totalLeads;
	}

	public long getTotalProposals() {
		return totalProposals;
	}

	public void setTotalProposals(long totalProposals) {
		this.totalProposals = totalProposals;
	}

	public BigDecimal getPipelineValue() {
		return pipelineValue;
	}

	public void setPipelineValue(BigDecimal pipelineValue) {
		this.pipelineValue = pipelineValue;
	}

	public BigDecimal getRevenueForecast() {
		return revenueForecast;
	}

	public void setRevenueForecast(BigDecimal revenueForecast) {
		this.revenueForecast = revenueForecast;
	}

	public long getActiveStores() {
		return activeStores;
	}

	public void setActiveStores(long activeStores) {
		this.activeStores = activeStores;
	}

	public long getInactiveStores() {
		return inactiveStores;
	}

	public void setInactiveStores(long inactiveStores) {
		this.inactiveStores = inactiveStores;
	}

	public long getActiveSubscriptions() {
		return activeSubscriptions;
	}

	public void setActiveSubscriptions(long activeSubscriptions) {
		this.activeSubscriptions = activeSubscriptions;
	}

	public long getBasicStores() {
		return basicStores;
	}

	public void setBasicStores(long basicStores) {
		this.basicStores = basicStores;
	}

	public long getProStores() {
		return proStores;
	}

	public void setProStores(long proStores) {
		this.proStores = proStores;
	}

	public long getPremiumStores() {
		return premiumStores;
	}

	public void setPremiumStores(long premiumStores) {
		this.premiumStores = premiumStores;
	}

	public long getStripeConnectedStores() {
		return stripeConnectedStores;
	}

	public void setStripeConnectedStores(long stripeConnectedStores) {
		this.stripeConnectedStores = stripeConnectedStores;
	}

	public BigDecimal getMonthlyRecurringRevenue() {
		return monthlyRecurringRevenue;
	}

	public void setMonthlyRecurringRevenue(BigDecimal monthlyRecurringRevenue) {
		this.monthlyRecurringRevenue = monthlyRecurringRevenue;
	}

	public BigDecimal getAnnualRecurringRevenue() {
		return annualRecurringRevenue;
	}

	public void setAnnualRecurringRevenue(BigDecimal annualRecurringRevenue) {
		this.annualRecurringRevenue = annualRecurringRevenue;
	}

	public long getTotalStores() {
	    return totalStores;
	}
	
	public void setTotalStores(long totalStores) {
	    this.totalStores = totalStores;
	}
    
}