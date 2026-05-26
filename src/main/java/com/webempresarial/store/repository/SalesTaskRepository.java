package com.webempresarial.store.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webempresarial.store.entity.SalesTask;
import com.webempresarial.store.model.TaskStatus;

public interface SalesTaskRepository extends JpaRepository<SalesTask, Long> {

    List<SalesTask> findByLeadIdOrderByDueAtAsc(Long leadId);

    List<SalesTask> findByLeadStoreIdOrderByDueAtAsc(Long storeId);

    List<SalesTask> findByAssignedToIdAndStatusOrderByDueAtAsc(
            Long userId,
            TaskStatus status
    );

    List<SalesTask> findByDueAtBeforeAndStatus(
            LocalDateTime date,
            TaskStatus status
    );

    @Query("""
        SELECT COUNT(t)
        FROM SalesTask t
        WHERE t.lead.store.id = :storeId
        AND t.status = com.webempresarial.store.model.TaskStatus.PENDING
    """)
    long countPendingTasks(Long storeId);

    @Query("""
        SELECT COUNT(t)
        FROM SalesTask t
        WHERE t.lead.store.id = :storeId
        AND t.status <> com.webempresarial.store.model.TaskStatus.COMPLETED
        AND t.dueAt IS NOT NULL
        AND t.dueAt < :now
    """)
    long countOverdueTasks(Long storeId, LocalDateTime now);
}