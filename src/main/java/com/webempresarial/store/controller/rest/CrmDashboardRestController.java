package com.webempresarial.store.controller.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.webempresarial.store.dto.lead.CrmActivityFeedDTO;
import com.webempresarial.store.dto.lead.CrmUpcomingTaskDTO;
import com.webempresarial.store.dto.lead.CrmDashboardDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CrmDashboardService;
import com.webempresarial.store.service.StoreContextService;

@RestController
@RequestMapping("/api/crm/dashboard")
public class CrmDashboardRestController {

    private final CrmDashboardService crmDashboardService;
    private final StoreContextService storeContextService;

    public CrmDashboardRestController(
            CrmDashboardService crmDashboardService,
            StoreContextService storeContextService
    ) {
        this.crmDashboardService = crmDashboardService;
        this.storeContextService = storeContextService;
    }

    @GetMapping
    public CrmDashboardDTO getDashboard(HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);
        return crmDashboardService.getDashboard(store.getId());
    }
    
    @GetMapping("/activity")
    public List<CrmActivityFeedDTO> getActivity(HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);
        return crmDashboardService.getRecentActivity(store.getId());
    }

    @GetMapping("/tasks")
    public List<CrmUpcomingTaskDTO> getUpcomingTasks(HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);
        return crmDashboardService.getUpcomingTasks(store.getId());
    }
}