package com.webempresarial.store.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.LeadActivity;
import com.webempresarial.store.model.ActivityType;
import com.webempresarial.store.repository.LeadActivityRepository;

@Service
public class LeadActivityService {

    private final LeadActivityRepository leadActivityRepository;

    public LeadActivityService(LeadActivityRepository leadActivityRepository) {
        this.leadActivityRepository = leadActivityRepository;
    }

    public void log(
            Lead lead,
            ActivityType type,
            String title,
            String description
    ) {
        LeadActivity activity = new LeadActivity();

        activity.setLead(lead);
        activity.setType(type);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setCreatedAt(LocalDateTime.now());

        leadActivityRepository.save(activity);
    }
}