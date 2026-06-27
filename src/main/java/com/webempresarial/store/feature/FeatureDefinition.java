package com.webempresarial.store.feature;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import java.util.Objects;

public final class FeatureDefinition {

    private final Feature feature;
    private final String displayName;
    private final String description;
    private final FeatureCategory category;
    private final StorePlan minimumPlan;
    private final String icon;
    private final String color;
    private final String url;
    private final int healthWeight;
    private final boolean premium;
    private final int order;
    private final boolean showInSidebar;
    private final boolean enabled;

    private FeatureDefinition(Builder builder) {
        this.feature = Objects.requireNonNull(builder.feature, "feature es requerido");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName es requerido");
        this.description = builder.description != null ? builder.description : "";
        this.category = Objects.requireNonNull(builder.category, "category es requerido");
        this.minimumPlan = Objects.requireNonNull(builder.minimumPlan, "minimumPlan es requerido");
        this.icon = builder.icon != null ? builder.icon : "●";
        this.color = builder.color != null ? builder.color : "secondary";
        this.url = builder.url != null ? builder.url : "#";
        this.healthWeight = Math.max(0, builder.healthWeight);
        this.premium = builder.premium;
        this.order = builder.order;
        this.showInSidebar = builder.showInSidebar;
        this.enabled = builder.enabled;
    }

    public static Builder builder(Feature feature) {
        return new Builder(feature);
    }

    public Feature getFeature() {
        return feature;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public FeatureCategory getCategory() {
        return category;
    }

    public StorePlan getMinimumPlan() {
        return minimumPlan;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public String getUrl() {
        return url;
    }

    public int getHealthWeight() {
        return healthWeight;
    }

    public boolean isPremium() {
        return premium;
    }

    public int getOrder() {
        return order;
    }

    public boolean isShowInSidebar() {
        return showInSidebar;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean requiresUpgradeFrom(StorePlan currentPlan) {
        if (currentPlan == null) {
            return true;
        }

        return planRank(currentPlan) < planRank(this.minimumPlan);
    }

    public boolean isAvailableFor(StorePlan plan) {
        if (plan == null) {
            return false;
        }

        return planRank(plan) >= planRank(this.minimumPlan);
    }

    private static int planRank(StorePlan plan) {
        return switch (plan) {
            case BASIC -> 1;
            case PRO -> 2;
            case PREMIUM -> 3;
        };
    }

    public static final class Builder {

        private final Feature feature;

        private String displayName;
        private String description;
        private FeatureCategory category;
        private StorePlan minimumPlan = StorePlan.BASIC;
        private String icon = "●";
        private String color = "secondary";
        private String url = "#";
        private int healthWeight = 0;
        private boolean premium = false;
        private int order = 999;
        private boolean showInSidebar = true;
        private boolean enabled = true;

        private Builder(Feature feature) {
            this.feature = Objects.requireNonNull(feature, "feature es requerido");
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(FeatureCategory category) {
            this.category = category;
            return this;
        }

        public Builder minimumPlan(StorePlan minimumPlan) {
            this.minimumPlan = minimumPlan;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder healthWeight(int healthWeight) {
            this.healthWeight = healthWeight;
            return this;
        }

        public Builder premium(boolean premium) {
            this.premium = premium;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder showInSidebar(boolean showInSidebar) {
            this.showInSidebar = showInSidebar;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public FeatureDefinition build() {
            return new FeatureDefinition(this);
        }
    }
}