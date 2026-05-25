package com.webempresarial.store.entity;

import java.time.LocalDateTime;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.LeadPriority;
import com.webempresarial.store.model.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_tasks")
public class SalesTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Lead lead;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private LeadPriority priority = LeadPriority.MEDIUM;

    private LocalDateTime dueAt;
    private LocalDateTime completedAt;

    @ManyToOne
    private AdminUser assignedTo;

    private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Lead getLead() {
		return lead;
	}

	public void setLead(Lead lead) {
		this.lead = lead;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public LeadPriority getPriority() {
		return priority;
	}

	public void setPriority(LeadPriority priority) {
		this.priority = priority;
	}

	public LocalDateTime getDueAt() {
		return dueAt;
	}

	public void setDueAt(LocalDateTime dueAt) {
		this.dueAt = dueAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public AdminUser getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(AdminUser assignedTo) {
		this.assignedTo = assignedTo;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
    
    
}
