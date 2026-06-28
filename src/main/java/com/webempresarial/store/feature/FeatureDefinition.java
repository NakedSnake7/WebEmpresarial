package com.webempresarial.store.feature;

import com.webempresarial.store.model.Feature;

import java.util.Objects;

public final class FeatureDefinition {

    private final Feature feature;

    private final String displayName;
    private final String shortName;
    private final String slug;
    private final String description;

    private final FeatureCategory category;

    private final String icon;
    private final String color;
    private final String url;

    private final String badge;
    private final String version;
    private final String documentationUrl;
    private final String helpUrl;

    private final int healthWeight;
    private final int order;

    private final String section;
    private final String sectionIcon;

    private final FeatureAccessPolicy accessPolicy;
    private final FeaturePresentation presentation;

    private FeatureDefinition(Builder builder) {
        this.feature = Objects.requireNonNull(builder.feature, "feature es requerido");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName es requerido");

        this.shortName = builder.shortName != null ? builder.shortName : this.displayName;
        this.slug = builder.slug != null ? builder.slug : this.feature.name().toLowerCase();
        this.description = builder.description != null ? builder.description : "";

        this.category = Objects.requireNonNull(builder.category, "category es requerido");

        this.icon = builder.icon != null ? builder.icon : "●";
        this.color = builder.color != null ? builder.color : "secondary";
        this.url = builder.url != null ? builder.url : "#";

        this.badge = builder.badge != null ? builder.badge : "";
        this.version = builder.version != null ? builder.version : "1.0";
        this.documentationUrl = builder.documentationUrl != null ? builder.documentationUrl : "#";
        this.helpUrl = builder.helpUrl != null ? builder.helpUrl : "#";

        this.healthWeight = Math.max(0, builder.healthWeight);
        this.order = builder.order;

        this.section = builder.section != null ? builder.section : resolveDefaultSection(builder.category);
        this.sectionIcon = builder.sectionIcon != null ? builder.sectionIcon : resolveDefaultSectionIcon(builder.category);

        this.accessPolicy = builder.accessPolicy != null
                ? builder.accessPolicy
                : FeatureAccessPolicy.basic();

        this.presentation = builder.presentation != null
                ? builder.presentation
                : FeaturePresentation.defaults();
    }

    public static Builder builder(Feature feature) {
        return new Builder(feature);
    }

  
    public Feature getFeature() { return feature; }
    public String getDisplayName() { return displayName; }
    public String getShortName() { return shortName; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public FeatureCategory getCategory() { return category; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
    public String getUrl() { return url; }
    public String getBadge() { return badge; }
    public String getVersion() { return version; }
    public String getDocumentationUrl() { return documentationUrl; }
    public String getHelpUrl() { return helpUrl; }
    public int getHealthWeight() { return healthWeight; }
    public int getOrder() { return order; }
    public String getSection() { return section; }
    public String getSectionIcon() { return sectionIcon; }
    public FeatureAccessPolicy getAccessPolicy() { return accessPolicy; }
    public FeaturePresentation getPresentation() { return presentation; }

 
    private static String resolveDefaultSection(FeatureCategory category) {
        return switch (category) {
            case ECOMMERCE -> "Ecommerce";
            case CRM -> "CRM Comercial";
            case MARKETING -> "Marketing";
            case AUTOMATION -> "Automatización";
            case BILLING -> "Billing";
            case PLATFORM -> "Plataforma";
            case AI -> "Inteligencia Artificial";
        };
    }

    private static String resolveDefaultSectionIcon(FeatureCategory category) {
        return switch (category) {
            case ECOMMERCE -> "🛒";
            case CRM -> "📊";
            case MARKETING -> "📣";
            case AUTOMATION -> "⚡";
            case BILLING -> "💳";
            case PLATFORM -> "🧩";
            case AI -> "🤖";
        };
    }

    public static final class Builder {

        private final Feature feature;

        private String displayName;
        private String shortName;
        private String slug;
        private String description;
        private FeatureCategory category;

        private String icon = "●";
        private String color = "secondary";
        private String url = "#";

        private String badge = "";
        private String version = "1.0";
        private String documentationUrl = "#";
        private String helpUrl = "#";

        private int healthWeight = 0;
        private int order = 999;

        private String section;
        private String sectionIcon;

        private FeatureAccessPolicy accessPolicy;
        private FeaturePresentation presentation;

        private Builder(Feature feature) {
            this.feature = Objects.requireNonNull(feature, "feature es requerido");
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder slug(String slug) {
            this.slug = slug;
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

        public Builder badge(String badge) {
            this.badge = badge;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder documentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }

        public Builder helpUrl(String helpUrl) {
            this.helpUrl = helpUrl;
            return this;
        }

        public Builder healthWeight(int healthWeight) {
            this.healthWeight = healthWeight;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder section(String section) {
            this.section = section;
            return this;
        }

        public Builder sectionIcon(String sectionIcon) {
            this.sectionIcon = sectionIcon;
            return this;
        }

        public Builder accessPolicy(FeatureAccessPolicy accessPolicy) {
            this.accessPolicy = accessPolicy;
            return this;
        }

        public Builder presentation(FeaturePresentation presentation) {
            this.presentation = presentation;
            return this;
        }

        public FeatureDefinition build() {
            return new FeatureDefinition(this);
        }
    }
}