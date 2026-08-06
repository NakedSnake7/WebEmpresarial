package com.webempresarial.store.digitaltransformation.domain.project;

import com.webempresarial.store.model.Store;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransformationProjectTest {

    @Test
    void shouldCreateProjectInCreatedStatus() {
        Store store = persistedStore(1L);

        TransformationProject project =
                TransformationProject.create(
                        store,
                        "DTE-ROBERT-SLINGERLAND",
                        "Robert Slingerland Digital Transformation",
                        "Robert Slingerland",
                        "https://robertslingerland.com",
                        TransformationProjectType.BRAND_EXPERIENCE,
                        "Crear una experiencia digital premium"
                );

        assertThat(project.getStatus())
                .isEqualTo(TransformationProjectStatus.CREATED);

        assertThat(project.isSourceOfTruthLocked()).isFalse();
        assertThat(project.getCurrentBlueprintVersion()).isNull();
        assertThat(project.getStore()).isSameAs(store);
    }

    @Test
    void shouldMoveProjectToSourcesPending() {
        TransformationProject project = validProject();

        project.markSourcesPending();

        assertThat(project.getStatus())
                .isEqualTo(
                        TransformationProjectStatus.SOURCES_PENDING
                );
    }

    @Test
    void shouldLockSourceOfTruthAfterSourcesAreIngested() {
        TransformationProject project = validProject();

        project.markSourcesPending();
        project.markSourcesIngested();
        project.lockSourceOfTruth();

        assertThat(project.getStatus())
                .isEqualTo(
                        TransformationProjectStatus.SOURCES_INGESTED
                );

        assertThat(project.isSourceOfTruthLocked()).isTrue();
    }

    @Test
    void shouldRejectLockBeforeSourcesAreIngested() {
        TransformationProject project = validProject();

        assertThatThrownBy(project::lockSourceOfTruth)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "cuando los documentos han sido ingeridos"
                );
    }

    @Test
    void shouldRejectSourceModificationWhenLocked() {
        TransformationProject project = validProject();

        project.markSourcesPending();
        project.markSourcesIngested();
        project.lockSourceOfTruth();

        assertThatThrownBy(
                project::ensureSourcesCanBeModified
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueadas");
    }

    @Test
    void shouldRejectUnpersistedStore() {
        Store store = mock(Store.class);

        when(store.getId()).thenReturn(null);

        assertThatThrownBy(() ->
                TransformationProject.create(
                        store,
                        "DTE-001",
                        "Proyecto",
                        "Cliente",
                        null,
                        TransformationProjectType.WEBSITE_TRANSFORMATION,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("store");
    }

    private static TransformationProject validProject() {
        return TransformationProject.create(
                persistedStore(1L),
                "DTE-001",
                "Proyecto de Transformación",
                "Cliente",
                "https://example.com",
                TransformationProjectType.BRAND_EXPERIENCE,
                "Transformar la experiencia digital"
        );
    }

    private static Store persistedStore(Long id) {
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(id);
        return store;
    }
}