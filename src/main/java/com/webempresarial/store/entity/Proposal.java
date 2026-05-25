package com.webempresarial.store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.webempresarial.store.model.ProposalStatus;

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
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Lead lead;

    private String title;

    private BigDecimal amount;

    private Integer closeProbability;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Integer getCloseProbability() {
		return closeProbability;
	}

	public void setCloseProbability(Integer closeProbability) {
		this.closeProbability = closeProbability;
	}

	public ProposalStatus getStatus() {
		return status;
	}

	public void setStatus(ProposalStatus status) {
		this.status = status;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public LocalDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(LocalDateTime acceptedAt) {
		this.acceptedAt = acceptedAt;
	}

	public LocalDateTime getRejectedAt() {
		return rejectedAt;
	}

	public void setRejectedAt(LocalDateTime rejectedAt) {
		this.rejectedAt = rejectedAt;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
    
}