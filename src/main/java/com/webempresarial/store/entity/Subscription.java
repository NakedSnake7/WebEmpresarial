package com.webempresarial.store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(
        name = "store_id",
        nullable = false,
        unique = true
    )
    private Store store;

    @Column(length = 100)
    private String stripeCustomerId;

    @Column(length = 100)
    private String stripeSubscriptionId;

    @Column(length = 100)
    private String stripePriceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StorePlan plan = StorePlan.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StorePlan pendingPlan;

    private LocalDateTime pendingPlanEffectiveAt;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private LocalDateTime nextBillingDate;

    private LocalDateTime currentPeriodStart;

    private LocalDateTime currentPeriodEnd;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal monthlyAmount;

    @Column(length = 3)
    private String currency = "MXN";

    @Column(nullable = false)
    private boolean billingExempt = false;
    
    @Column(nullable = false)
    private boolean cancelAtPeriodEnd = false;

	@PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
	}

	public String getStripeSubscriptionId() {
		return stripeSubscriptionId;
	}

	public void setStripeSubscriptionId(String stripeSubscriptionId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
	}

	public String getStripePriceId() {
		return stripePriceId;
	}

	public void setStripePriceId(String stripePriceId) {
		this.stripePriceId = stripePriceId;
	}

	public StorePlan getPlan() {
		return plan;
	}

	public void setPlan(StorePlan plan) {
		this.plan = plan;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	public LocalDateTime getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(LocalDateTime startsAt) {
		this.startsAt = startsAt;
	}

	public LocalDateTime getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(LocalDateTime endsAt) {
		this.endsAt = endsAt;
	}

	public LocalDateTime getNextBillingDate() {
		return nextBillingDate;
	}

	public void setNextBillingDate(LocalDateTime nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public LocalDateTime getCurrentPeriodStart() {
		return currentPeriodStart;
	}

	public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
		this.currentPeriodStart = currentPeriodStart;
	}

	public LocalDateTime getCurrentPeriodEnd() {
		return currentPeriodEnd;
	}

	public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
		this.currentPeriodEnd = currentPeriodEnd;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
    public BigDecimal getMonthlyAmount() {
		return monthlyAmount;
	}

	public void setMonthlyAmount(BigDecimal monthlyAmount) {
		this.monthlyAmount = monthlyAmount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public boolean isBillingExempt() {
	    return billingExempt;
	}

	public void setBillingExempt(boolean billingExempt) {
	    this.billingExempt = billingExempt;
	}
	
	public boolean isCancelAtPeriodEnd() {
	    return cancelAtPeriodEnd;
	}

	public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
	    this.cancelAtPeriodEnd = cancelAtPeriodEnd;
	}
	
	public StorePlan getPendingPlan() {
	    return pendingPlan;
	}

	public void setPendingPlan(StorePlan pendingPlan) {
	    this.pendingPlan = pendingPlan;
	}

	public LocalDateTime getPendingPlanEffectiveAt() {
	    return pendingPlanEffectiveAt;
	}

	public void setPendingPlanEffectiveAt(LocalDateTime pendingPlanEffectiveAt) {
	    this.pendingPlanEffectiveAt = pendingPlanEffectiveAt;
	}
	// =====================================================
	// DOMAIN BEHAVIOR
	// =====================================================

	public boolean isActive() {
	    return status == SubscriptionStatus.ACTIVE;
	}

	public boolean isTrial() {
	    return status == SubscriptionStatus.TRIAL;
	}

	public boolean isPastDue() {
	    return status == SubscriptionStatus.PAST_DUE;
	}

	public boolean isCancelled() {
	    return status == SubscriptionStatus.CANCELLED;
	}

	public boolean isExpired() {
	    return status == SubscriptionStatus.EXPIRED;
	}

	public boolean canAccessPlatform() {
	    return isActive()
	            || isTrial()
	            || isPastDue()
	            || billingExempt;
	}

	public boolean requiresPayment() {
	    return !billingExempt
	            && (isPastDue() || isExpired() || isCancelled());
	}

	public boolean isBillable() {
	    return !billingExempt
	            && stripeSubscriptionId != null
	            && !stripeSubscriptionId.isBlank();
	}

	public boolean isPaidAccess() {
	    return isActive() && !billingExempt;
	}

	public boolean isTrialExpired() {
	    return isTrial()
	            && endsAt != null
	            && endsAt.isBefore(LocalDateTime.now());
	}
}