package com.webempresarial.store.knowledge.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;   
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.knowledge.api.dto.KnowledgeDetailResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgePageResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeSearchRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeSummaryResponse;
import com.webempresarial.store.knowledge.api.exception.KnowledgeApiExceptionHandler;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.api.service.KnowledgeDetailApiService;
import com.webempresarial.store.knowledge.api.service.KnowledgeSearchApiService;
import com.webempresarial.store.knowledge.api.service.KnowledgeVersionDetailApiService;
import com.webempresarial.store.knowledge.api.service.KnowledgeVersionQueryApiService;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.service.SubscriptionAccessService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = KnowledgeQueryController.class
)
@Import({
        SecurityConfig.class,
        KnowledgeApiExceptionHandler.class
})
class KnowledgeQueryControllerMockMvcTest {

    private static final String ACTOR =
            "admin@webempresarial.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeSearchApiService knowledgeSearchApiService;

    @MockitoBean
    private KnowledgeDetailApiService knowledgeDetailApiService;

    @MockitoBean
    private KnowledgeVersionQueryApiService knowledgeVersionQueryApiService;

    @MockitoBean
    private KnowledgeVersionDetailApiService knowledgeVersionDetailApiService;

    @MockitoBean
    private StoreResolver storeResolver;

    /*
     * Dependencias requeridas por SecurityConfig
     * y componentes MVC globales.
     */
    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService
            inventoryPersistentAlertService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private SubscriptionAccessService subscriptionAccessService;

    @MockitoBean
    private StoreThemeResolver storeThemeResolver;
    
