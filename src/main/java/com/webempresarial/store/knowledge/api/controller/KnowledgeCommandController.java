package com.webempresarial.store.knowledge.api.controller;

import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeTenantNotResolvedException;
import com.webempresarial.store.knowledge.api.service.CreateKnowledgeApiService;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeCommandController {

    private final CreateKnowledgeApiService createKnowledgeApiService;
    private final StoreResolver storeResolver;

    public KnowledgeCommandController(
            CreateKnowledgeApiService createKnowledgeApiService,
            StoreResolver storeResolver
    ) {
        this.createKnowledgeApiService =
                Objects.requireNonNull(
                        createKnowledgeApiService,
                        "CreateKnowledgeApiService es obligatorio"
                );

        this.storeResolver =
                Objects.requireNonNull(
                        storeResolver,
                        "StoreResolver es obligatorio"
                );
    }

    @PostMapping
    public ResponseEntity<KnowledgeCreatedResponse> create(
            @Valid
            @RequestBody
            CreateKnowledgeRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication
    ) {
        Store store =
                resolveStore(httpRequest);

        String actor =
                resolveActor(authentication);

        KnowledgeCreatedResponse response =
                createKnowledgeApiService.create(
                        store,
                        request,
                        actor
                );

        URI location =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.id())
                        .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    private Store resolveStore(
            HttpServletRequest request
    ) {
        final Store store;

        try {
            store =
                    storeResolver.getCurrentStore(
                            request
                    );
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

    private String resolveActor(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "No fue posible identificar al usuario autenticado"
            );
        }

        return authentication
                .getName()
                .trim();
    }
}