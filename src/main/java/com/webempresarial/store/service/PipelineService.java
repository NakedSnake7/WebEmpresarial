package com.webempresarial.store.service;

import java.util.List; 

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.lead.PipelineStageStatsDTO;
import com.webempresarial.store.repository.LeadRepository;
 
@Service
public class PipelineService {

    private final LeadRepository leadRepository;

    public PipelineService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<PipelineStageStatsDTO> getStats(Long storeId) {
        return leadRepository.getPipelineStats(storeId);
    }
}