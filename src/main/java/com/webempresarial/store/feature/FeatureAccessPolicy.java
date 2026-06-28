package com.webempresarial.store.feature;

import com.webempresarial.store.model.StorePlan;

public final class FeatureAccessPolicy {

    private final StorePlan minimumPlan;
    private final boolean premium;
    private final boolean enabled;

    private FeatureAccessPolicy(Builder builder) {
        this.minimumPlan = builder.minimumPlan;
        this.premium = builder.premium;
        this.enabled = builder.enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FeatureAccessPolicy basic() {
        return builder().minimumPlan(StorePlan.BASIC).premium(false).build();
    }

    public StorePlan getMinimumPlan() { return minimumPlan; }
    public boolean isPremium() { return premium; }
    public boolean isEnabled() { return enabled; }

    public boolean requiresUpgradeFrom(StorePlan currentPlan) {
        if (currentPlan == null) return true;
        return planRank(currentPlan) < planRank(minimumPlan);
    }

    public boolean isAvailableFor(StorePlan plan) {
        if (!enabled || plan == null) return false;
        return planRank(plan) >= planRank(minimumPlan);
    }

    private static int planRank(StorePlan plan) {
        return switch (plan) {
            case BASIC -> 1;
            case PRO -> 2;
            case PREMIUM -> 3;
        };
    }

    public static final class Builder {
        private StorePlan minimumPlan = StorePlan.BASIC;
        private boolean premium = false;
        private boolean enabled = true;

        public Builder minimumPlan(StorePlan minimumPlan) {
            this.minimumPlan = minimumPlan;
            return this;
        }

        public Builder premium(boolean premium) {
            this.premium = premium;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public FeatureAccessPolicy build() {
            return new FeatureAccessPolicy(this);
        }
    }
}