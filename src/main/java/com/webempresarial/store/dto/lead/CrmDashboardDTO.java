package com.webempresarial.store.dto.lead;

import java.math.BigDecimal;

public record CrmDashboardDTO(
	    long leadsToday,
	    long leadsThisWeek,
	    long leadsThisMonth,
	    long hotLeads,
	    long bookedCalls,
	    long sentProposals,
	    BigDecimal pipelineValue,
	    BigDecimal revenueForecast,
	    BigDecimal closeRate,
	    String bestChannel,
	    String bestSalesUser
	) {}
