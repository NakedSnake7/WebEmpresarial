package com.webempresarial.store.dto.lead;

import com.webempresarial.store.model.LeadStatus;

public record UpdateLeadStatusDTO(
	    LeadStatus status
	) {}
