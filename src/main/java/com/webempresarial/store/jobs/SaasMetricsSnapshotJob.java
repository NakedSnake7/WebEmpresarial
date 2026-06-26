package com.webempresarial.store.jobs;

import com.webempresarial.store.dto.saas.SaasMetricsDTO;
import com.webempresarial.store.entity.SaasMetricSnapshot;
import com.webempresarial.store.repository.SaasMetricSnapshotRepository;
import com.webempresarial.store.service.SaasMetricsService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SaasMetricsSnapshotJob {

    private final SaasMetricsService saasMetricsService;
    private final SaasMetricSnapshotRepository snapshotRepository;

    public SaasMetricsSnapshotJob(
            SaasMetricsService saasMetricsService,
            SaasMetricSnapshotRepository snapshotRepository
    ) {
        this.saasMetricsService = saasMetricsService;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void captureDailySnapshot() {

        SaasMetricsDTO metrics = saasMetricsService.getMetrics();

        SaasMetricSnapshot snapshot = new SaasMetricSnapshot();
        snapshot.setMrr(metrics.getMonthlyRecurringRevenue());
        snapshot.setArr(metrics.getAnnualRecurringRevenue());
        snapshot.setActiveSubscriptions(metrics.getActiveSubscriptions());
        snapshot.setActiveStores(metrics.getActiveStores());

        snapshotRepository.save(snapshot);
    }
}