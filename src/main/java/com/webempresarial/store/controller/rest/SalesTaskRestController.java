package com.webempresarial.store.controller.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webempresarial.store.dto.lead.SalesTaskDTO;
import com.webempresarial.store.service.SalesTaskService;

@RestController
@RequestMapping("/api/crm/tasks")
public class SalesTaskRestController {

    private final SalesTaskService salesTaskService;

    public SalesTaskRestController(SalesTaskService salesTaskService) {
        this.salesTaskService = salesTaskService;
    }

    @GetMapping
    public List<SalesTaskDTO> getTasks() {
        return salesTaskService.getAllTasks();
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long id) {
        salesTaskService.completeTask(id);
        return ResponseEntity.ok().build();
    }
}