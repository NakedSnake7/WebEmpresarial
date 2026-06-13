package com.webempresarial.store.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.saas.SaasMetricsDTO;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.ProposalRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

@Service
public class SaasMetricsService {

    private final StoreRepository storeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LeadRepository leadRepository;
    private final ProposalRepository proposalRepository;

    public SaasMetricsService(
            StoreRepository storeRepository,
            SubscriptionRepository subscriptionRepository,
            LeadRepository leadRepository,
            ProposalRepository proposalRepository
    ) {
        this.storeRepository = storeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.leadRepository = leadRepository;
        this.proposalRepository = proposalRepository;
    }

    public SaasMetricsDTO getMetrics() {

        SaasMetricsDTO dto = new SaasMetricsDTO();

        long activeStores = storeRepository.countByActivaTrue();
        long inactiveStores = storeRepository.countByActivaFalse();

        dto.setActiveStores(activeStores);
        dto.setInactiveStores(inactiveStores);
        dto.setTotalStores(activeStores + inactiveStores);

        dto.setBasicStores(storeRepository.countByPlan(StorePlan.BASIC));
        dto.setProStores(storeRepository.countByPlan(StorePlan.PRO));
        dto.setPremiumStores(storeRepository.countByPlan(StorePlan.PREMIUM));

        dto.setStripeConnectedStores(storeRepository.countByStripeConnectedTrue());

        dto.setActiveSubscriptions(
                subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE)
        );

        BigDecimal mrr = subscriptionRepository
                .sumMonthlyAmountByStatus(SubscriptionStatus.ACTIVE);

        if (mrr == null) {
            mrr = BigDecimal.ZERO;
        }

        dto.setMonthlyRecurringRevenue(mrr);
        dto.setAnnualRecurringRevenue(mrr.multiply(BigDecimal.valueOf(12)));

        dto.setTotalLeads(
                leadRepository.countAllPlatformLeads()
        );

        dto.setTotalProposals(
                proposalRepository.countAllPlatformProposals()
        );

        BigDecimal pipelineValue =
                leadRepository.getGlobalPipelineValue();

        if (pipelineValue == null) {
            pipelineValue = BigDecimal.ZERO;
        }

        dto.setPipelineValue(pipelineValue);

        BigDecimal revenueForecast =
                proposalRepository.getGlobalRevenueForecast();

        if (revenueForecast == null) {
            revenueForecast = BigDecimal.ZERO;
        }

        dto.setRevenueForecast(revenueForecast);

        return dto;
    }
}