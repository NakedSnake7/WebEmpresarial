package com.webempresarial.store.dto.billing;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.webempresarial.store.model.StorePlan;

public class SaaSCheckoutRequestDTO {

    @NotBlank
    private String companyName;

    @NotBlank
    private String domain;

    @NotBlank
    private String ownerName;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private StorePlan plan;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public StorePlan getPlan() {
		return plan;
	}

	public void setPlan(StorePlan plan) {
		this.plan = plan;
	}

    
}