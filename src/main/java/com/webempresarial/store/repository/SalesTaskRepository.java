package com.webempresarial.store.repository;

import java.time.LocalDateTime; 
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.entity.SalesTask;
import com.webempresarial.store.model.TaskStatus;

public interface SalesTaskRepository extends JpaRepository<SalesTask, Long> {

    List<SalesTask> findByAssignedToIdAndStatusOrderByDueAtAsc(
        Long userId,
        TaskStatus status
    );

    List<SalesTask> findByDueAtBeforeAndStatus(
        LocalDateTime date,
        TaskStatus status
    );
    List<SalesTask> findByLeadIdOrderByDueAtAsc(Long leadId);

  
}