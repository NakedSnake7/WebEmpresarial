package com.webempresarial.store.feature;

import com.webempresarial.store.feature.automation.AutomationDefinition;
import com.webempresarial.store.feature.event.ModuleEventDefinition;
import com.webempresarial.store.feature.permission.PermissionDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;

import java.util.ArrayList;
import java.util.List;

public final class PlatformModuleDescriptor {

    private final String name;
    private final String description;

    private final List<FeatureDefinition> features;
    private final List<SidebarSectionDefinition> sidebarSections;
    private final List<PermissionDefinition> permissions;
    private final List<AutomationDefinition> automations;
    private final List<ModuleEventDefinition> events;
    
    

    private PlatformModuleDescriptor(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.features = List.copyOf(builder.features);
        this.sidebarSections = List.copyOf(builder.sidebarSections);
        this.permissions = List.copyOf(builder.permissions);
        this.automations = List.copyOf(builder.automations);
        this.events = List.copyOf(builder.events);
    }
    
    

    public static Builder builder(String name) {
        return new Builder(name);
    }

    
    public List<ModuleEventDefinition> getEvents() {
        return events;
    }
    
    public String getName() { return name; }

    public String getDescription() { return description; }

    public List<FeatureDefinition> getFeatures() { return features; }

    public List<SidebarSectionDefinition> getSidebarSections() { return sidebarSections; }

    public List<PermissionDefinition> getPermissions() { return permissions; }

    public List<AutomationDefinition> getAutomations() { return automations; }

    public static final class Builder {

        private final String name;
        private String description = "";

        private final List<FeatureDefinition> features = new ArrayList<>();
        private final List<SidebarSectionDefinition> sidebarSections = new ArrayList<>();
        private final List<PermissionDefinition> permissions = new ArrayList<>();
        private final List<AutomationDefinition> automations = new ArrayList<>();
        private final List<ModuleEventDefinition> events = new ArrayList<>();
        
        public Builder event(ModuleEventDefinition event) {
            this.events.add(event);
            return this;
        }

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

        public Builder permission(PermissionDefinition permission) {
            this.permissions.add(permission);
            return this;
        }

        public Builder automation(AutomationDefinition automation) {
            this.automations.add(automation);
            return this;
        }

        public PlatformModuleDescriptor build() {
            return new PlatformModuleDescriptor(this);
        }
    }
    
}