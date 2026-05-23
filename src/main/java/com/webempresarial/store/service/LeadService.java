package com.webempresarial.store.service;

import com.webempresarial.store.dto.LeadRequestDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.events.LeadCreatedEvent;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.LeadRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LeadService(
            LeadRepository leadRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.leadRepository = leadRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Lead createLead(
            LeadRequestDTO dto,
            Store store
    ) {
        Lead lead = new Lead();

        lead.setStore(store);
        lead.setNombre(dto.getNombre().trim());
        lead.setWhatsapp(dto.getWhatsapp().trim());
        lead.setEmpresa(trimOrNull(dto.getEmpresa()));
        lead.setInstagram(trimOrNull(dto.getInstagram()));
        lead.setServicio(dto.getServicio().trim());
        lead.setPresupuesto(dto.getPresupuesto().trim());
        lead.setObjetivo(trimOrNull(dto.getObjetivo()));
        lead.setSource(trimOrDefault(dto.getSource(), "index"));

        Lead savedLead = leadRepository.save(lead);

        eventPublisher.publishEvent(
                new LeadCreatedEvent(savedLead)
        );

        return savedLead;
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String trimOrDefault(
            String value,
            String defaultValue
    ) {
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }
}