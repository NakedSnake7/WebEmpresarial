package com.webempresarial.store.feature.automation;

import com.webempresarial.store.model.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AutomationDefinition {

    private final String code;
    private final String name;
    private final String description;
    private final Feature feature;
    private final String trigger;
    private final List<Class<? extends AutomationCondition>> conditions;
    private final List<Class<? extends AutomationAction>> actions;

    private AutomationDefinition(Builder builder) {
        this.code = Objects.requireNonNull(builder.code, "code es requerido");
        this.name = Objects.requireNonNull(builder.name, "name es requerido");
        this.description = builder.description != null ? builder.description : "";
        this.feature = Objects.requireNonNull(builder.feature, "feature es requerido");
        this.trigger = Objects.requireNonNull(builder.trigger, "trigger es requerido");
        this.conditions = List.copyOf(builder.conditions);
        this.actions = List.copyOf(builder.actions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String code() { return code; }
    public String name() { return name; }
    public String description() { return description; }
    public Feature feature() { return feature; }
    public String trigger() { return trigger; }
    public List<Class<? extends AutomationCondition>> conditions() { return conditions; }
    public List<Class<? extends AutomationAction>> actions() { return actions; }

    public static final class Builder {

        private String code;
        private String name;
        private String description = "";
        private Feature feature;
        private String trigger;

        private final List<Class<? extends AutomationCondition>> conditions = new ArrayList<>();
        private final List<Class<? extends AutomationAction>> actions = new ArrayList<>();

        private Builder() {}

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder feature(Feature feature) {
            this.feature = feature;
            return this;
        }

        public Builder trigger(String trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder when(Class<? extends AutomationCondition> condition) {
            this.conditions.add(condition);
            return this;
        }

        public Builder action(Class<? extends AutomationAction> action) {
            this.actions.add(action);
            return this;
        }

        public AutomationDefinition build() {
            return new AutomationDefinition(this);
        }
    }
}