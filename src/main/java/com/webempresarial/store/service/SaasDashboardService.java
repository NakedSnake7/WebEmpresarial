package com.webempresarial.store.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.saas.SaasDashboardDTO;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.ProposalRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

@Service
public class SaasDashboardService {

    private final StoreRepository storeRepository;
    private final LeadRepository leadRepository;
    private final ProposalRepository proposalRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SaasDashboardService(
            StoreRepository storeRepository,
            LeadRepository leadRepository,
            ProposalRepository proposalRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.storeRepository = storeRepository;
        this.leadRepository = leadRepository;
        this.proposalRepository = proposalRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public SaasDashboardDTO getDashboard() {

        long totalStores =
                storeRepository.count();

        long activeStores =
                storeRepository.countByActivaTrue();

        /*
         * Estas métricas siguen usando Store.plan como caché
         * de compatibilidad para distribución visual.
         */
        long basicStores =
                storeRepository.countByPlan(StorePlan.BASIC);

        long proStores =
                storeRepository.countByPlan(StorePlan.PRO);

        long premiumStores =
                storeRepository.countByPlan(StorePlan.PREMIUM);

        long totalLeads =
                leadRepository.countAllPlatformLeads();

        long totalProposals =
                proposalRepository.countAllPlatformProposals();

        BigDecimal globalPipelineValue =
                leadRepository.getGlobalPipelineValue();

        BigDecimal globalRevenueForecast =
                proposalRepository.getGlobalRevenueForecast();

        if (globalPipelineValue == null) {
            globalPipelineValue = BigDecimal.ZERO;
        }

        if (globalRevenueForecast == null) {
            globalRevenueForecast = BigDecimal.ZERO;
        }

        BigDecimal estimatedMRR =
                subscriptionRepository.calculatePlatformMRR();

        if (estimatedMRR == null) {
            estimatedMRR = BigDecimal.ZERO;
        }

        BigDecimal estimatedARR =
                estimatedMRR.multiply(
                        BigDecimal.valueOf(12)
                );

        return new SaasDashboardDTO(
                totalStores,
                activeStores,
                basicStores,
                proStores,
                premiumStores,
                totalLeads,
                totalProposals,
                globalPipelineValue,
                globalRevenueForecast,
                estimatedMRR,
                estimatedARR
        );
    }
}