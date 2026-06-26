package com.webempresarial.store.service;

import com.webempresarial.store.dto.saas.ExecutiveMetricsDTO;
import com.webempresarial.store.dto.saas.SaasMetricsDTO;
import com.webempresarial.store.entity.SaasMetricSnapshot;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.SaasMetricSnapshotRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExecutiveMetricsService {

    private final SaasMetricsService saasMetricsService;
    private final SubscriptionRepository subscriptionRepository;
    private final SaasMetricSnapshotRepository snapshotRepository;

    public ExecutiveMetricsService(
            SaasMetricsService saasMetricsService,
            SubscriptionRepository subscriptionRepository,
            SaasMetricSnapshotRepository snapshotRepository
    ) {
        this.saasMetricsService = saasMetricsService;
        this.subscriptionRepository = subscriptionRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public ExecutiveMetricsDTO getMetrics() {

        ExecutiveMetricsDTO dto = new ExecutiveMetricsDTO();

        SaasMetricsDTO current = saasMetricsService.getMetrics();

        BigDecimal currentMRR = safe(current.getMonthlyRecurringRevenue());
        BigDecimal currentARR = safe(current.getAnnualRecurringRevenue());

        dto.setCurrentMRR(currentMRR);
        dto.setCurrentARR(currentARR);
        dto.setActiveCustomers(current.getActiveSubscriptions());

        BigDecimal previousMRR = getPreviousMRR();
        dto.setPreviousMRR(previousMRR);

        dto.setMrrGrowthPercent(percentGrowth(previousMRR, currentMRR));
        dto.setArrGrowthPercent(percentGrowth(previousMRR.multiply(BigDecimal.valueOf(12)), currentARR));

        if (current.getActiveSubscriptions() > 0) {
            dto.setArpu(
                    currentMRR.divide(
                            BigDecimal.valueOf(current.getActiveSubscriptions()),
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        }

        LocalDateTime monthStart =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime now = LocalDateTime.now();

        dto.setNewCustomersThisMonth(
                subscriptionRepository.countByStatusAndCreatedAtBetween(
                        SubscriptionStatus.ACTIVE,
                        monthStart,
                        now
                )
        );

        dto.setCancelledCustomersThisMonth(
                subscriptionRepository.countByStatusAndCreatedAtBetween(
                        SubscriptionStatus.CANCELLED,
                        monthStart,
                        now
                )
        );

        dto.setTrialCustomers(
                subscriptionRepository.countByStatus(
                        SubscriptionStatus.TRIAL
                )
        );

        dto.setPastDueCustomers(
                subscriptionRepository.countByStatus(
                        SubscriptionStatus.PAST_DUE
                )
        );

        dto.setChurnRatePercent(
                calculateChurnRate(
                        dto.getCancelledCustomersThisMonth(),
                        current.getActiveSubscriptions()
                )
        );

        dto.setTrialConversionPercent(
                calculateTrialConversion()
        );

        return dto;
    }

    private BigDecimal getPreviousMRR() {

        LocalDate since = LocalDate.now().minusDays(60);

        List<SaasMetricSnapshot> snapshots =
                snapshotRepository
                        .findBySnapshotDateAfterOrderBySnapshotDateAsc(since);

        if (snapshots == null || snapshots.isEmpty()) {
            return BigDecimal.ZERO;
        }

        LocalDate targetDate = LocalDate.now().minusDays(30);

        return snapshots.stream()
                .filter(s -> !s.getSnapshotDate().isAfter(targetDate))
                .reduce((first, second) -> second)
                .map(SaasMetricSnapshot::getMrr)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal percentGrowth(
            BigDecimal previous,
            BigDecimal current
    ) {
        previous = safe(previous);
        current = safe(current);

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateChurnRate(
            long cancelledThisMonth,
            long activeCustomers
    ) {
        long base = activeCustomers + cancelledThisMonth;

        if (base <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(cancelledThisMonth)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(base), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTrialConversion() {

        long active =
                subscriptionRepository.countByStatus(
                        SubscriptionStatus.ACTIVE
                );

        long trial =
                subscriptionRepository.countByStatus(
                        SubscriptionStatus.TRIAL
                );

        long total = active + trial;

        if (total <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(active)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}