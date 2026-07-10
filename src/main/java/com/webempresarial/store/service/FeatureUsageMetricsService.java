package com.webempresarial.store.service;

import com.webempresarial.store.dto.saas.FeatureUsageMetricDTO; 
import com.webempresarial.store.dto.saas.TopStoreUsageDTO;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.repository.FeatureUsageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeatureUsageMetricsService {

    private final FeatureUsageRepository featureUsageRepository;

    public FeatureUsageMetricsService(
            FeatureUsageRepository featureUsageRepository
    ) {
        this.featureUsageRepository = featureUsageRepository;
    }
    
    private String resolveIcon(Feature feature) {
        return switch (feature) {
            case PRODUCTS -> "📦";
            case CATEGORIES -> "🏷️";
            case INVENTORY -> "📊";
            case ORDERS -> "🧾";
            case CHECKOUT -> "🛒";
            case CRM -> "📊";
            case LEADS -> "🗂️";
            case TASKS -> "✅";
            case PROPOSALS -> "📄";
            case PIPELINE -> "🎯";
            case REVIEWS -> "⭐";
            case COUPONS -> "🏷️";
            case EMAIL_MARKETING -> "✉️";
            case WHATSAPP_AUTOMATION -> "💬";
            case AUTOMATIONS -> "⚡";
            case CUSTOM_DOMAIN -> "🌐";
            case STRIPE_CONNECT -> "💳";
            case ANALYTICS -> "📈";
            case MULTI_USER -> "👥";
            case API_ACCESS -> "🔌";
            case WHITE_LABEL_FULL -> "🎨";
        };
    }

    private String resolveModule(Feature feature) {
        return switch (feature) {
            case PRODUCTS, CATEGORIES, INVENTORY, ORDERS, CHECKOUT -> "Ecommerce";
            case CRM, LEADS, TASKS, PROPOSALS, PIPELINE -> "CRM";
            case REVIEWS, COUPONS, EMAIL_MARKETING -> "Marketing";
            case WHATSAPP_AUTOMATION, AUTOMATIONS -> "Automation";
            case CUSTOM_DOMAIN, ANALYTICS, MULTI_USER, API_ACCESS, WHITE_LABEL_FULL -> "Platform";
            case STRIPE_CONNECT -> "Billing";
        };
    }

    public List<FeatureUsageMetricDTO> getUsageByFeature(int days) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        return featureUsageRepository
                .countUsageByFeatureBetween(start, end)
                .stream()
                .map(row -> {
                    Feature feature = (Feature) row[0];
                    long total = ((Number) row[1]).longValue();

                    return new FeatureUsageMetricDTO(
                            feature.name(),
                            resolveIcon(feature),
                            resolveModule(feature),
                            total
                    );
                }) 
                .toList();
    }

    public List<TopStoreUsageDTO> getTopStores(int days) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        return featureUsageRepository
                .topStoresByUsageBetween(start, end)
                .stream()
                .map(row -> new TopStoreUsageDTO(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    // Métodos de conveniencia

    public List<FeatureUsageMetricDTO> getUsageLast7Days() {
        return getUsageByFeature(7);
    }

    public List<FeatureUsageMetricDTO> getUsageLast30Days() {
        return getUsageByFeature(30);
    }

    public List<FeatureUsageMetricDTO> getUsageLast90Days() {
        return getUsageByFeature(90);
    }

    public List<TopStoreUsageDTO> getTopStoresLast7Days() {
        return getTopStores(7);
    }

    public List<TopStoreUsageDTO> getTopStoresLast30Days() {
        return getTopStores(30);
    }

    public List<TopStoreUsageDTO> getTopStoresLast90Days() {
        return getTopStores(90);
    }
}