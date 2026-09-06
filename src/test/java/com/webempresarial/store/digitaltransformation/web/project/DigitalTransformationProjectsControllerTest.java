package com.webempresarial.store.digitaltransformation.web.project;

import com.webempresarial.store.digitaltransformation.application.project.api.TransformationProjectOptionResponse;
import com.webempresarial.store.digitaltransformation.application.project.api.TransformationProjectSelectionQuery;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectStatus;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DigitalTransformationProjectsControllerTest {

    @Mock
    private TransformationProjectSelectionQuery
            projectSelectionQuery;

    @Mock
    private StoreResolver
            storeResolver;

    @Mock
    private HttpServletRequest
            request;

    @Mock
    private Store
            store;

    private DigitalTransformationProjectsController
            controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        controller =
                new DigitalTransformationProjectsController(
                        projectSelectionQuery,
                        storeResolver
                );
    }

    @Test
    void shouldRenderProjectsPageForCurrentTenant() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                store.getId()
        ).thenReturn(
                10L
        );

        when(
                store.isActiva()
        ).thenReturn(
                true
        );

        TransformationProjectOptionResponse project =
                project(
                        100L,
                        "TRF-001",
                        "Digital Transformation"
                );

        when(
                projectSelectionQuery.findAvailableProjects(
                        10L
                )
        ).thenReturn(
                List.of(
                        project
                )
        );

        Model model =
                new ExtendedModelMap();

        String view =
                controller.projects(
                        request,
                        model
                );

        assertThat(view)
                .isEqualTo(
                        "admin/digital-transformation/projects/index"
                );

        assertThat(
                model.getAttribute(
                        "transformationProjects"
                )
        )
                .isEqualTo(
                        List.of(project)
                );

        assertThat(
                model.getAttribute(
                        "digitalTransformationPage"
                )
        )
                .isEqualTo(
                        true
                );

        verify(projectSelectionQuery)
                .findAvailableProjects(
                        10L
                );
    }

    @Test
    void shouldRenderEmptyProjectList() {
        validStore();

        when(
                projectSelectionQuery.findAvailableProjects(
                        10L
                )
        ).thenReturn(
                List.of()
        );

        Model model =
                new ExtendedModelMap();

        String view =
                controller.projects(
                        request,
                        model
                );

        assertThat(view)
                .isEqualTo(
                        "admin/digital-transformation/projects/index"
                );

        assertThat(
                (List<?>) model.getAttribute(
                        "transformationProjects"
                )
        ).isEmpty();
    }

    @Test
    void shouldRejectMissingStore() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.projects(
                        request,
                        new ExtendedModelMap()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "resolver la tienda"
                );

        verifyNoInteractions(
                projectSelectionQuery
        );
    }

    @Test
    void shouldRejectStoreWithoutId() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                store.getId()
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.projects(
                        request,
                        new ExtendedModelMap()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "resolver la tienda"
                );

        verifyNoInteractions(
                projectSelectionQuery
        );
    }

    @Test
    void shouldRejectInactiveStore() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                store.getId()
        ).thenReturn(
                10L
        );

        when(
                store.isActiva()
        ).thenReturn(
                false
        );

        assertThatThrownBy(() ->
                controller.projects(
                        request,
                        new ExtendedModelMap()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "inactiva"
                );

        verifyNoInteractions(
                projectSelectionQuery
        );
    }

    @Test
    void shouldWrapStoreResolverFailure() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenThrow(
                new IllegalStateException(
                        "resolver failure"
                )
        );

        assertThatThrownBy(() ->
                controller.projects(
                        request,
                        new ExtendedModelMap()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "resolver la tienda"
                )
                .hasCauseInstanceOf(
                        IllegalStateException.class
                );

        verifyNoInteractions(
                projectSelectionQuery
        );
    }

    @Test
    void shouldRejectNullProjectListReturnedByQuery() {
        validStore();

        when(
                projectSelectionQuery.findAvailableProjects(
                        10L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.projects(
                        request,
                        new ExtendedModelMap()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "lista nula"
                );
    }

    private void validStore() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                store.getId()
        ).thenReturn(
                10L
        );

        when(
                store.isActiva()
        ).thenReturn(
                true
        );
    }

    private static TransformationProjectOptionResponse project(
            Long id,
            String code,
            String name
    ) {
        return new TransformationProjectOptionResponse(
                id,
                code,
                name,
                "Test Client",
                TransformationProjectType.values()[0],
                TransformationProjectStatus.CREATED,
                false,
                null,
                Instant.parse(
                        "2026-08-20T12:00:00Z"
                ),
                Instant.parse(
                        "2026-08-20T12:30:00Z"
                )
        );
    }
}