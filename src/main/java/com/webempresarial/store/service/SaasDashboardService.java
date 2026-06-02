package com.webempresarial.store.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.saas.SaasDashboardDTO;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.ProposalRepository;
import com.webempresarial.store.repository.StoreRepository;

@Service
public class SaasDashboardService {

    private final StoreRepository storeRepository;
    private final LeadRepository leadRepository;
    private final ProposalRepository proposalRepository;

    public SaasDashboardService(
            StoreRepository storeRepository,
            LeadRepository leadRepository,
            ProposalRepository proposalRepository
    ) {
        this.storeRepository = storeRepository;
        this.leadRepository = leadRepository;
        this.proposalRepository = proposalRepository;
    }

    public SaasDashboardDTO getDashboard() {

        long totalStores = storeRepository.count();
        long activeStores = storeRepository.countByActivaTrue();

        long basicStores = storeRepository.countByPlan(StorePlan.BASIC);
        long proStores = storeRepository.countByPlan(StorePlan.PRO);
        long premiumStores = storeRepository.countByPlan(StorePlan.PREMIUM);

        long totalLeads = leadRepository.countAllPlatformLeads();
        long totalProposals = proposalRepository.countAllPlatformProposals();

        BigDecimal globalPipelineValue = leadRepository.getGlobalPipelineValue();
        BigDecimal globalRevenueForecast = proposalRepository.getGlobalRevenueForecast();

        BigDecimal estimatedMRR = calculateMRR(
                basicStores,
                proStores,
                premiumStores
        );

        BigDecimal estimatedARR = estimatedMRR.multiply(BigDecimal.valueOf(12));

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

    private BigDecimal calculateMRR(
            long basicStores,
            long proStores,
            long premiumStores
    ) {
        BigDecimal basicPrice = BigDecimal.valueOf(499);
        BigDecimal proPrice = BigDecimal.valueOf(999);
        BigDecimal premiumPrice = BigDecimal.valueOf(1999);

        return basicPrice.multiply(BigDecimal.valueOf(basicStores))
                .add(proPrice.multiply(BigDecimal.valueOf(proStores)))
                .add(premiumPrice.multiply(BigDecimal.valueOf(premiumStores)));
    }
}