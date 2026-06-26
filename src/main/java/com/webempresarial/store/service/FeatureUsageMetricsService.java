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

    public List<FeatureUsageMetricDTO> getUsageByFeature(int days) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        return featureUsageRepository
                .countUsageByFeatureBetween(start, end)
                .stream()
                .map(row -> new FeatureUsageMetricDTO(
                        (Feature) row[0],
                        ((Number) row[1]).longValue()
                ))
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