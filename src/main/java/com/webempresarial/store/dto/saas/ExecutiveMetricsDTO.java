package com.webempresarial.store.dto.saas;

import java.math.BigDecimal;

public class ExecutiveMetricsDTO {

    private BigDecimal currentMRR = BigDecimal.ZERO;
    private BigDecimal previousMRR = BigDecimal.ZERO;
    private BigDecimal mrrGrowthPercent = BigDecimal.ZERO;

    private BigDecimal currentARR = BigDecimal.ZERO;
    private BigDecimal arrGrowthPercent = BigDecimal.ZERO;

    private BigDecimal arpu = BigDecimal.ZERO;

    private long activeCustomers;
    private long newCustomersThisMonth;
    private long cancelledCustomersThisMonth;
    private long trialCustomers;
    private long pastDueCustomers;

    private BigDecimal churnRatePercent = BigDecimal.ZERO;
    private BigDecimal trialConversionPercent = BigDecimal.ZERO;

    public BigDecimal getCurrentMRR() {
        return currentMRR;
    }

    public void setCurrentMRR(BigDecimal currentMRR) {
        this.currentMRR = currentMRR;
    }

    public BigDecimal getPreviousMRR() {
        return previousMRR;
    }

    public void setPreviousMRR(BigDecimal previousMRR) {
        this.previousMRR = previousMRR;
    }

    public BigDecimal getMrrGrowthPercent() {
        return mrrGrowthPercent;
    }

    public void setMrrGrowthPercent(BigDecimal mrrGrowthPercent) {
        this.mrrGrowthPercent = mrrGrowthPercent;
    }

    public BigDecimal getCurrentARR() {
        return currentARR;
    }

    public void setCurrentARR(BigDecimal currentARR) {
        this.currentARR = currentARR;
    }

    public BigDecimal getArrGrowthPercent() {
        return arrGrowthPercent;
    }

    public void setArrGrowthPercent(BigDecimal arrGrowthPercent) {
        this.arrGrowthPercent = arrGrowthPercent;
    }

    public BigDecimal getArpu() {
        return arpu;
    }

    public void setArpu(BigDecimal arpu) {
        this.arpu = arpu;
    }

    public long getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(long activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public long getNewCustomersThisMonth() {
        return newCustomersThisMonth;
    }

    public void setNewCustomersThisMonth(long newCustomersThisMonth) {
        this.newCustomersThisMonth = newCustomersThisMonth;
    }

    public long getCancelledCustomersThisMonth() {
        return cancelledCustomersThisMonth;
    }

    public void setCancelledCustomersThisMonth(long cancelledCustomersThisMonth) {
        this.cancelledCustomersThisMonth = cancelledCustomersThisMonth;
    }

    public long getTrialCustomers() {
        return trialCustomers;
    }

    public void setTrialCustomers(long trialCustomers) {
        this.trialCustomers = trialCustomers;
    }

    public long getPastDueCustomers() {
        return pastDueCustomers;
    }

    public void setPastDueCustomers(long pastDueCustomers) {
        this.pastDueCustomers = pastDueCustomers;
    }

    public BigDecimal getChurnRatePercent() {
        return churnRatePercent;
    }

    public void setChurnRatePercent(BigDecimal churnRatePercent) {
        this.churnRatePercent = churnRatePercent;
    }

    public BigDecimal getTrialConversionPercent() {
        return trialConversionPercent;
    }

    public void setTrialConversionPercent(BigDecimal trialConversionPercent) {
        this.trialConversionPercent = trialConversionPercent;
    }
}