package com.webempresarial.store.controller.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webempresarial.store.dto.lead.SalesTaskDTO;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.SalesTaskService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/crm/tasks")
public class SalesTaskRestController {

    private final SalesTaskService salesTaskService;
    private final StoreContextService storeContextService;
    private final FeatureAccessService featureAccessService;

    public SalesTaskRestController(
            SalesTaskService salesTaskService,
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService
    ) {
        this.salesTaskService = salesTaskService;
        this.storeContextService = storeContextService;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping
    public List<SalesTaskDTO> getTasks(HttpServletRequest request) {
        Store store = storeContextService.getCurrentStore(request);
        featureAccessService.requireFeature(store, Feature.TASKS);
        return salesTaskService.getAllTasks(store.getId());
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeContextService.getCurrentStore(request);
        featureAccessService.requireFeature(store, Feature.TASKS);
        salesTaskService.completeTask(id, store.getId());
        return ResponseEntity.ok().build();
    }
}