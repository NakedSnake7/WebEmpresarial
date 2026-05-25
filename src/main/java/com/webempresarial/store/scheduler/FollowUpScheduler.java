package com.webempresarial.store.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.service.AutomationService;

@Component
public class FollowUpScheduler {

    private final LeadRepository leadRepository;
    private final AutomationService automationService;

    public FollowUpScheduler(
            LeadRepository leadRepository,
            AutomationService automationService
    ) {
        this.leadRepository = leadRepository;
        this.automationService = automationService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkFollowUps() {

        LocalDateTime now = LocalDateTime.now();

        List<Lead> leads = leadRepository.findLeadsNeedingFollowUp(now);

        leads.forEach(automationService::onLeadNotResponding24h);
    }
}