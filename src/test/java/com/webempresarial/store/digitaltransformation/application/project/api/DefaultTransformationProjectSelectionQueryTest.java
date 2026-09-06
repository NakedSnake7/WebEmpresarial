package com.webempresarial.store.digitaltransformation.application.project.api;

import com.webempresarial.store.digitaltransformation.domain.project.*;
import com.webempresarial.store.model.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTransformationProjectSelectionQueryTest {

    @Mock
    private TransformationProjectRepository projectRepository;

    private DefaultTransformationProjectSelectionQuery query;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        query =
                new DefaultTransformationProjectSelectionQuery(
                        projectRepository
                );
    }

    @Test
    void shouldReturnProjectsForTenantOrderedByRepository() {
        TransformationProject first =
                project(
                        101L,
                        "TRF-002",
                        "Second project",
                        "Client B"
                );

        TransformationProject second =
                project(
                        100L,
                        "TRF-001",
                        "First project",
                        "Client A"
                );

        when(
                projectRepository
                        .findAllByStoreIdOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        List<TransformationProjectOptionResponse> result =
                query.findAvailableProjects(
                        10L
                );

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).id())
                .isEqualTo(101L);

        assertThat(result.get(0).code())
                .isEqualTo("TRF-002");

        assertThat(result.get(0).name())
                .isEqualTo("Second project");

        assertThat(result.get(0).clientName())
                .isEqualTo("Client B");

        assertThat(result.get(0).status())
                .isEqualTo(
                        TransformationProjectStatus.CREATED
                );

        assertThat(result.get(1).id())
                .isEqualTo(100L);

        verify(projectRepository)
                .findAllByStoreIdOrderByCreatedAtDesc(
                        10L
                );
    }

    @Test
    void shouldReturnEmptyListWhenTenantHasNoProjects() {
        when(
                projectRepository
                        .findAllByStoreIdOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(
                List.of()
        );

        assertThat(
                query.findAvailableProjects(
                        10L
                )
        ).isEmpty();
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeRepositoryAccess() {
        assertThatThrownBy(() ->
                query.findAvailableProjects(
                        0L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                projectRepository
        );
    }

    @Test
    void shouldRejectNullStoreIdBeforeRepositoryAccess() {
        assertThatThrownBy(() ->
                query.findAvailableProjects(
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                projectRepository
        );
    }

    @Test
    void shouldRejectNullListReturnedByRepository() {
        when(
                projectRepository
                        .findAllByStoreIdOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                query.findAvailableProjects(
                        10L
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "lista nula"
                );
    }

    @Test
    void shouldPreserveProjectMetadata() {
        TransformationProject project =
                project(
                        100L,
                        "TRF-001",
                        "Digital Transformation",
                        "Robert Slingerland"
                );

        when(
                projectRepository
                        .findAllByStoreIdOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(
                List.of(
                        project
                )
        );

        TransformationProjectOptionResponse response =
                query.findAvailableProjects(
                        10L
                ).get(0);

        assertThat(response.projectType())
                .isEqualTo(
                        project.getProjectType()
                );

        assertThat(response.status())
                .isEqualTo(
                        project.getStatus()
                );

        assertThat(response.sourceOfTruthLocked())
                .isEqualTo(
                        project.isSourceOfTruthLocked()
                );

        assertThat(response.currentBlueprintVersion())
                .isEqualTo(
                        project.getCurrentBlueprintVersion()
                );

        assertThat(response.createdAt())
                .isEqualTo(
                        project.getCreatedAt()
                );

        assertThat(response.updatedAt())
                .isEqualTo(
                        project.getUpdatedAt()
                );
    }

    private static TransformationProject project(
            Long id,
            String code,
            String name,
            String clientName
    ) {
        Store store =
                mock(
                        Store.class
                );

        when(
                store.getId()
        ).thenReturn(
                10L
        );

        TransformationProject project =
                TransformationProject.create(
                        store,
                        code,
                        name,
                        clientName,
                        "https://example.com",
                        firstProjectType(),
                        "Executive intent"
                );

        assignField(
                project,
                "id",
                id
        );

        assignField(
                project,
                "createdAt",
                Instant.parse(
                        "2026-08-20T12:00:00Z"
                )
        );

        assignField(
                project,
                "updatedAt",
                Instant.parse(
                        "2026-08-20T12:30:00Z"
                )
        );

        return project;
    }

    private static TransformationProjectType firstProjectType() {
        return TransformationProjectType
                .values()[0];
    }

    private static void assignField(
            Object target,
            String fieldName,
            Object value
    ) {
        try {
            var field =
                    target.getClass()
                            .getDeclaredField(
                                    fieldName
                            );

            field.setAccessible(
                    true
            );

            field.set(
                    target,
                    value
            );

        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "No fue posible preparar el fixture",
                    exception
            );
        }
    }
}