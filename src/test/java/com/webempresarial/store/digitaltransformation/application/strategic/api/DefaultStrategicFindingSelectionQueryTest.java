package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStrategicFindingSelectionQueryTest {

    @Mock
    private StrategicArtifactRepository artifactRepository;

    private DefaultStrategicFindingSelectionQuery query;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        query =
                new DefaultStrategicFindingSelectionQuery(
                        artifactRepository
                );
    }

    @Test
    void shouldReturnAvailableFindingsForTenantAndProject() {
        StrategicArtifact first =
                finding(
                        101L,
                        "FND-001",
                        "Customers do not understand the value proposition."
                );

        StrategicArtifact second =
                finding(
                        102L,
                        "FND-002",
                        "The current experience creates unnecessary friction."
                );

        when(
                artifactRepository
                        .findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
                                20L,
                                10L,
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        List<StrategicFindingOptionResponse> result =
                query.findAvailableFindings(
                        10L,
                        20L
                );

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).id())
                .isEqualTo(101L);

        assertThat(result.get(0).code())
                .isEqualTo("FND-001");

        assertThat(result.get(0).statement())
                .isEqualTo(
                        "Customers do not understand the value proposition."
                );

        assertThat(result.get(0).status())
                .isEqualTo(
                        StrategicArtifactStatus.DRAFT
                );

        assertThat(result.get(0).confidence())
                .isEqualTo(
                        StrategicConfidence.STRONGLY_SUPPORTED
                );

        assertThat(result.get(0).requiresReview())
                .isFalse();

        assertThat(result.get(1).id())
                .isEqualTo(102L);

        assertThat(result.get(1).code())
                .isEqualTo("FND-002");

        verify(artifactRepository)
                .findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
                        20L,
                        10L,
                        StrategicArtifactType.FINDING
                );
    }

    @Test
    void shouldReturnEmptyListWhenProjectHasNoFindings() {
        when(
                artifactRepository
                        .findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
                                20L,
                                10L,
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                List.of()
        );

        assertThat(
                query.findAvailableFindings(
                        10L,
                        20L
                )
        ).isEmpty();
    }

    @Test
    void shouldRejectInvalidStoreIdBeforeRepositoryAccess() {
        assertThatThrownBy(() ->
                query.findAvailableFindings(
                        0L,
                        20L
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "storeId"
                );

        verifyNoInteractions(
                artifactRepository
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeRepositoryAccess() {
        assertThatThrownBy(() ->
                query.findAvailableFindings(
                        10L,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                artifactRepository
        );
    }

    @Test
    void shouldRejectNullListReturnedByRepository() {
        when(
                artifactRepository
                        .findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
                                20L,
                                10L,
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                query.findAvailableFindings(
                        10L,
                        20L
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
    void shouldRejectNonFindingArtifactReturnedByRepository() {
        StrategicArtifact invalid =
                artifact(
                        103L,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "Business problem"
                );

        when(
                artifactRepository
                        .findAllByProjectIdAndProjectStoreIdAndArtifactTypeOrderByCreatedAtAsc(
                                20L,
                                10L,
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                List.of(
                        invalid
                )
        );

        assertThatThrownBy(() ->
                query.findAvailableFindings(
                        10L,
                        20L
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no es FINDING"
                );
    }

    private static StrategicArtifact finding(
            Long id,
            String code,
            String statement
    ) {
        return artifact(
                id,
                code,
                StrategicArtifactType.FINDING,
                statement
        );
    }

    private static StrategicArtifact artifact(
            Long id,
            String code,
            StrategicArtifactType type,
            String statement
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact artifact =
                StrategicArtifact.create(
                        evidence.getProject(),
                        code,
                        type,
                        StrategicConfidence.STRONGLY_SUPPORTED,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        statement,
                        "Rationale",
                        "Business implication"
                );

        assignId(
                artifact,
                id
        );

        return artifact;
    }

    private static void assignId(
            StrategicArtifact artifact,
            Long id
    ) {
        try {
            Field field =
                    StrategicArtifact.class
                            .getDeclaredField(
                                    "id"
                            );

            field.setAccessible(
                    true
            );

            field.set(
                    artifact,
                    id
            );

        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "No fue posible asignar identidad al fixture",
                    exception
            );
        }
    }
}