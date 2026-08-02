package com.webempresarial.store.knowledge.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeRequest;
import com.webempresarial.store.knowledge.api.dto.CreateKnowledgeVersionRequest;
import com.webempresarial.store.knowledge.api.dto.KnowledgeCreatedResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeLifecycleResponse;
import com.webempresarial.store.knowledge.api.dto.KnowledgeVersionCreatedResponse;
import com.webempresarial.store.knowledge.api.dto.PublishKnowledgeRequest;
import com.webempresarial.store.knowledge.api.exception.KnowledgeApiExceptionHandler;
import com.webempresarial.store.knowledge.api.exception.KnowledgeObjectNotFoundException;
import com.webempresarial.store.knowledge.api.exception.KnowledgeVersionNotFoundException;
import com.webempresarial.store.knowledge.api.exception.DuplicateKnowledgeCodeException;
import com.webempresarial.store.knowledge.api.exception.DuplicateKnowledgeVersionException;

import static org.mockito.Mockito.doThrow;
import com.webempresarial.store.knowledge.api.service.ApproveKnowledgeApiService;
import com.webempresarial.store.knowledge.api.service.ArchiveKnowledgeApiService;
import com.webempresarial.store.knowledge.api.service.CreateKnowledgeApiService;
import com.webempresarial.store.knowledge.api.service.CreateKnowledgeVersionApiService;
import com.webempresarial.store.knowledge.api.service.PublishKnowledgeApiService;
import com.webempresarial.store.knowledge.api.service.SubmitKnowledgeForReviewApiService;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.InventoryPersistentAlertService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.service.SubscriptionAccessService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = KnowledgeCommandController.class
)
@Import({
        SecurityConfig.class,
        KnowledgeApiExceptionHandler.class
})
class KnowledgeCommandControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateKnowledgeApiService createKnowledgeApiService;

    @MockitoBean
    private CreateKnowledgeVersionApiService
            createKnowledgeVersionApiService;

    @MockitoBean
    private SubmitKnowledgeForReviewApiService
            submitKnowledgeForReviewApiService;

    @MockitoBean
    private ApproveKnowledgeApiService approveKnowledgeApiService;

    @MockitoBean
    private PublishKnowledgeApiService publishKnowledgeApiService;

    @MockitoBean
    private ArchiveKnowledgeApiService archiveKnowledgeApiService;

    @MockitoBean
    private StoreResolver storeResolver;

    /*
     * SecurityConfig requiere estos dos servicios.
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
    private InventoryPersistentAlertService inventoryPersistentAlertService;
    
    @MockitoBean
    private StoreSettingsService storeSettingsService;
    
    @MockitoBean
    private SubscriptionAccessService subscriptionAccessService;
    
    @MockitoBean
    private StoreThemeResolver storeThemeResolver;
    
    @Test
    void shouldReturn400WhenKnowledgeCodeIsBlank() throws Exception {

        CreateKnowledgeRequest invalidRequest =
                new CreateKnowledgeRequest(
                        "",
                        KnowledgeTypeCode.values()[0],
                        KnowledgeDomain.values()[0],
                        KnowledgeClassification.values()[0],
                        KnowledgeRiskLevel.values()[0],
                        KnowledgeContextType.STORE,
                        "HTTP-CONTEXT",
                        "Knowledge HTTP Architecture",
                        "Resumen válido.",
                        "# Contenido válido",
                        "MARKDOWN",
                        new BigDecimal("0.9500"),
                        "KS-000"
                );

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
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
                        jsonPath("$.violations[0].field")
                                .value("code")
                )
                .andExpect(
                        jsonPath("$.violations[0].message")
                                .value(
                                        "El código del conocimiento es obligatorio"
                                )
                );
    }

    @Test
    void shouldCreateKnowledgeAndReturn201() throws Exception {

        Store store = createStore();

        CreateKnowledgeRequest request =
                createRequest("KS-861");

        KnowledgeCreatedResponse serviceResponse =
                new KnowledgeCreatedResponse(
                        101L,
                        "KS-861",
                        KnowledgeStatus.DRAFT,
                        201L,
                        "1.0.0",
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                12,
                                0
                        )
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                createKnowledgeApiService.create(
                        eq(store),
                        any(CreateKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "http://localhost/api/knowledge/101"
                        )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-861")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("DRAFT")
                )
                .andExpect(
                        jsonPath("$.initialVersionId")
                                .value(201)
                )
                .andExpect(
                        jsonPath("$.semanticVersion")
                                .value("1.0.0")
                );

        verify(createKnowledgeApiService)
                .create(
                        eq(store),
                        any(CreateKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                );
    }

    private CreateKnowledgeRequest createRequest(
            String code
    ) {
        return new CreateKnowledgeRequest(
                code,
                KnowledgeTypeCode.values()[0],
                KnowledgeDomain.values()[0],
                KnowledgeClassification.values()[0],
                KnowledgeRiskLevel.values()[0],
                KnowledgeContextType.STORE,
                "HTTP-CONTEXT",
                "Knowledge HTTP Architecture",
                "Resumen de la prueba HTTP.",
                "# Knowledge HTTP Architecture",
                "MARKDOWN",
                new BigDecimal("0.9500"),
                "KS-000"
        );
    }

    private Store createStore() {
        Store store = new Store();

        /*
         * El servicio simulado solo requiere un Store resuelto
         * y activo. El ID debe existir para las validaciones
         * del controlador.
         */
        setStoreId(store, 10L);

        store.setNombre("HTTP Knowledge Store");
        store.setDominio("knowledge-http.local");
        store.setActiva(true);

        return store;
    }

    private void setStoreId(
            Store store,
            Long id
    ) {
        org.springframework.test.util.ReflectionTestUtils
                .setField(
                        store,
                        "id",
                        id
                );
    }
    
    @Test
    void shouldRejectUnauthenticatedCreateRequest() throws Exception {

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest("KS-862")
                                        )
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                );
    }
    @Test
    void shouldRejectCreateWithoutCsrfToken() throws Exception {

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest("KS-863")
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }
    @Test
    void shouldRejectUserWithoutAdministrativeRole() throws Exception {

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("cliente@webempresarial.com")
                                                .roles("CLIENTE")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest("KS-864")
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }
    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        CreateKnowledgeRequest invalidRequest =
                new CreateKnowledgeRequest(
                        "",
                        null,
                        null,
                        null,
                        null,
                        KnowledgeContextType.STORE,
                        null,
                        "",
                        "",
                        "",
                        "",
                        new BigDecimal("1.5000"),
                        null
                );

        mockMvc.perform(
                post("/api/knowledge")
                        .with(
                                user("admin@webempresarial.com")
                                        .roles("STORE_ADMIN")
                        )
                        .with(csrf())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        invalidRequest
                                )
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.status")
                        .value(400)
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
                jsonPath("$.violations")
                        .isArray()
        )
        .andExpect(
                jsonPath("$.violations")
                        .isNotEmpty()
        );
    }
    @Test
    void shouldReturn409WhenKnowledgeCodeAlreadyExists()
            throws Exception {

        Store store = createStore();

        CreateKnowledgeRequest request =
                createRequest("KS-865");

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                createKnowledgeApiService.create(
                        eq(store),
                        any(CreateKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new DuplicateKnowledgeCodeException(
                        "KS-865"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ya existe un KnowledgeObject "
                                                + "con el código KS-865 "
                                                + "dentro de la tienda actual"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/knowledge")
                );
    }
    @Test
    void shouldReturn422WhenTenantCannotBeResolved()
            throws Exception {

        when(storeResolver.getCurrentStore(any()))
                .thenThrow(
                        new RuntimeException(
                                "Dominio no encontrado"
                        )
                );

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest("KS-866")
                                        )
                                )
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No fue posible resolver "
                                                + "la tienda de la solicitud"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/knowledge")
                );
    }
    @Test
    void shouldReturn422WhenStoreIsInactive()
            throws Exception {

        Store store = createStore();
        store.setActiva(false);

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        mockMvc.perform(
                        post("/api/knowledge")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest("KS-867")
                                        )
                                )
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La tienda se encuentra inactiva"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/knowledge")
                );
    }
    @Test
    void shouldCreateKnowledgeVersionAndReturn201()
            throws Exception {

        Store store = createStore();

        CreateKnowledgeVersionRequest request =
                createVersionRequest(
                        1,
                        1,
                        0
                );

        KnowledgeVersionCreatedResponse serviceResponse =
                new KnowledgeVersionCreatedResponse(
                        101L,
                        202L,
                        "1.1.0",
                        "Knowledge HTTP Architecture 1.1",
                        new BigDecimal("0.9700"),
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                13,
                                15
                        )
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                createKnowledgeVersionApiService.create(
                        eq(store.getId()),
                        eq(101L),
                        any(CreateKnowledgeVersionRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/knowledge/101/versions")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "http://localhost/api/knowledge/101/versions/202"
                        )
                )
                .andExpect(
                        jsonPath("$.knowledgeObjectId")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.versionId")
                                .value(202)
                )
                .andExpect(
                        jsonPath("$.semanticVersion")
                                .value("1.1.0")
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Knowledge HTTP Architecture 1.1"
                                )
                )
                .andExpect(
                        jsonPath("$.confidence")
                                .value(0.9700)
                );

        verify(createKnowledgeVersionApiService)
                .create(
                        eq(store.getId()),
                        eq(101L),
                        any(CreateKnowledgeVersionRequest.class),
                        eq("admin@webempresarial.com")
                );
    }
    @Test
    void shouldReturn400WhenVersionRequestIsInvalid()
            throws Exception {

        CreateKnowledgeVersionRequest invalidRequest =
                new CreateKnowledgeVersionRequest(
                        -1,
                        -1,
                        -1,
                        "",
                        "",
                        "",
                        "",
                        new BigDecimal("1.5000"),
                        null
                );

        mockMvc.perform(
                        post("/api/knowledge/101/versions")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
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
                                .value(
                                        "/api/knowledge/101/versions"
                                )
                )
                .andExpect(
                        jsonPath("$.violations")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.violations")
                                .isNotEmpty()
                );
    }
    @Test
    void shouldReturn409WhenSemanticVersionAlreadyExists()
            throws Exception {

        Store store = createStore();

        CreateKnowledgeVersionRequest request =
                createVersionRequest(
                        1,
                        0,
                        0
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                createKnowledgeVersionApiService.create(
                        eq(store.getId()),
                        eq(101L),
                        any(CreateKnowledgeVersionRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new DuplicateKnowledgeVersionException(
                        "1.0.0"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/versions")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La versión 1.0.0 ya existe "
                                                + "para el KnowledgeObject"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/knowledge/101/versions"
                                )
                );
    }
    @Test
    void shouldReturn404WhenCreatingVersionForUnknownKnowledgeObject()
            throws Exception {

        Store store = createStore();

        CreateKnowledgeVersionRequest request =
                createVersionRequest(
                        1,
                        1,
                        0
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                createKnowledgeVersionApiService.create(
                        eq(store.getId()),
                        eq(999L),
                        any(CreateKnowledgeVersionRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/999/versions")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
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
                                        "No se encontró el KnowledgeObject "
                                                + "con id 999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/knowledge/999/versions"
                                )
                );
    }
    @Test
    void shouldReturn422WhenKnowledgeObjectDoesNotAllowNewVersions()
            throws Exception {

        Store store = createStore();

        CreateKnowledgeVersionRequest request =
                createVersionRequest(
                        2,
                        0,
                        0
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                createKnowledgeVersionApiService.create(
                        eq(store.getId()),
                        eq(101L),
                        any(CreateKnowledgeVersionRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new IllegalStateException(
                        "No pueden crearse versiones cuando "
                                + "el objeto está en estado PUBLISHED"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/versions")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Unprocessable Entity"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No pueden crearse versiones cuando "
                                                + "el objeto está en estado PUBLISHED"
                                )
                );
    }
    
    @Test
    void shouldSubmitKnowledgeForReviewAndReturn200()
            throws Exception {

        Store store = createStore();

        KnowledgeLifecycleResponse serviceResponse =
                new KnowledgeLifecycleResponse(
                        101L,
                        "KS-871",
                        KnowledgeStatus.IN_REVIEW,
                        null,
                        null,
                        null,
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                13,
                                25
                        )
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                submitKnowledgeForReviewApiService.submit(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/knowledge/101/submit-review")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-871")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_REVIEW")
                )
                .andExpect(
                        jsonPath("$.currentVersionId")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.validFrom")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.validUntil")
                                .doesNotExist()
                );

        verify(submitKnowledgeForReviewApiService)
                .submit(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                );
    }
    
    @Test
    void shouldReturn404WhenSubmittingUnknownKnowledgeForReview()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                submitKnowledgeForReviewApiService.submit(
                        eq(store.getId()),
                        eq(999L),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/999/submit-review")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
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
                                .value(
                                        "/api/knowledge/999/submit-review"
                                )
                );
    }
    
    @Test
    void shouldReturn422WhenKnowledgeCannotBeSubmittedForReview()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                submitKnowledgeForReviewApiService.submit(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new IllegalStateException(
                        "Solo el conocimiento en estado DRAFT puede enviarse a revisión"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/submit-review")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unprocessable Entity")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Solo el conocimiento en estado DRAFT puede enviarse a revisión"
                                )
                );
    }
    @Test
    void shouldApproveKnowledgeAndReturn200()
            throws Exception {

        Store store = createStore();

        KnowledgeLifecycleResponse serviceResponse =
                new KnowledgeLifecycleResponse(
                        101L,
                        "KS-872",
                        KnowledgeStatus.APPROVED,
                        null,
                        null,
                        null,
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                13,
                                30
                        )
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                approveKnowledgeApiService.approve(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/knowledge/101/approve")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-872")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("APPROVED")
                );

        verify(approveKnowledgeApiService)
                .approve(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                );
    }
    @Test
    void shouldReturn404WhenApprovingUnknownKnowledge()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                approveKnowledgeApiService.approve(
                        eq(store.getId()),
                        eq(999L),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/999/approve")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/knowledge/999/approve"
                                )
                );
    }
    @Test
    void shouldReturn422WhenKnowledgeCannotBeApproved()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                approveKnowledgeApiService.approve(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new IllegalStateException(
                        "Solo el conocimiento en revisión puede aprobarse"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/approve")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Solo el conocimiento en revisión puede aprobarse"
                                )
                );
    }
    
    @Test
    void shouldPublishKnowledgeAndReturn200()
            throws Exception {

        Store store = createStore();

        LocalDateTime validFrom =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        30
                );

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        202L,
                        validFrom,
                        null
                );

        KnowledgeLifecycleResponse serviceResponse =
                new KnowledgeLifecycleResponse(
                        101L,
                        "KS-873",
                        KnowledgeStatus.PUBLISHED,
                        202L,
                        validFrom,
                        null,
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                13,
                                31
                        )
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                publishKnowledgeApiService.publish(
                        eq(store.getId()),
                        eq(101L),
                        any(PublishKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/knowledge/101/publish")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-873")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PUBLISHED")
                )
                .andExpect(
                        jsonPath("$.currentVersionId")
                                .value(202)
                )
                .andExpect(
                        jsonPath("$.validFrom")
                                .value("2026-08-02T13:30:00")
                )
                .andExpect(
                        jsonPath("$.validUntil")
                                .value(
                                        org.hamcrest.Matchers.nullValue()
                                )
                );

        verify(publishKnowledgeApiService)
                .publish(
                        eq(store.getId()),
                        eq(101L),
                        any(PublishKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                );
    }
    
    @Test
    void shouldReturn400WhenPublishRequestIsInvalid()
            throws Exception {

        PublishKnowledgeRequest invalidRequest =
                new PublishKnowledgeRequest(
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        post("/api/knowledge/101/publish")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
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
                                .value(
                                        "/api/knowledge/101/publish"
                                )
                )
                .andExpect(
                        jsonPath("$.violations")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.violations")
                                .isNotEmpty()
                );
    }
    @Test
    void shouldReturn400WhenPublishValidityPeriodIsInvalid()
            throws Exception {

        PublishKnowledgeRequest invalidRequest =
                new PublishKnowledgeRequest(
                        202L,
                        LocalDateTime.of(
                                2026,
                                8,
                                10,
                                10,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                9,
                                10,
                                0
                        )
                );

        mockMvc.perform(
                        post("/api/knowledge/101/publish")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.violations")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.violations")
                                .isNotEmpty()
                );
    }
    @Test
    void shouldReturn404WhenPublishingUnknownKnowledge()
            throws Exception {

        Store store = createStore();

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        202L,
                        LocalDateTime.now(),
                        null
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                publishKnowledgeApiService.publish(
                        eq(store.getId()),
                        eq(999L),
                        any(PublishKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/999/publish")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
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
                                        "No se encontró el KnowledgeObject con id 999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/knowledge/999/publish"
                                )
                );
    }
    @Test
    void shouldReturn404WhenPublishingUnknownVersion()
            throws Exception {

        Store store = createStore();

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        999L,
                        LocalDateTime.now(),
                        null
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                publishKnowledgeApiService.publish(
                        eq(store.getId()),
                        eq(101L),
                        any(PublishKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new KnowledgeVersionNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/publish")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
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
                                        "No se encontró la KnowledgeObjectVersion con id 999"
                                )
                );
    }
    @Test
    void shouldReturn422WhenKnowledgeCannotBePublished()
            throws Exception {

        Store store = createStore();

        PublishKnowledgeRequest request =
                new PublishKnowledgeRequest(
                        202L,
                        LocalDateTime.now(),
                        null
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                publishKnowledgeApiService.publish(
                        eq(store.getId()),
                        eq(101L),
                        any(PublishKnowledgeRequest.class),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new IllegalStateException(
                        "Solo el conocimiento aprobado puede publicarse"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/publish")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unprocessable Entity")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Solo el conocimiento aprobado puede publicarse"
                                )
                );
    }
    @Test
    void shouldArchiveKnowledgeAndReturn200()
            throws Exception {

        Store store = createStore();

        LocalDateTime validFrom =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        30
                );

        KnowledgeLifecycleResponse serviceResponse =
                new KnowledgeLifecycleResponse(
                        101L,
                        "KS-874",
                        KnowledgeStatus.ARCHIVED,
                        202L,
                        validFrom,
                        null,
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                13,
                                45
                        )
                );

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                archiveKnowledgeApiService.archive(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                )
        ).thenReturn(serviceResponse);

        mockMvc.perform(
                        post("/api/knowledge/101/archive")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(101)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("KS-874")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ARCHIVED")
                )
                .andExpect(
                        jsonPath("$.currentVersionId")
                                .value(202)
                );

        verify(archiveKnowledgeApiService)
                .archive(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                );
    }
    
    @Test
    void shouldReturn404WhenArchivingUnknownKnowledge()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                archiveKnowledgeApiService.archive(
                        eq(store.getId()),
                        eq(999L),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new KnowledgeObjectNotFoundException(
                        999L
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/999/archive")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No se encontró el KnowledgeObject con id 999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/knowledge/999/archive"
                                )
                );
    }
    @Test
    void shouldReturn422WhenKnowledgeCannotBeArchived()
            throws Exception {

        Store store = createStore();

        when(storeResolver.getCurrentStore(any()))
                .thenReturn(store);

        when(
                archiveKnowledgeApiService.archive(
                        eq(store.getId()),
                        eq(101L),
                        eq("admin@webempresarial.com")
                )
        ).thenThrow(
                new IllegalStateException(
                        "Solo el conocimiento publicado puede archivarse"
                )
        );

        mockMvc.perform(
                        post("/api/knowledge/101/archive")
                                .with(
                                        user("admin@webempresarial.com")
                                                .roles("STORE_ADMIN")
                                )
                                .with(csrf())
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(422)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unprocessable Entity")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Solo el conocimiento publicado puede archivarse"
                                )
                );
    }
    
    private CreateKnowledgeVersionRequest createVersionRequest(
            int major,
            int minor,
            int patch
    ) {
        return new CreateKnowledgeVersionRequest(
                major,
                minor,
                patch,
                "Knowledge HTTP Architecture 1.1",
                "Nueva versión creada desde la API HTTP.",
                "# Knowledge HTTP Architecture 1.1",
                "MARKDOWN",
                new BigDecimal("0.9700"),
                "KS-000"
        );
    }
    
}