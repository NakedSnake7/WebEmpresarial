package com.webempresarial.store.feature;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformKernel {

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
            throw new IllegalArgumentException("Feature no registrada: " + feature);
        }

        return definition;
    }

    public boolean hasFeature(Store store, Feature feature) {
        if (store == null || feature == null || !store.isActiva()) {
            return false;
        }

        if (!isRegistered(feature)) {
            return false;
        }

        return get(feature)
                .getAccessPolicy()
                .isAvailableFor(store.getPlan());
    }

    public boolean isLocked(Store store, Feature feature) {
        return !hasFeature(store, feature);
    }

    public Collection<FeatureDefinition> getAll() {
        return registry.values();
    }

    public List<FeatureDefinition> getAllOrdered() {
        return ordered(registry.values().stream().toList());
    }

    public List<FeatureDefinition> available(Store store) {
        if (store == null || !store.isActiva()) {
            return List.of();
        }

        return registry.values()
                .stream()
                .filter(this::enabled)
                .filter(f -> f.getAccessPolicy().isAvailableFor(store.getPlan()))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> locked(Store store) {
        if (store == null || !store.isActiva()) {
            return List.of();
        }

        return registry.values()
                .stream()
                .filter(this::enabled)
                .filter(f -> f.getAccessPolicy().requiresUpgradeFrom(store.getPlan()))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> sidebar(Store store) {
        return available(store)
                .stream()
                .filter(f -> f.getPresentation().isShowInSidebar())
                .toList();
    }

    public List<FeatureDefinition> dashboard(Store store) {
        return available(store)
                .stream()
                .filter(f -> f.getPresentation().isShowInDashboard())
                .toList();
    }

    public List<FeatureDefinition> upgrades(Store store) {
        return locked(store)
                .stream()
                .filter(f -> f.getPresentation().isShowUpgradeCard())
                .toList();
    }

    public List<FeatureDefinition> billing(Store store) {
        return available(store)
                .stream()
                .filter(f -> f.getPresentation().isShowInBilling())
                .toList();
    }

    public List<FeatureDefinition> trackable(Store store) {
        return available(store)
                .stream()
                .filter(f -> f.getPresentation().isTrackUsage())
                .toList();
    }

    public List<FeatureDefinition> health(Store store) {
        return available(store)
                .stream()
                .filter(f -> f.getHealthWeight() > 0)
                .toList();
    }

    public List<FeatureDefinition> byCategory(FeatureCategory category) {
        if (category == null) {
            return List.of();
        }

        return registry.values()
                .stream()
                .filter(this::enabled)
                .filter(f -> f.getCategory() == category)
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public boolean isRegistered(Feature feature) {
        return registry.containsKey(feature);
    }

    public boolean isPremium(Feature feature) {
        return get(feature).getAccessPolicy().isPremium();
    }

    public List<FeatureDefinition> getByCategory(FeatureCategory category) {
        return byCategory(category);
    }

    public List<FeatureDefinition> getByMinimumPlan(StorePlan plan) {
        if (plan == null) {
            return List.of();
        }

        return registry.values()
                .stream()
                .filter(this::enabled)
                .filter(f -> f.getAccessPolicy().getMinimumPlan() == plan)
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getAvailableForPlan(StorePlan plan) {
        if (plan == null) {
            return List.of();
        }

        return registry.values()
                .stream()
                .filter(this::enabled)
                .filter(f -> f.getAccessPolicy().isAvailableFor(plan))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getLockedForPlan(StorePlan plan) {
        if (plan == null) {
            return List.of();
        }

        return registry.values()
                .stream()
                .filter(this::enabled)
                .filter(f -> f.getAccessPolicy().requiresUpgradeFrom(plan))
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }

    public List<FeatureDefinition> getSidebarFeatures(StorePlan plan) {
        return getAvailableForPlan(plan)
                .stream()
                .filter(f -> f.getPresentation().isShowInSidebar())
                .toList();
    }

    private boolean enabled(FeatureDefinition definition) {
        return definition.getAccessPolicy().isEnabled();
    }

    private List<FeatureDefinition> ordered(List<FeatureDefinition> features) {
        return features.stream()
                .sorted(Comparator.comparingInt(FeatureDefinition::getOrder))
                .toList();
    }
}