package com.webempresarial.store.dto.saas;

import java.util.ArrayList;
import java.util.List;

public class StoreHealthDTO {

    private Long storeId;
    private String storeName;
    private String domain;
    private int overallScore;

    private int brandingScore;
    private int billingScore;
    private int ecommerceScore;
    private int crmScore;
    private int automationScore;
    private int activityScore;

    private String status;
    private List<String> recommendations = new ArrayList<>();

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public int getBrandingScore() {
        return brandingScore;
    }

    public void setBrandingScore(int brandingScore) {
        this.brandingScore = brandingScore;
    }

    public int getBillingScore() {
        return billingScore;
    }

    public void setBillingScore(int billingScore) {
        this.billingScore = billingScore;
    }

    public int getEcommerceScore() {
        return ecommerceScore;
    }

    public void setEcommerceScore(int ecommerceScore) {
        this.ecommerceScore = ecommerceScore;
    }

    public int getCrmScore() {
        return crmScore;
    }

    public void setCrmScore(int crmScore) {
        this.crmScore = crmScore;
    }

    public int getAutomationScore() {
        return automationScore;
    }

    public void setAutomationScore(int automationScore) {
        this.automationScore = automationScore;
    }

    public int getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(int activityScore) {
        this.activityScore = activityScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}