package com.webempresarial.store.repository;

import com.webempresarial.store.entity.FeatureUsage;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface FeatureUsageRepository extends JpaRepository<FeatureUsage, Long> {

    long countByStoreAndFeature(Store store, Feature feature);

    long countByFeatureAndUsedAtBetween(
            Feature feature,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT fu.feature, COUNT(fu)
        FROM FeatureUsage fu
        WHERE fu.usedAt BETWEEN :start AND :end
        GROUP BY fu.feature
        ORDER BY COUNT(fu) DESC
    """)
    List<Object[]> countUsageByFeatureBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT fu.store.nombre, fu.store.dominio, COUNT(fu)
        FROM FeatureUsage fu
        WHERE fu.usedAt BETWEEN :start AND :end
        GROUP BY fu.store.id, fu.store.nombre, fu.store.dominio
        ORDER BY COUNT(fu) DESC
    """)
    List<Object[]> topStoresByUsageBetween(
            LocalDateTime start,
            LocalDateTime end
    );
    long countByStoreAndUsedAtAfter(
            Store store,
            LocalDateTime usedAt
    );
}