package com.webempresarial.store.scheduler;

import java.time.LocalDateTime; 
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.feature.runtime.TraceType;
import com.webempresarial.store.feature.runtime.annotations.Trace;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.service.AutomationService;

@Component
public class FollowUpScheduler {

    private final LeadRepository leadRepository;
    private final StoreRepository storeRepository;
    private final AutomationService automationService;

    public FollowUpScheduler(
            LeadRepository leadRepository,
            StoreRepository storeRepository,
            AutomationService automationService
    ) {
        this.leadRepository = leadRepository;
        this.storeRepository = storeRepository;
        this.automationService = automationService;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Trace(
            type = TraceType.SCHEDULER,name = "FollowUpScheduler.checkFollowUps",
            source = "CRM Scheduler"
    )
    public void checkFollowUps() {

        LocalDateTime now = LocalDateTime.now();

        List<Store> stores = storeRepository.findAll();

        for (Store store : stores) {

            List<Lead> leads = leadRepository.findLeadsNeedingFollowUp(
                    store.getId(),
                    now
            );

            leads.forEach(automationService::onLeadNotResponding24h);
        }
    }
}