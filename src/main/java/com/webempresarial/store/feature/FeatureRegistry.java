package com.webempresarial.store.feature;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FeatureRegistry {

    private final Map<Feature, FeatureDefinition> registry =
            new EnumMap<>(Feature.class);

    public void register(FeatureDefinition definition) {

        if (definition == null || definition.getFeature() == null) {
            throw new IllegalArgumentException("FeatureDefinition inválida");
        }

        registry.put(definition.getFeature(), definition);
    }

    public FeatureDefinition get(Feature feature) {
        FeatureDefinition definition = registry.get(feature);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "Feature no registrada: " + feature
            );
        }

        return definition;
    }

    public Collection<FeatureDefinition> getAll() {
        return registry.values();
    }

    public List<FeatureDefinition> getAllOrdered() {
        return registry.values()
                .stream()
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getByCategory(FeatureCategory category) {
        return registry.values()
                .stream()
                .filter(f -> f.getCategory() == category)
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getByMinimumPlan(StorePlan plan) {
        return registry.values()
                .stream()
                .filter(f -> f.getMinimumPlan() == plan)
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getAvailableForPlan(StorePlan plan) {
        return registry.values()
                .stream()
                .filter(FeatureDefinition::isEnabled)
                .filter(f -> f.isAvailableFor(plan))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getLockedForPlan(StorePlan plan) {
        return registry.values()
                .stream()
                .filter(FeatureDefinition::isEnabled)
                .filter(f -> f.requiresUpgradeFrom(plan))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getSidebarFeatures(StorePlan plan) {
        return registry.values()
                .stream()
                .filter(FeatureDefinition::isEnabled)
                .filter(FeatureDefinition::isShowInSidebar)
                .filter(f -> f.isAvailableFor(plan))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public boolean isRegistered(Feature feature) {
        return registry.containsKey(feature);
    }

    public boolean isPremium(Feature feature) {
        return get(feature).isPremium();
    }
}