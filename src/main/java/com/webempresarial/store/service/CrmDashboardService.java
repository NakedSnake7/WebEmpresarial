package com.webempresarial.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.webempresarial.store.dto.lead.CrmDashboardDTO;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.SalesTaskRepository;

@Service
public class CrmDashboardService {

    private final LeadRepository leadRepository;
    private final SalesTaskRepository salesTaskRepository;

    public CrmDashboardService(
            LeadRepository leadRepository,
            SalesTaskRepository salesTaskRepository
    ) {
        this.leadRepository = leadRepository;
        this.salesTaskRepository = salesTaskRepository;
    }

    public CrmDashboardDTO getDashboard(Long storeId) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endToday = now.toLocalDate().atTime(LocalTime.MAX);

        LocalDateTime startWeek = now
                .toLocalDate()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();

        LocalDateTime startMonth = now
                .toLocalDate()
                .withDayOfMonth(1)
                .atStartOfDay();

        long leadsToday = leadRepository.countLeadsBetween(
                storeId,
                startToday,
                endToday
        );

        long leadsThisWeek = leadRepository.countLeadsBetween(
                storeId,
                startWeek,
                now
        );

        long leadsThisMonth = leadRepository.countLeadsBetween(
                storeId,
                startMonth,
                now
        );

        long hotLeads = leadRepository.countHotLeads(storeId);

        long pendingTasks = salesTaskRepository.countPendingTasks(storeId);

        long overdueTasks = salesTaskRepository.countOverdueTasks(
                storeId,
                now
        );

        long bookedCalls = leadRepository.countBookedCalls(storeId);

        long sentProposals = leadRepository.countSentProposals(storeId);

        BigDecimal pipelineValue = leadRepository.getPipelineValue(storeId);

        BigDecimal revenueForecast = pipelineValue;

        long totalLeads = leadRepository.countAllLeads(storeId);

        long closedLeads = leadRepository.countClosedLeads(storeId);

        BigDecimal closeRate = calculateCloseRate(
                totalLeads,
                closedLeads
        );

        String bestChannel = resolveBestSource(storeId);

        String bestSalesUser = "Sin asignar";

        return new CrmDashboardDTO(
                leadsToday,
                leadsThisWeek,
                leadsThisMonth,
                hotLeads,
                pendingTasks,
                overdueTasks,
                bookedCalls,
                sentProposals,
                pipelineValue,
                revenueForecast,
                closeRate,
                bestChannel,
                bestSalesUser
        );
    }

    private BigDecimal calculateCloseRate(
            long totalLeads,
            long closedLeads
    ) {
        if (totalLeads == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(closedLeads)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalLeads),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private String resolveBestSource(Long storeId) {

        List<Object[]> sources = leadRepository.getLeadsBySourceRaw(storeId);

        if (sources.isEmpty()) {
            return "Sin datos";
        }

        Object source = sources.get(0)[0];

        return source == null
                ? "Sin origen"
                : String.valueOf(source);
    }
}