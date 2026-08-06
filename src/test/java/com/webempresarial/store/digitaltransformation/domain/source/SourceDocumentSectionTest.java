package com.webempresarial.store.digitaltransformation.domain.source;

import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SourceDocumentSectionTest {

    @Test
    void shouldCreateSectionFromVerifiedContent() {
        SourceDocumentSection section =
                SourceDocumentSection.create(
                        TestSources.verifiedContent(),
                        "AUDIT-EXECUTIVE-OVERVIEW",
                        SourceSectionType.EXECUTIVE_SUMMARY,
                        "Panorama General",
                        2,
                        2,
                        1,
                        "La marca de Robert es hoy más fuerte " +
                        "que su plataforma digital.",
                        "Resumen de la posición digital actual."
                );

        assertThat(section.getSectionCode())
                .isEqualTo("AUDIT-EXECUTIVE-OVERVIEW");

        assertThat(section.containsPage(2)).isTrue();
        assertThat(section.containsPage(3)).isFalse();
    }

    @Test
    void shouldRejectSectionFromUnverifiedContent() {
        SourceDocumentContent content =
                SourceDocumentContent.create(
                        TestSources.validSource(),
                        1,
                        SourceContentExtractionMethod.MANUAL,
                        null,
                        null
                );

        assertThatThrownBy(() ->
                SourceDocumentSection.create(
                        content,
                        "SECTION-001",
                        SourceSectionType.OTHER,
                        "Sección",
                        1,
                        1,
                        1,
                        "Contenido",
                        null
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verificado");
    }

    @Test
    void shouldRejectInvalidPageRange() {
        assertThatThrownBy(() ->
                SourceDocumentSection.create(
                        TestSources.verifiedContent(),
                        "SECTION-001",
                        SourceSectionType.OTHER,
                        "Sección",
                        4,
                        2,
                        1,
                        "Contenido",
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("página final");
    }
}