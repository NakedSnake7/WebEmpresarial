package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeadCardDTO(
	    Long id,
	    String fullName,
	    String businessName,
	    String email,
	    String phone,
	    String status,
	    String temperature,
	    String priority,
	    Integer score,
	    BigDecimal projectedValue,
	    String source,
	    String ownerName,
	    LocalDateTime createdAt,
	    LocalDateTime nextFollowUpAt
	) {}