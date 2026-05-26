package com.webempresarial.store.controller.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webempresarial.store.dto.lead.PipelineStageStatsDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.PipelineService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/crm/pipeline")
public class PipelineRestController {

    private final PipelineService pipelineService;
    private final StoreContextService storeContextService;

    public PipelineRestController(
            PipelineService pipelineService,
            StoreContextService storeContextService
    ) {
        this.pipelineService = pipelineService;
        this.storeContextService = storeContextService;
    }

    @GetMapping("/stats")
    public List<PipelineStageStatsDTO> getStats(HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);
        return pipelineService.getStats(store.getId());
    }
}