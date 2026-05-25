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
            CreateTaskDTO dto
    ) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado"));

        SalesTask task = new SalesTask();

        task.setLead(lead);
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setDueAt(dto.dueAt());

        task.setPriority(
                LeadPriority.valueOf(dto.priority())
        );

        task.setStatus(TaskStatus.PENDING);

        task.setCreatedAt(LocalDateTime.now());

        salesTaskRepository.save(task);
    }
    public List<SalesTaskDTO> getAllTasks() {
        return salesTaskRepository.findAll()
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
    public void completeTask(Long taskId) {

        SalesTask task = salesTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        salesTaskRepository.save(task);
    }
}