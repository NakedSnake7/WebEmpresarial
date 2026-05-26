package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.lead.CreateTaskDTO;
import com.webempresarial.store.dto.lead.SalesTaskDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.entity.SalesTask;
import com.webempresarial.store.model.LeadPriority;
import com.webempresarial.store.model.TaskStatus;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.SalesTaskRepository;

import jakarta.transaction.Transactional;

@Service
public class SalesTaskService {

    private final SalesTaskRepository salesTaskRepository;
    private final LeadRepository leadRepository;

    public SalesTaskService(
            SalesTaskRepository salesTaskRepository,
            LeadRepository leadRepository
    ) {
        this.salesTaskRepository = salesTaskRepository;
        this.leadRepository = leadRepository;
    }
    
    public SalesTask createTask(
            Lead lead,
            String title,
            String description,
            LocalDateTime dueAt,
            LeadPriority priority
    ) {
        SalesTask task = new SalesTask();

        task.setLead(lead);
        task.setTitle(title);
        task.setDescription(description);
        task.setDueAt(dueAt);
        task.setPriority(priority);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        return salesTaskRepository.save(task);
    }
    @Transactional
    public void createManualTask(
            Long leadId,
            Long storeId,
            CreateTaskDTO dto
    ) {

        Lead lead = leadRepository.findByIdAndStoreId(leadId, storeId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado para esta tienda"));

        SalesTask task = new SalesTask();

        task.setLead(lead);
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setDueAt(dto.dueAt());
        task.setPriority(LeadPriority.valueOf(dto.priority()));
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        salesTaskRepository.save(task);
    }
    public List<SalesTaskDTO> getAllTasks(Long storeId) {
        return salesTaskRepository.findByLeadStoreIdOrderByDueAtAsc(storeId)
                .stream()
                .map(task -> new SalesTaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus().name(),
                        task.getPriority().name(),
                        task.getDueAt()
                ))
                .toList();
    }
    @Transactional
    public void completeTask(
            Long taskId,
            Long storeId
    ) {

        SalesTask task = salesTaskRepository.findByIdAndLeadStoreId(taskId, storeId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada para esta tienda"));

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        salesTaskRepository.save(task);
    }
}