package com.webempresarial.store.feature;

public final class FeaturePresentation {

    private final boolean showInSidebar;
    private final boolean showInDashboard;
    private final boolean showInBilling;
    private final boolean showUpgradeCard;
    private final boolean trackUsage;

    private FeaturePresentation(Builder builder) {
        this.showInSidebar = builder.showInSidebar;
        this.showInDashboard = builder.showInDashboard;
        this.showInBilling = builder.showInBilling;
        this.showUpgradeCard = builder.showUpgradeCard;
        this.trackUsage = builder.trackUsage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FeaturePresentation defaults() {
        return builder().build();
    }

    public boolean isShowInSidebar() { return showInSidebar; }
    public boolean isShowInDashboard() { return showInDashboard; }
    public boolean isShowInBilling() { return showInBilling; }
    public boolean isShowUpgradeCard() { return showUpgradeCard; }
    public boolean isTrackUsage() { return trackUsage; }

    public static final class Builder {
        private boolean showInSidebar = true;
        private boolean showInDashboard = true;
        private boolean showInBilling = true;
        private boolean showUpgradeCard = true;
        private boolean trackUsage = true;

        public Builder showInSidebar(boolean value) {
            this.showInSidebar = value;
            return this;
        }

        public Builder showInDashboard(boolean value) {
            this.showInDashboard = value;
            return this;
        }

        public Builder showInBilling(boolean value) {
            this.showInBilling = value;
            return this;
        }

        public Builder showUpgradeCard(boolean value) {
            this.showUpgradeCard = value;
            return this;
        }

        public Builder trackUsage(boolean value) {
            this.trackUsage = value;
            return this;
        }

        public FeaturePresentation build() {
            return new FeaturePresentation(this);
        }
    }
}