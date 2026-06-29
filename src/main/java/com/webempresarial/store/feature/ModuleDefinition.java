package com.webempresarial.store.feature;

import com.webempresarial.store.feature.dashboard.DashboardWidgetDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;

import java.util.ArrayList;
import java.util.List;

public final class ModuleDefinition {

    private final String name;
    private final String description;
    private final List<FeatureDefinition> features;
    private final List<SidebarSectionDefinition> sidebarSections;
    private final List<DashboardWidgetDefinition> dashboardWidgets;

    private ModuleDefinition(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.features = List.copyOf(builder.features);
        this.sidebarSections = List.copyOf(builder.sidebarSections);
        this.dashboardWidgets = List.copyOf(builder.dashboardWidgets);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<FeatureDefinition> getFeatures() {
        return features;
    }

    public List<SidebarSectionDefinition> getSidebarSections() {
        return sidebarSections;
    }

    public List<DashboardWidgetDefinition> getDashboardWidgets() {
        return dashboardWidgets;
    }

    public static final class Builder {

        private final String name;
        private String description = "";

        private final List<FeatureDefinition> features = new ArrayList<>();
        private final List<SidebarSectionDefinition> sidebarSections = new ArrayList<>();
        private final List<DashboardWidgetDefinition> dashboardWidgets = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder feature(FeatureDefinition feature) {
            this.features.add(feature);
            return this;
        }

        public Builder sidebarSection(SidebarSectionDefinition section) {
            this.sidebarSections.add(section);
            return this;
        }

        public Builder dashboardWidget(DashboardWidgetDefinition widget) {
            this.dashboardWidgets.add(widget);
            return this;
        }

        public ModuleDefinition build() {
            return new ModuleDefinition(this);
        }
    }
}