    @Test
    void shouldSearchKnowledgeAndReturn200()
            throws Exception {

        Store store = createStore();

        KnowledgeSummaryResponse item =
                createSummaryResponse();

        KnowledgePageResponse<KnowledgeSummaryResponse> serviceResponse =
                new KnowledgePageResponse<>(
                        List.of(item),
                        0,
                        20,
                        1L,
                        1,
                        true,
                        true,
                        false
                );

        KnowledgeSearchRequest request =
                createSearchRequest();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                knowledgeSearchApiService.search(
                        eq(store.getId()),
                        any(KnowledgeSearchRequest.class)
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                get("/api/knowledge")
                        .with(
                                user(ACTOR)
                                        .roles("STORE_ADMIN")
                        )
                        .param(
                                "domain",
                                request.domain().name()
                        )
                        .param(
                                "status",
                                request.status().name()
                        )
                        .param(
                                "minimumConfidence",
                                request.minimumConfidence()
                                        .toPlainString()
                        )
                        .param(
                                "effectiveAt",
                                request.effectiveAt()
                                        .toString()
                        )
                        .param(
                                "text",
                                request.text()
                        )
                        .param(
                                "page",
                                String.valueOf(request.page())
                        )
                        .param(
                                "size",
                                String.valueOf(request.size())
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.content").isArray()
        )
        .andExpect(
                jsonPath("$.content.length()").value(1)
        )
        .andExpect(
                jsonPath("$.content[0].id").value(101)
        )
        .andExpect(
                jsonPath("$.content[0].code").value("KS-901")
        )
        .andExpect(
                jsonPath("$.content[0].status")
                        .value("PUBLISHED")
        )
        .andExpect(
                jsonPath("$.content[0].semanticVersion")
                        .value("1.0.0")
        )
        .andExpect(
                jsonPath("$.page").value(0)
        )
        .andExpect(
                jsonPath("$.size").value(20)
        )
        .andExpect(
                jsonPath("$.totalElements").value(1)
        )
        .andExpect(
                jsonPath("$.totalPages").value(1)
        )
        .andExpect(
                jsonPath("$.first").value(true)
        )
        .andExpect(
                jsonPath("$.last").value(true)
        )
        .andExpect(
                jsonPath("$.empty").value(false)
        );

        verify(knowledgeSearchApiService)
                .search(
                        eq(store.getId()),
                        any(KnowledgeSearchRequest.class)
                );
    }
    @Test
    void shouldReturn400WhenSearchRequestIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/knowledge")
                                .with(
                                        user(ACTOR)
                                                .roles("STORE_ADMIN")
                                )
                                .param(
                                        "minimumConfidence",
                                        "1.5000"
                                )
                                .param("page", "-1")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La solicitud contiene valores inválidos"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/knowledge")
                )
                .andExpect(
                        jsonPath("$.violations").isArray()
                )
                .andExpect(
                        jsonPath("$.violations").isNotEmpty()
                );
    }
    
    @Test
    void shouldAllowAuthenticatedKnowledgeSearchWithoutCsrf()
            throws Exception {

        Store store = createStore();

        KnowledgePageResponse<KnowledgeSummaryResponse> response =
                new KnowledgePageResponse<>(
                        List.of(),
                        0,
                        20,
                        0L,
                        0,
                        true,
                        true,
                        true
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                knowledgeSearchApiService.search(
                        eq(store.getId()),
                        any(KnowledgeSearchRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get("/api/knowledge")
                                .with(
                                        user(ACTOR)
                                                .roles("STORE_ADMIN")
                                )
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                );
    }
    
    @Test
    void shouldRejectUnauthenticatedKnowledgeSearch()
            throws Exception {

    	mockMvc.perform(
                get("/api/knowledge")
                        .param("page", "0")
                        .param("size", "20")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(
                header().string(
                        "Location",
                        "http://localhost/admin/login"
                )
        );
    }
    
    @Test
    void shouldFindKnowledgeByIdAndReturn200()
            throws Exception {

        Store store = createStore();

        KnowledgeDetailResponse response =
                createDetailResponse();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                knowledgeDetailApiService.findById(
                        store.getId(),
                        101L
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get("/api/knowledge/101")
                                .with(
                                        user(ACTOR)
                                                .roles("STORE_ADMIN")
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-901")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PUBLISHED")
                )
                .andExpect(
                        jsonPath("$.currentVersion.id")
                                .value(201)
                )
                .andExpect(
                        jsonPath("$.currentVersion.semanticVersion")
                                .value("1.0.0")
                )
                .andExpect(
                        jsonPath("$.currentVersion.title")
                                .value(
                                        "Knowledge Query Architecture"
                                )
                );

        verify(knowledgeDetailApiService)
                .findById(
                        store.getId(),
                        101L
                );
    }
    
    @Test
    void shouldReturn404WhenKnowledgeIdDoesNotExist()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                knowledgeDetailApiService.findById(
                        store.getId(),
                        999L
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        get("/api/knowledge/999")
                                .with(
                                        user(ACTOR)
                                                .roles("STORE_ADMIN")
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No se encontró el KnowledgeObject con id 999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/knowledge/999")
                );
    }
    
    @Test
    void shouldFindKnowledgeByCodeAndReturn200()
            throws Exception {

        Store store = createStore();

        KnowledgeDetailResponse response =
                createDetailResponse();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                knowledgeDetailApiService.findByCode(
                        store.getId(),
                        "KS-901"
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get("/api/knowledge/code/KS-901")
                                .with(
                                        user(ACTOR)
                                                .roles("STORE_ADMIN")
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-901")
                )
                .andExpect(
                        jsonPath("$.currentVersion.semanticVersion")
                                .value("1.0.0")
                );

        verify(knowledgeDetailApiService)
                .findByCode(
                        store.getId(),
                        "KS-901"
                );
    }
    
    @Test
    void shouldReturn404WhenKnowledgeCodeDoesNotExist()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                knowledgeDetailApiService.findByCode(
                        store.getId(),
                        "KS-999"
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        "No se encontró el KnowledgeObject con código KS-999"
                )
        );

        mockMvc.perform(
                        get("/api/knowledge/code/KS-999")
                                .with(
                                        user(ACTOR)
                                                .roles("STORE_ADMIN")
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No se encontró el KnowledgeObject "
                                                + "con código KS-999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/knowledge/code/KS-999"
                                )
                );
    }
    
    private KnowledgeSearchRequest createSearchRequest() {
        return new KnowledgeSearchRequest(
                null,
                null,
                KnowledgeDomain.values()[0],
                null,
                null,
                KnowledgeStatus.PUBLISHED,
                null,
                null,
                new BigDecimal("0.9000"),
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        40
                ),
                "architecture",
                0,
                20
        );
    }

    private KnowledgeSummaryResponse createSummaryResponse() {
        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        0
                );

        return new KnowledgeSummaryResponse(
                101L,
                "KS-901",
                KnowledgeTypeCode.values()[0],
                KnowledgeDomain.values()[0],
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                KnowledgeStatus.PUBLISHED,
                KnowledgeContextType.STORE,
                "10",
                201L,
                "1.0.0",
                "Knowledge Query Architecture",
                "Arquitectura de consultas del Knowledge Engine.",
                new BigDecimal("0.9500"),
                createdAt,
                null,
                createdAt,
                createdAt
        );
    }

    private KnowledgeDetailResponse createDetailResponse() {
        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        0
                );

        KnowledgeDetailResponse.VersionDetail version =
                new KnowledgeDetailResponse.VersionDetail(
                        201L,
                        "1.0.0",
                        "Knowledge Query Architecture",
                        "Arquitectura de consultas del Knowledge Engine.",
                        "# Knowledge Query Architecture",
                        "MARKDOWN",

                        "<h1>Knowledge Query Architecture</h1>",
                        "MARKDOWN",

                        new BigDecimal("0.9500"),
                        "KS-000",
                        createdAt,
                        createdAt,
                        ACTOR,
                        ACTOR
                );

        return new KnowledgeDetailResponse(
                101L,
                "KS-901",
                KnowledgeTypeCode.values()[0],
                KnowledgeDomain.values()[0],
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                KnowledgeStatus.PUBLISHED,
                KnowledgeContextType.STORE,
                "10",
                createdAt,
                null,
                createdAt,
                createdAt,
                version,
                version
        );
    }

    private Store createStore() {
        Store store = new Store();

        ReflectionTestUtils.setField(
                store,
                "id",
                10L
        );

        store.setNombre("Knowledge Query Store");
        store.setDominio("knowledge-query.local");
        store.setActiva(true);

        return store;
    }
    
}