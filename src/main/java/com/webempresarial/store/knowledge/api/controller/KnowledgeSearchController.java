package com.webempresarial.store.knowledge.api.controller;

import com.webempresarial.store.knowledge.api.dto.KnowledgeDetailResponse; 
import com.webempresarial.store.knowledge.api.dto.KnowledgePageResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeSearchRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeSummaryResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeVersionSummaryResponse;
import com.webempresarial.store.knowledge.api.service.KnowledgeDetailApiService;
import com.webempresarial.store.knowledge.api.service.KnowledgeSearchApiService;
import com.webempresarial.store.knowledge.api.service.KnowledgeVersionQueryApiService;
import com.webempresarial.store.knowledge.api.exception.KnowledgeTenantNotResolvedException;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeSearchController {

    private final KnowledgeSearchApiService knowledgeSearchApiService;
    private final KnowledgeDetailApiService knowledgeDetailApiService;
    private final KnowledgeVersionQueryApiService knowledgeVersionQueryApiService;
    private final StoreResolver storeResolver;

    public KnowledgeSearchController(
            KnowledgeSearchApiService knowledgeSearchApiService,
            KnowledgeDetailApiService knowledgeDetailApiService,
            KnowledgeVersionQueryApiService knowledgeVersionQueryApiService,
            StoreResolver storeResolver
    ) {
        this.knowledgeSearchApiService =
                Objects.requireNonNull(
                        knowledgeSearchApiService,
                        "KnowledgeSearchApiService es obligatorio"
                );

        this.knowledgeDetailApiService =
                Objects.requireNonNull(
                        knowledgeDetailApiService,
                        "KnowledgeDetailApiService es obligatorio"
                );
        this.knowledgeVersionQueryApiService =
                Objects.requireNonNull(
                		knowledgeVersionQueryApiService,
                        "knowledgeVersionQueryApiService es obligatorio"
                );

        this.storeResolver =
                Objects.requireNonNull(
                        storeResolver,
                        "StoreResolver es obligatorio"
                );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<KnowledgeDetailResponse> findByCode(
            @PathVariable
            String code,
            HttpServletRequest httpRequest
    ) {
        Store store =
                resolveStore(httpRequest);

        KnowledgeDetailResponse response =
                knowledgeDetailApiService.findByCode(
                        store.getId(),
                        code
                );

        return ResponseEntity.ok(response);
    }
    
    
    
    @GetMapping("/{knowledgeObjectId}")
    public ResponseEntity<KnowledgeDetailResponse> findById(
            @PathVariable
            Long knowledgeObjectId,
            HttpServletRequest httpRequest
    ) {
        Store store =
                resolveStore(httpRequest);

        KnowledgeDetailResponse response =
                knowledgeDetailApiService.findById(
                        store.getId(),
                        knowledgeObjectId
                );

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{knowledgeObjectId}/versions")
    public ResponseEntity<List<KnowledgeVersionSummaryResponse>>
    findVersions(
            @PathVariable Long knowledgeObjectId,
            HttpServletRequest request
    ) {
        Store store =
                resolveStore(request);

        List<KnowledgeVersionSummaryResponse> response =
                knowledgeVersionQueryApiService.findAll(
                        store.getId(),
                        knowledgeObjectId
                );

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/search")
    public ResponseEntity<
            KnowledgePageResponse<KnowledgeSummaryResponse>
            > search(
            @Valid
            @RequestBody
            KnowledgeSearchRequest request,
            HttpServletRequest httpRequest
    ) {
        Store store =
                resolveStore(httpRequest);

        KnowledgePageResponse<KnowledgeSummaryResponse> response =
                knowledgeSearchApiService.search(
                        store.getId(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    private Store resolveStore(
            HttpServletRequest request
    ) {
        final Store store;

        try {
            store = storeResolver.getCurrentStore(request);
        } catch (RuntimeException exception) {
            throw new KnowledgeTenantNotResolvedException(
                    "No fue posible resolver la tienda de la solicitud"
            );
        }

        if (store == null || store.getId() == null) {
            throw new KnowledgeTenantNotResolvedException(
                    "No fue posible resolver la tienda de la solicitud"
            );
        }

        if (!store.isActiva()) {
            throw new KnowledgeTenantNotResolvedException(
                    "La tienda se encuentra inactiva"
            );
        }

        return store;
    }
}